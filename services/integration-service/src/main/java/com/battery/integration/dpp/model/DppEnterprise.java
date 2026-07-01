package com.battery.integration.dpp.model;

import java.time.LocalDateTime;

/**
 * EU DPP Registry enterprise registration data.
 * Each enterprise must be pre-registered before submitting passports to the Registry.
 */
public class DppEnterprise {

    private String enterpriseId;
    private String legalName;
    private String vatNumber;
    private String eoriNumber;
    private String registeredAddress;
    private String countryCode;
    private String leiCode;
    private String dppRegistryId;
    private RegistrationStatus status;
    private String oidcIssuer;
    private String certificateThumbprint;
    private LocalDateTime registeredAt;
    private LocalDateTime updatedAt;

    public enum RegistrationStatus {
        PENDING,
        BOUND,
        VERIFIED,
        REVOKED,
        FAILED
    }

    public DppEnterprise() {}

    public DppEnterprise(String enterpriseId, String legalName, String vatNumber,
                         String eoriNumber, String countryCode) {
        this.enterpriseId = enterpriseId;
        this.legalName = legalName;
        this.vatNumber = vatNumber;
        this.eoriNumber = eoriNumber;
        this.countryCode = countryCode;
        this.status = RegistrationStatus.PENDING;
    }

    public String getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(String enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getVatNumber() { return vatNumber; }
    public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }
    public String getEoriNumber() { return eoriNumber; }
    public void setEoriNumber(String eoriNumber) { this.eoriNumber = eoriNumber; }
    public String getRegisteredAddress() { return registeredAddress; }
    public void setRegisteredAddress(String registeredAddress) { this.registeredAddress = registeredAddress; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getLeiCode() { return leiCode; }
    public void setLeiCode(String leiCode) { this.leiCode = leiCode; }
    public String getDppRegistryId() { return dppRegistryId; }
    public void setDppRegistryId(String dppRegistryId) { this.dppRegistryId = dppRegistryId; }
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
    public String getOidcIssuer() { return oidcIssuer; }
    public void setOidcIssuer(String oidcIssuer) { this.oidcIssuer = oidcIssuer; }
    public String getCertificateThumbprint() { return certificateThumbprint; }
    public void setCertificateThumbprint(String thumbprint) { this.certificateThumbprint = thumbprint; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
