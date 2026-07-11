package com.creditbank.mvp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.creditbank.mvp.mapper")
@EnableScheduling
public class CreditBankMvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditBankMvpApplication.class, args);
    }
}
