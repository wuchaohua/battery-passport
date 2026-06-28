 package com.battery.security;

 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.security.config.annotation.web.builders.HttpSecurity;
 import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
 import org.springframework.security.web.SecurityFilterChain;
 import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
 import org.springframework.web.cors.CorsConfiguration;
 import org.springframework.web.cors.CorsConfigurationSource;
 import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

 import java.util.List;

 @Configuration
 @EnableWebSecurity
 public class SecurityConfig {

     private final JwtTokenProvider jwtTokenProvider;

     public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
         this.jwtTokenProvider = jwtTokenProvider;
     }

     @Bean
     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
         http
             .cors(cors -> cors.configurationSource(corsSource()))
             .csrf(csrf -> csrf.disable())
             .authorizeHttpRequests(auth -> auth
                 .requestMatchers("/api/v1/auth/**", "/actuator/**").permitAll()
                 .anyRequest().authenticated()
             )
             .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                     UsernamePasswordAuthenticationFilter.class);
         return http.build();
     }

     @Bean
     public CorsConfigurationSource corsSource() {
         CorsConfiguration config = new CorsConfiguration();
         config.setAllowedOrigins(List.of("*"));
         config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
         config.setAllowedHeaders(List.of("*"));
         config.setExposedHeaders(List.of("X-Tenant-Id", "Authorization"));
         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
         source.registerCorsConfiguration("/**", config);
         return source;
     }
 }
