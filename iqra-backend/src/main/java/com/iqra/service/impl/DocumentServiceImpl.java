package com.iqra.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iqra.common.BusinessException;
import com.iqra.common.Constants;
import com.iqra.config.FileStorageConfig;
import com.iqra.mapper.DocumentChunkMapper;
import com.iqra.mapper.DocumentMapper;
import com.iqra.model.entity.Document;
import com.iqra.model.entity.DocumentChunk;
import com.iqra.model.vo.DocumentVO;
import com.iqra.service.DocumentService;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    private final FileStorageConfig fileStorageConfig;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final DocumentChunkMapper documentChunkMapper;

    @Override
    @Transactional
    public Document uploadDocument(MultipartFile file, String department, String tags, String uploadBy) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);

        // 校验文件类型
        if (!isAllowedExtension(extension)) {
            throw new BusinessException("不支持的文件格式，仅支持: " + String.join(", ", fileStorageConfig.getAllowedExtensions()));
        }

        // 校验文件大小
        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            throw new BusinessException("文件大小超过限制: " + (fileStorageConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        // 保存文件 - 使用绝对路径而不是相对路径
        String uuidFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + "." + extension;
        Path uploadPath = Paths.get(fileStorageConfig.getUploadPath()).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                log.error("创建上传目录失败: {}", uploadPath, e);
                throw new BusinessException("创建上传目录失败");
            }
        }

        try {
            Path filePath = uploadPath.resolve(uuidFileName);
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            log.error("上传文件失败", e);
            throw new BusinessException("上传文件失败");
        }

        // 保存文档记录
        Document document = new Document();
        document.setFileName(originalFilename);
        document.setFilePath(uploadPath.resolve(uuidFileName).toString());
        document.setFileType(extension);
        document.setFileSize(file.getSize());
        document.setUploadBy(uploadBy);
        document.setDepartment(department);
        document.setTags(tags);
        document.setStatus(Constants.DOC_STATUS_PENDING);
        this.save(document);

        // 异步处理文档
        processDocumentAsync(document.getId());

        return document;
    }

    @Override
    public Page<DocumentVO> getDocumentList(Integer pageNum, Integer pageSize, String fileName, String fileType, String status) {
        Page<Document> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();

        if (fileName != null && !fileName.isEmpty()) {
            wrapper.like(Document::getFileName, fileName);
        }
        if (fileType != null && !fileType.isEmpty()) {
            wrapper.eq(Document::getFileType, fileType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Document::getStatus, status);
        }

        wrapper.orderByDesc(Document::getCreateTime);
        Page<Document> documentPage = this.page(page, wrapper);

        Page<DocumentVO> voPage = new Page<>();
        BeanUtils.copyProperties(documentPage, voPage, "records");
        List<DocumentVO> voList = new ArrayList<>();
        for (Document doc : documentPage.getRecords()) {
            DocumentVO vo = new DocumentVO();
            BeanUtils.copyProperties(doc, vo);
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public boolean deleteDocument(Long id) {
        Document document = this.getById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 删除物理文件
        try {
            Files.deleteIfExists(Paths.get(document.getFilePath()));
        } catch (IOException e) {
            log.warn("删除物理文件失败: {}", document.getFilePath());
        }

        // 删除分块记录
        LambdaQueryWrapper<DocumentChunk> chunkWrapper = new LambdaQueryWrapper<>();
        chunkWrapper.eq(DocumentChunk::getDocumentId, id);
        documentChunkMapper.delete(chunkWrapper);

        return this.removeById(id);
    }

    @Async
    public void processDocumentAsync(Long documentId) {
        Document document = this.getById(documentId);
        if (document == null) {
            return;
        }

        try {
            // 更新状态为处理中
            document.setStatus(Constants.DOC_STATUS_PROCESSING);
            this.updateById(document);

            // 解析文档
            String content = parseDocument(document.getFilePath(), document.getFileType());

            // 分块
            List<TextSegment> segments = splitDocument(content, document.getId());

            // 向量化并入库
            for (TextSegment segment : segments) {
                dev.langchain4j.data.embedding.Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
            }

            // 保存分块记录到MySQL
            saveDocumentChunks(document.getId(), segments);

            // 更新状态为成功
            document.setStatus(Constants.DOC_STATUS_SUCCESS);
            this.updateById(document);

            log.info("文档处理成功: {}", document.getFileName());
        } catch (Exception e) {
            log.error("文档处理失败: {}", document.getFileName(), e);
            document.setStatus(Constants.DOC_STATUS_FAILED);

            // 提供更友好的错误信息
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("ConnectException")) {
                errorMessage = "Embedding模型服务不可用，请检查Ollama服务是否启动（端口11437）";
            }
            document.setErrorMessage(errorMessage);
            this.updateById(document);
        }
    }

    @Override
    public void processDocument(Long documentId) {
        processDocumentAsync(documentId);
    }

    private String parseDocument(String filePath, String fileType) throws Exception {
        Tika tika = new Tika();
        return tika.parseToString(Paths.get(filePath));
    }

    private List<TextSegment> splitDocument(String content, Long documentId) {
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
        dev.langchain4j.data.document.Document lcDocument = dev.langchain4j.data.document.Document.from(content);
        List<TextSegment> segments = splitter.split(lcDocument);

        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            segment.metadata().put("documentId", String.valueOf(documentId));
            segment.metadata().put("chunkIndex", i);
        }

        return segments;
    }

    private void saveDocumentChunks(Long documentId, List<TextSegment> segments) {
        for (int i = 0; i < segments.size(); i++) {
            DocumentChunk chunk = new DocumentChunk();
            chunk.setChunkId(UUID.randomUUID().toString());
            chunk.setDocumentId(documentId);
            chunk.setContent(segments.get(i).text());
            chunk.setChunkIndex(i);
            chunk.setCreateTime(LocalDateTime.now());
            documentChunkMapper.insert(chunk);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        for (String allowed : fileStorageConfig.getAllowedExtensions()) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}
