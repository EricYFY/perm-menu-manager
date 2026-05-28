package com.example.permmenu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
@MapperScan("com.example.permmenu.mapper")
public class PermMenuApplication {

    public static void main(String[] args) {
        SpringApplication.run(PermMenuApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            String createTableSql = "CREATE TABLE IF NOT EXISTS perm_menu_edit_lock (" +
                    "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "LOCKED_BY VARCHAR(64), " +
                    "LOCKED_AT DATETIME, " +
                    "TEMP_TABLE_NAME VARCHAR(128), " +
                    "STATUS VARCHAR(16))";
            jdbcTemplate.execute(createTableSql);
        };
    }
}
