 package com.battery.integration;

 import org.springframework.boot.SpringApplication;
 import org.springframework.boot.autoconfigure.SpringBootApplication;
 import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

 @SpringBootApplication(scanBasePackages = {"com.battery.integration", "com.battery.security"})
 @EnableDiscoveryClient
 public class IntegrationApplication {
     public static void main(String[] args) { SpringApplication.run(IntegrationApplication.class, args); }
 }
