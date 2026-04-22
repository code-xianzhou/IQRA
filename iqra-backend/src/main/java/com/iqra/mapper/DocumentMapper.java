package com.iqra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iqra.model.entity.Document;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
}
