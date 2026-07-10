package com.creditbank.mvp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动入口。
 * 运行 main 方法后，SpringBoot 会：
 * 1. 启动内嵌 Tomcat（默认 8080 端口）
 * 2. 扫描并装配所有 @RestController / @Service / @Configuration
 * 3. 扫描 mapper 包下的 Mapper 接口（@MapperScan）
 */
@SpringBootApplication
@MapperScan("com.creditbank.mvp.mapper")
public class CreditBankMvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditBankMvpApplication.class, args);
    }
}
