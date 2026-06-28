 package com.battery.security;

 import com.battery.common.TenantContext;
 import io.jsonwebtoken.Claims;
 import jakarta.servlet.FilterChain;
 import jakarta.servlet.ServletException;
 import jakarta.servlet.http.HttpServletRequest;
 import jakarta.servlet.http.HttpServletResponse;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.web.filter.OncePerRequestFilter;

 import java.io.IOException;
 import java.util.Collections;

 public class JwtAuthenticationFilter extends OncePerRequestFilter {

     private final JwtTokenProvider jwtTokenProvider;

     public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
         this.jwtTokenProvider = jwtTokenProvider;
     }

     @Override
     protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
         String header = request.getHeader("Authorization");
         if (header != null && header.startsWith("Bearer ")) {
             String token = header.substring(7);
             try {
                 Claims claims = jwtTokenProvider.parseToken(token);
                 String userId = claims.getSubject();
                 String enterpriseCode = claims.get("enterpriseCode", String.class);
                 String tenantId = claims.get("tenantId", String.class);

                 TenantContext.setUserId(userId);
                 TenantContext.setEnterpriseCode(enterpriseCode);
                 TenantContext.setTenantId(tenantId);

                 UsernamePasswordAuthenticationToken auth =
                         new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                 SecurityContextHolder.getContext().setAuthentication(auth);
             } catch (Exception ignored) {
                 // 无效token，不设置认证上下文
             }
         }

         // 从请求头传递租户信息（SaaS模式）
        if (TenantContext.getTenantId() == null) {
            String tenantHeader = request.getHeader("X-Tenant-Id");
            if (tenantHeader != null) TenantContext.setTenantId(tenantHeader);
        }

         try {
             chain.doFilter(request, response);
         } finally {
             TenantContext.clear();
         }
     }
 }
