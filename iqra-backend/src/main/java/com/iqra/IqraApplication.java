package com.iqra;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
@MapperScan("com.iqra.mapper")
public class IqraApplication {

    public static void main(String[] args) {
        SpringApplication.run(IqraApplication.class, args);
    }
}
