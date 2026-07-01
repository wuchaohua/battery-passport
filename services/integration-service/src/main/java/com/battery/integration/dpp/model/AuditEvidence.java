package com.battery.integration.dpp.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable audit evidence chain entry.
 * Each entry records an identity-binding operation with a cryptographic
 * link to the previous entry, forming an evidence chain.
 */
public class AuditEvidence {

    private String evidenceId;
    private String enterpriseId;
    private EventType eventType;
    private String operatorId;
    private String description;
    private String payloadDigest;         // SHA-256 digest of the operation payload
    private String previousDigest;        // SHA-256 of previous evidence entry (chain link)
    private String signatureValue;        // Signed digest (operator private key)
    private String signingCertThumbprint;
    private LocalDateTime occurredAt;

    public enum EventType {
        ENTERPRISE_REGISTERED,
        ENTERPRISE_UPDATED,
        CERTIFICATE_ISSUED,
        CERTIFICATE_BOUND,
        CERTIFICATE_REVOKED,
        REGISTRATION_SUBMITTED,
        REGISTRATION_CONFIRMED,
        REGISTRATION_REJECTED,
        REGISTRATION_REVOKED,
        AUDIT_CHAIN_VERIFIED
    }

    public AuditEvidence() {}

    public AuditEvidence(String evidenceId, String enterpriseId, EventType eventType,
                         String operatorId, String description, String payloadDigest,
                         String previousDigest) {
        this.evidenceId = evidenceId;
        this.enterpriseId = enterpriseId;
        this.eventType = eventType;
        this.operatorId = operatorId;
        this.description = description;
        this.payloadDigest = payloadDigest;
        this.previousDigest = previousDigest;
        this.occurredAt = LocalDateTime.now();
    }

    public String getEvidenceId() { return evidenceId; }
    public void setEvidenceId(String evidenceId) { this.evidenceId = evidenceId; }
    public String getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(String enterpriseId) { this.enterpriseId = enterpriseId; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPayloadDigest() { return payloadDigest; }
    public void setPayloadDigest(String payloadDigest) { this.payloadDigest = payloadDigest; }
    public String getPreviousDigest() { return previousDigest; }
    public void setPreviousDigest(String previousDigest) { this.previousDigest = previousDigest; }
    public String getSignatureValue() { return signatureValue; }
    public void setSignatureValue(String signatureValue) { this.signatureValue = signatureValue; }
    public String getSigningCertThumbprint() { return signingCertThumbprint; }
    public void setSigningCertThumbprint(String signingCertThumbprint) { this.signingCertThumbprint = signingCertThumbprint; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
