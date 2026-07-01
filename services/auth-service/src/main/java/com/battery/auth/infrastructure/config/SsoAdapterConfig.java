package com.battery.auth.infrastructure.config;

import com.battery.auth.infrastructure.adapter.TrinaCasSsoAdapter;
import com.battery.auth.infrastructure.adapter.TrinaOidcSsoAdapter;
import com.battery.security.SsoAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class SsoAdapterConfig {

    @Bean
    @ConditionalOnProperty(name = "battery.sso.provider", havingValue = "trina")
    public SsoAdapter trinaSsoAdapter(TrinaCasSsoAdapter casAdapter, TrinaOidcSsoAdapter oidcAdapter) {
        return oidcAdapter;
    }

    @Bean
    @ConditionalOnProperty(name = "battery.sso.provider", havingValue = "tuobang")
    public SsoAdapter tuobangSsoAdapter() {
        return new TuobangSsoAdapter();
    }

    @Bean
    @Profile("saas")
    public SsoAdapter saasSsoAdapter() {
        return new SaasBuiltinAuthAdapter();
    }

    static class TuobangSsoAdapter implements SsoAdapter {
        @Override
        public String protocol() { return "OIDC"; }
        @Override
        public String buildLoginUrl(String redirectUri) {
            return "https://sso.tuobang.com/auth?client_id=battery-passport&redirect_uri=" + (redirectUri != null ? redirectUri : "");
        }
        @Override
        public PlatformUser handleCallback(String authCode, String state) {
            PlatformUser user = new PlatformUser();
            user.setUserId("tb-" + authCode.substring(0, 8));
            user.setUsername("tuobang_user");
            user.setDisplayName("托邦用户");
            user.setEnterpriseCode("tuobang");
            return user;
        }
        @Override
        public PlatformUser validateToken(String token) { return null; }
    }

    static class SaasBuiltinAuthAdapter implements SsoAdapter {
        @Override
        public String protocol() { return "BUILTIN_JWT"; }
        @Override
        public String buildLoginUrl(String redirectUri) {
            return "/api/v1/auth/saas/login";
        }
        @Override
        public PlatformUser handleCallback(String authCode, String state) {
            PlatformUser user = new PlatformUser();
            user.setUserId("saas-" + System.currentTimeMillis());
            user.setUsername("saas_user");
            user.setDisplayName("SaaS用户");
            user.setEnterpriseCode("saas");
            user.getAttributes().put("tenantId", state);
            return user;
        }
        @Override
        public PlatformUser validateToken(String token) { return null; }
    }
}