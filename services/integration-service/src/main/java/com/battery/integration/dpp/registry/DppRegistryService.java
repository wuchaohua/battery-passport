package com.battery.integration.dpp.registry;

import com.battery.integration.dpp.audit.AuditService;
import com.battery.integration.dpp.config.DppRegistryProperties;
import com.battery.integration.dpp.model.AuditEvidence.EventType;
import com.battery.integration.dpp.model.DppEnterprise;
import com.battery.integration.dpp.model.DppEnterprise.RegistrationStatus;
import com.battery.integration.dpp.model.DppRegistration;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Business logic for EU DPP Registry integration.
 * Manages the pre-registration lifecycle: enterprise registration, certificate binding,
 * and status tracking.
 */
public class DppRegistryService {

    private static final Logger log = LoggerFactory.getLogger(DppRegistryService.class);

    private final DppRegistryProperties properties;
    private final DppRegistryClient registryClient;
    private final AuditService auditService;
    private final Map<String, DppRegistration> registrationStore = new ConcurrentHashMap<>();

    public DppRegistryService(DppRegistryProperties properties,
                              DppRegistryClient registryClient,
                              AuditService auditService) {
        this.properties = properties;
        this.registryClient = registryClient;
        this.auditService = auditService;
    }

    /**
     * Pre-register an enterprise with the DPP Registry.
     * Step 1: Enterprise submits legal identity, gets a pending registration.
     */
    public DppRegistration preRegisterEnterprise(DppEnterprise enterprise, String operatorId) {
        String registrationId = UUID.randomUUID().toString();
        String payloadDigest = digest(enterprise.getEnterpriseId() + enterprise.getVatNumber()
                + enterprise.getEoriNumber());

        DppRegistration registration = new DppRegistration();
        registration.setRegistrationId(registrationId);
        registration.setEnterpriseId(enterprise.getEnterpriseId());
        registration.setEnterpriseLegalName(enterprise.getLegalName());
        registration.setVatNumber(enterprise.getVatNumber());
        registration.setEoriNumber(enterprise.getEoriNumber());
        registration.setStatus(RegistrationStatus.PENDING);
        registration.setCreatedAt(LocalDateTime.now());
        registration.setUpdatedAt(LocalDateTime.now());

        registrationStore.put(registrationId, registration);

        auditService.record(enterprise.getEnterpriseId(), EventType.ENTERPRISE_REGISTERED,
                operatorId, "Enterprise pre-registered: " + enterprise.getLegalName(),
                payloadDigest);

        log.info("Pre-registered enterprise {} with registration {}",
                enterprise.getEnterpriseId(), registrationId);
        return registration;
    }

    /**
     * Submit the registration to the EU DPP Registry API.
     * Step 2: Send pre-registration data to the official Registry.
     */
    public DppRegistration submitToRegistry(String registrationId, String operatorId) {
        DppRegistration registration = registrationStore.get(registrationId);
        if (registration == null) {
            throw new DppRegistryException("Registration not found: " + registrationId);
        }
        if (registration.getStatus() != RegistrationStatus.PENDING
                && registration.getStatus() != RegistrationStatus.BOUND) {
            throw new DppRegistryException("Invalid registration status: " + registration.getStatus());
        }

        try {
            JsonNode response = registryClient.registerEnterprise(
                    registration.getEnterpriseId(),
                    registration.getEnterpriseLegalName(),
                    registration.getVatNumber(),
                    registration.getEoriNumber(),
                    "CN" // default, context-driven in production
            );

            String registryId = response.has("registryId")
                    ? response.get("registryId").asText() : registration.getEnterpriseId();
            registration.setDppRegistryId(registryId);

            // The Registry returns a pending status, awaiting certificate binding
            registration.setStatus(RegistrationStatus.PENDING);
            registration.setUpdatedAt(LocalDateTime.now());

            auditService.record(registration.getEnterpriseId(), EventType.REGISTRATION_SUBMITTED,
                    operatorId, "Submitted to DPP Registry, got ID: " + registryId,
                    digest(registryId));

            log.info("Submitted registration {} to DPP Registry, got registryId: {}",
                    registrationId, registryId);
        } catch (Exception e) {
            registration.setStatus(RegistrationStatus.FAILED);
            registration.setFailureReason(e.getMessage());
            registration.setUpdatedAt(LocalDateTime.now());
            log.error("Failed to submit registration {} to DPP Registry", registrationId, e);
        }

        return registration;
    }

    /**
     * Bind a PKI certificate to the registration.
     * Step 3: Associate the enterprise's certificate with the DPP Registry registration.
     */
    public DppRegistration bindCertificate(String registrationId,
                                            String certId,
                                            String certPem,
                                            String oidcSubject,
                                            String oidcIssuer,
                                            String operatorId) {
        DppRegistration registration = registrationStore.get(registrationId);
        if (registration == null) {
            throw new DppRegistryException("Registration not found: " + registrationId);
        }

        registration.setBoundCertId(certId);
        registration.setBoundOidcSubject(oidcSubject);
        registration.setOidcIssuer(oidcIssuer);
        registration.setStatus(RegistrationStatus.BOUND);
        registration.setUpdatedAt(LocalDateTime.now());

        // Notify the DPP Registry of the certificate binding
        if (registration.getDppRegistryId() != null) {
            try {
                registryClient.bindCertificate(registration.getDppRegistryId(), certPem);
            } catch (Exception e) {
                log.warn("Failed to notify DPP Registry of certificate binding: {}", e.getMessage());
            }
        }

        String evidenceData = registrationId + certId + oidcSubject;
        auditService.record(registration.getEnterpriseId(), EventType.CERTIFICATE_BOUND,
                operatorId, "Certificate " + certId + " bound to OIDC subject " + oidcSubject,
                digest(evidenceData));

        log.info("Bound certificate {} to registration {} (OIDC subject: {})",
                certId, registrationId, oidcSubject);
        return registration;
    }

    /**
     * Confirm registration from the DPP Registry (async callback).
     */
    public DppRegistration confirmRegistration(String registrationId, String operatorId) {
        DppRegistration registration = registrationStore.get(registrationId);
        if (registration == null) {
            throw new DppRegistryException("Registration not found: " + registrationId);
        }
        registration.setStatus(RegistrationStatus.VERIFIED);
        registration.setUpdatedAt(LocalDateTime.now());

        auditService.record(registration.getEnterpriseId(), EventType.REGISTRATION_CONFIRMED,
                operatorId, "Registration confirmed by DPP Registry", digest(registrationId));

        log.info("Registration {} confirmed by DPP Registry", registrationId);
        return registration;
    }

    /**
     * Revoke a registration.
     */
    public DppRegistration revokeRegistration(String registrationId, String reason, String operatorId) {
        DppRegistration registration = registrationStore.get(registrationId);
        if (registration == null) {
            throw new DppRegistryException("Registration not found: " + registrationId);
        }

        registration.setStatus(RegistrationStatus.REVOKED);
        registration.setFailureReason(reason);
        registration.setUpdatedAt(LocalDateTime.now());

        if (registration.getDppRegistryId() != null) {
            try {
                registryClient.revokeRegistration(registration.getDppRegistryId(), reason);
            } catch (Exception e) {
                log.warn("Failed to notify DPP Registry of revocation: {}", e.getMessage());
            }
        }

        auditService.record(registration.getEnterpriseId(), EventType.REGISTRATION_REVOKED,
                operatorId, "Registration revoked: " + reason, digest(reason));

        log.info("Registration {} revoked: {}", registrationId, reason);
        return registration;
    }

    public DppRegistration getRegistration(String registrationId) {
        return registrationStore.get(registrationId);
    }

    public List<DppRegistration> getAllRegistrations() {
        return new ArrayList<>(registrationStore.values());
    }

    public DppRegistration getRegistrationByEnterpriseId(String enterpriseId) {
        return registrationStore.values().stream()
                .filter(r -> enterpriseId.equals(r.getEnterpriseId()))
                .findFirst()
                .orElse(null);
    }

    private String digest(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute digest", e);
        }
    }
}
