package com.yiu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplicationBuilder(App.class).build();
//        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
        System.out.println("the server is running...");
        while (true) {
            //skip
        }
    }
}
