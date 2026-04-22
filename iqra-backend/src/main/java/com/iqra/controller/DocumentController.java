package com.iqra.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iqra.common.Result;
import com.iqra.model.entity.Document;
import com.iqra.model.vo.DocumentVO;
import com.iqra.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public Result<Document> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "department", required = false) String department,
                                    @RequestParam(value = "tags", required = false) String tags,
                                    @RequestParam(value = "uploadBy", defaultValue = "system") String uploadBy) {
        Document document = documentService.uploadDocument(file, department, tags, uploadBy);
        return Result.success(document);
    }

    @GetMapping("/list")
    public Result<Page<DocumentVO>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) String fileName,
                                          @RequestParam(required = false) String fileType,
                                          @RequestParam(required = false) String status) {
        Page<DocumentVO> page = documentService.getDocumentList(pageNum, pageSize, fileName, fileType, status);
        return Result.success(page);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return Result.success();
    }
}
