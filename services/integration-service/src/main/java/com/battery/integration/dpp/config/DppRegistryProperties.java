package com.battery.integration.dpp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for EU DPP Registry integration.
 */
@ConfigurationProperties(prefix = "battery.dpp.registry")
public class DppRegistryProperties {

    /** Base URL of the EU DPP Registry API. */
    private String baseUrl = "https://dpp-registry.ec.europa.eu/api/v1";

    /** OIDC issuer URL for the DPP Registry's identity provider. */
    private String oidcIssuerUrl = "https://dpp-registry.ec.europa.eu/auth/realms/dpp";

    /** OIDC client ID assigned to this platform. */
    private String oidcClientId = "battery-passport-platform";

    /** OIDC client secret. */
    private String oidcClientSecret = "";

    /** PKI trust store path for validating DPP Registry server certificates. */
    private String trustStorePath = "";

    /** PKI trust store password. */
    private String trustStorePassword = "";

    /** Directory for enterprise key stores. */
    private String keyStoreDir = "/etc/battery/dpp/keystores";

    /** Default key store password. */
    private String keyStorePassword = "changeit";

    /** Whether mTLS is enabled for DPP Registry communication. */
    private boolean mtlsEnabled = true;

    /** Connection timeout in milliseconds. */
    private int connectTimeoutMs = 10000;

    /** Read timeout in milliseconds. */
    private int readTimeoutMs = 30000;

    /** Maximum audit evidence entries to retain per enterprise. */
    private int maxAuditEntriesPerEnterprise = 10000;

    // Getters and setters

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getOidcIssuerUrl() { return oidcIssuerUrl; }
    public void setOidcIssuerUrl(String oidcIssuerUrl) { this.oidcIssuerUrl = oidcIssuerUrl; }
    public String getOidcClientId() { return oidcClientId; }
    public void setOidcClientId(String oidcClientId) { this.oidcClientId = oidcClientId; }
    public String getOidcClientSecret() { return oidcClientSecret; }
    public void setOidcClientSecret(String oidcClientSecret) { this.oidcClientSecret = oidcClientSecret; }
    public String getTrustStorePath() { return trustStorePath; }
    public void setTrustStorePath(String trustStorePath) { this.trustStorePath = trustStorePath; }
    public String getTrustStorePassword() { return trustStorePassword; }
    public void setTrustStorePassword(String trustStorePassword) { this.trustStorePassword = trustStorePassword; }
    public String getKeyStoreDir() { return keyStoreDir; }
    public void setKeyStoreDir(String keyStoreDir) { this.keyStoreDir = keyStoreDir; }
    public String getKeyStorePassword() { return keyStorePassword; }
    public void setKeyStorePassword(String keyStorePassword) { this.keyStorePassword = keyStorePassword; }
    public boolean isMtlsEnabled() { return mtlsEnabled; }
    public void setMtlsEnabled(boolean mtlsEnabled) { this.mtlsEnabled = mtlsEnabled; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    public int getMaxAuditEntriesPerEnterprise() { return maxAuditEntriesPerEnterprise; }
    public void setMaxAuditEntriesPerEnterprise(int maxAuditEntriesPerEnterprise) { this.maxAuditEntriesPerEnterprise = maxAuditEntriesPerEnterprise; }
}
