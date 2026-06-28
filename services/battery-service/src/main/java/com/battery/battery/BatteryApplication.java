 package com.battery.battery;

 import org.springframework.boot.SpringApplication;
 import org.springframework.boot.autoconfigure.SpringBootApplication;
 import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

 @SpringBootApplication(scanBasePackages = {"com.battery.battery", "com.battery.security", "com.battery.mybatis"})
 @EnableDiscoveryClient
 public class BatteryApplication {
     public static void main(String[] args) {
         SpringApplication.run(BatteryApplication.class, args);
     }
 }
