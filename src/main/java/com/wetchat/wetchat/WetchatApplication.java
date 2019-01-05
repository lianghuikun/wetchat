package com.wetchat.wetchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude
        = {DataSourceAutoConfiguration.class})
public class WetchatApplication {

    public static void main(String[] args) {
        SpringApplication.run(WetchatApplication.class, args);
    }

}

