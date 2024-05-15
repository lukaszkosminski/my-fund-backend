package com.myfund;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MyFundApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyFundApplication.class, args);
    }
}
