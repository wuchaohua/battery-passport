 package com.battery.auth;

 import org.springframework.boot.SpringApplication;
 import org.springframework.boot.autoconfigure.SpringBootApplication;
 import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

 @SpringBootApplication(scanBasePackages = {"com.battery.auth", "com.battery.security", "com.battery.mybatis"})
 @EnableDiscoveryClient
 public class AuthApplication {
     public static void main(String[] args) {
         SpringApplication.run(AuthApplication.class, args);
     }
 }
