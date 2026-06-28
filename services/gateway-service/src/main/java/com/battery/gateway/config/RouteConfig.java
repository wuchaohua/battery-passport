 package com.battery.gateway.config;

 import org.springframework.cloud.gateway.route.RouteLocator;
 import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;

 @Configuration
 public class RouteConfig {

     @Bean
     public RouteLocator customRoutes(RouteLocatorBuilder builder) {
         return builder.routes()
             .route("auth-service", r -> r
                 .path("/api/v1/auth/**")
                 .filters(f -> f.stripPrefix(1))
                 .uri("lb://auth-service"))
             .route("battery-service", r -> r
                 .path("/api/v1/passports/**")
                 .filters(f -> f.stripPrefix(1))
                 .uri("lb://battery-service"))
             .route("tenant-service", r -> r
                 .path("/api/v1/tenants/**")
                 .filters(f -> f.stripPrefix(1))
                 .uri("lb://tenant-service"))
             .route("report-service", r -> r
                 .path("/api/v1/reports/**")
                 .filters(f -> f.stripPrefix(1))
                 .uri("lb://report-service"))
             .route("user-service", r -> r
                 .path("/api/v1/users/**")
                 .filters(f -> f.stripPrefix(1))
                 .uri("lb://user-service"))
             .route("integration-service", r -> r
                 .path("/api/v1/integration/**")
                 .filters(f -> f.stripPrefix(1))
                 .uri("lb://integration-service"))
             .build();
     }
 }
