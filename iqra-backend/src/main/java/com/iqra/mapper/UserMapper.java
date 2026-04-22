package com.iqra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iqra.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
