package com.battery.integration.dpp.model;

import com.battery.integration.dpp.model.DppEnterprise.RegistrationStatus;
import java.time.LocalDateTime;

/**
 * Registration record tracking the lifecycle of an enterprise's DPP Registry registration.
 * Captures the binding between OIDC identity, PKI certificate, and Registry account.
 */
public class DppRegistration {

    private String registrationId;
    private String enterpriseId;
    private String enterpriseLegalName;
    private String vatNumber;
    private String eoriNumber;
    private String dppRegistryId;
    private RegistrationStatus status;
    private String boundCertId;
    private String boundOidcSubject;
    private String oidcIssuer;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DppRegistration() {}

    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }
    public String getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(String enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getEnterpriseLegalName() { return enterpriseLegalName; }
    public void setEnterpriseLegalName(String enterpriseLegalName) { this.enterpriseLegalName = enterpriseLegalName; }
    public String getVatNumber() { return vatNumber; }
    public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }
    public String getEoriNumber() { return eoriNumber; }
    public void setEoriNumber(String eoriNumber) { this.eoriNumber = eoriNumber; }
    public String getDppRegistryId() { return dppRegistryId; }
    public void setDppRegistryId(String dppRegistryId) { this.dppRegistryId = dppRegistryId; }
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
    public String getBoundCertId() { return boundCertId; }
    public void setBoundCertId(String boundCertId) { this.boundCertId = boundCertId; }
    public String getBoundOidcSubject() { return boundOidcSubject; }
    public void setBoundOidcSubject(String boundOidcSubject) { this.boundOidcSubject = boundOidcSubject; }
    public String getOidcIssuer() { return oidcIssuer; }
    public void setOidcIssuer(String oidcIssuer) { this.oidcIssuer = oidcIssuer; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
