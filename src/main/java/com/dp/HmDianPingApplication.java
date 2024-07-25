package com.dp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import springfox.documentation.oas.annotations.EnableOpenApi;


@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.dp.mapper")
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableOpenApi
@ServletComponentScan(basePackages = {"com.xx.filter.xss"})
public class HmDianPingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmDianPingApplication.class, args);
    }

}
