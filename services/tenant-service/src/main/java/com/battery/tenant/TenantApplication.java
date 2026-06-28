 package com.battery.tenant;

 import org.springframework.boot.SpringApplication;
 import org.springframework.boot.autoconfigure.SpringBootApplication;
 import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

 @SpringBootApplication(scanBasePackages = {"com.battery.tenant", "com.battery.security", "com.battery.mybatis"})
 @EnableDiscoveryClient
 public class TenantApplication {
     public static void main(String[] args) { SpringApplication.run(TenantApplication.class, args); }
 }
