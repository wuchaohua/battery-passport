package com.battery.integration.dpp.model;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;

/**
 * Enterprise PKI X.509 certificate bound to an OIDC identity.
 * Used for mTLS authentication with the EU DPP Registry.
 */
public class DppCertificate {

    private String certId;
    private String enterpriseId;
    private String subjectDn;
    private String issuerDn;
    private String serialNumber;
    private String sha256Thumbprint;
    private CertStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private String revocationReason;
    private transient X509Certificate x509Certificate;

    public enum CertStatus {
        ACTIVE,
        EXPIRED,
        REVOKED
    }

    public DppCertificate() {}

    public String getCertId() { return certId; }
    public void setCertId(String certId) { this.certId = certId; }
    public String getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(String enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getSubjectDn() { return subjectDn; }
    public void setSubjectDn(String subjectDn) { this.subjectDn = subjectDn; }
    public String getIssuerDn() { return issuerDn; }
    public void setIssuerDn(String issuerDn) { this.issuerDn = issuerDn; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getSha256Thumbprint() { return sha256Thumbprint; }
    public void setSha256Thumbprint(String sha256Thumbprint) { this.sha256Thumbprint = sha256Thumbprint; }
    public CertStatus getStatus() { return status; }
    public void setStatus(CertStatus status) { this.status = status; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    public String getRevocationReason() { return revocationReason; }
    public void setRevocationReason(String revocationReason) { this.revocationReason = revocationReason; }
    public X509Certificate getX509Certificate() { return x509Certificate; }
    public void setX509Certificate(X509Certificate x509Certificate) { this.x509Certificate = x509Certificate; }
}
