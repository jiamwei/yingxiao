package com.wjm.yingxiao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wjm.yingxiao.mapper") // 扫描Mapper接口
public class YingxiaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(YingxiaoApplication.class, args);
    }

}
