package com.iqra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iqra.model.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {
}
