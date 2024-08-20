package com.dp.voucher;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dp.voucher.mapper")
public class VoucherApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoucherApplication.class, args);
    }
}
