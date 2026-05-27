package com.example.permmenu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 菜单权限管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.example.permmenu.mapper")
public class PermMenuApplication {

    public static void main(String[] args) {
        SpringApplication.run(PermMenuApplication.class, args);
    }
}
