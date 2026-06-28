 package com.battery.security;

 import java.util.Map;

 /**
  * SSO 适配器接口 —— 各企业基于此接口实现自身认证协议。
  * 平台通过 Spring @ConditionalOnProperty 按 enterprise.code 注入。
  */
 public interface SsoAdapter {

     /** 支持的协议类型: CAS, OIDC, SAML2, LDAP */
     String protocol();

     /** 构建企业专属登录URL */
     String buildLoginUrl(String redirectUri);

     /** 回调处理，返回平台用户信息（userId, name, enterpriseCode） */
     PlatformUser handleCallback(String authCode, String state);

     /** Token 验证 */
     PlatformUser validateToken(String token);

     class PlatformUser {
         private String userId;
         private String username;
         private String displayName;
         private String email;
         private String enterpriseCode;
         private Map<String, Object> attributes;

         public String getUserId() { return userId; }
         public void setUserId(String userId) { this.userId = userId; }
         public String getUsername() { return username; }
         public void setUsername(String username) { this.username = username; }
         public String getDisplayName() { return displayName; }
         public void setDisplayName(String displayName) { this.displayName = displayName; }
         public String getEmail() { return email; }
         public void setEmail(String email) { this.email = email; }
         public String getEnterpriseCode() { return enterpriseCode; }
         public void setEnterpriseCode(String enterpriseCode) { this.enterpriseCode = enterpriseCode; }
         public Map<String, Object> getAttributes() { return attributes; }
         public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
     }
 }
