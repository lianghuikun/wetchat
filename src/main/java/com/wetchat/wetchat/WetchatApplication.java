package com.wetchat.wetchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude
        = {DataSourceAutoConfiguration.class})
@EnableAspectJAutoProxy
@EnableCaching
public class WetchatApplication {
    public static void main(String[] args) {
        SpringApplication.run(WetchatApplication.class, args);
    }

}

