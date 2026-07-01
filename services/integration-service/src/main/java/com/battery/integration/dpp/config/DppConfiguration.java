package com.battery.integration.dpp.config;

import com.battery.integration.dpp.certificate.CertificateManager;
import com.battery.integration.dpp.identity.OidcPkiBindingService;
import com.battery.integration.dpp.registry.DppRegistryClient;
import com.battery.integration.dpp.registry.DppRegistryService;
import com.battery.integration.dpp.audit.AuditService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyStore;

@Configuration
@EnableConfigurationProperties(DppRegistryProperties.class)
public class DppConfiguration {

    private final DppRegistryProperties properties;

    public DppConfiguration(DppRegistryProperties properties) {
        this.properties = properties;
    }

    @Bean
    public CertificateManager certificateManager() {
        return new CertificateManager(properties);
    }

    @Bean
    public AuditService auditService() {
        return new AuditService(properties);
    }

    @Bean
    public DppRegistryClient dppRegistryClient(CertificateManager certificateManager) {
        return new DppRegistryClient(properties, certificateManager);
    }

    @Bean
    public DppRegistryService dppRegistryService(DppRegistryClient registryClient,
                                                  AuditService auditService) {
        return new DppRegistryService(properties, registryClient, auditService);
    }

    @Bean
    public OidcPkiBindingService oidcPkiBindingService(CertificateManager certificateManager,
                                                         DppRegistryService registryService,
                                                         AuditService auditService) {
        return new OidcPkiBindingService(certificateManager, registryService, auditService);
    }
}
