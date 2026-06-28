 package com.battery.auth.application.service;

 import com.battery.common.TenantContext;
 import com.battery.security.JwtTokenProvider;
 import com.battery.security.SsoAdapter;
 import org.springframework.stereotype.Service;

 import java.util.HashMap;
 import java.util.Map;

 @Service
 public class AuthService {

     private final SsoAdapter ssoAdapter;
     private final JwtTokenProvider jwtTokenProvider;

     public AuthService(SsoAdapter ssoAdapter, JwtTokenProvider jwtTokenProvider) {
         this.ssoAdapter = ssoAdapter;
         this.jwtTokenProvider = jwtTokenProvider;
     }

     public String getLoginUrl(String redirectUri) {
         return ssoAdapter.buildLoginUrl(redirectUri);
     }

     public String handleCallback(String code, String state) {
         SsoAdapter.PlatformUser user = ssoAdapter.handleCallback(code, state);
         return jwtTokenProvider.createToken(user.getUserId(), user.getEnterpriseCode());
     }

     public Map<String, Object> validateToken(String token) {
         boolean valid = jwtTokenProvider.validateToken(token);
         Map<String, Object> result = new HashMap<>();
         result.put("valid", valid);
         if (valid) {
             var claims = jwtTokenProvider.parseToken(token);
             result.put("userId", claims.getSubject());
             result.put("enterpriseCode", claims.get("enterpriseCode"));
         }
         return result;
     }

     public void logout(String token) {
         // JWT 无状态，未来可引入令牌黑名单Redis方案
         TenantContext.clear();
     }
 }
