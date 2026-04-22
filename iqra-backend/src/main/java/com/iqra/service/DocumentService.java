package com.iqra.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.iqra.model.entity.Document;
import com.iqra.model.vo.DocumentVO;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService extends IService<Document> {

    Document uploadDocument(MultipartFile file, String department, String tags, String uploadBy);

    Page<DocumentVO> getDocumentList(Integer pageNum, Integer pageSize, String fileName, String fileType, String status);

    boolean deleteDocument(Long id);

    void processDocument(Long documentId);
}
