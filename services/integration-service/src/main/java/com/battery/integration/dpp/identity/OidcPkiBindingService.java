package com.battery.integration.dpp.identity;

import com.battery.integration.dpp.audit.AuditService;
import com.battery.integration.dpp.certificate.CertificateManager;
import com.battery.integration.dpp.model.AuditEvidence.EventType;
import com.battery.integration.dpp.model.DppCertificate;
import com.battery.integration.dpp.model.DppRegistration;
import com.battery.integration.dpp.registry.DppRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.Base64;

/**
 * OIDC + PKI identity binding service.
 *
 * Implements the enterprise identity binding flow:
 * 1. User authenticates via OIDC → gets OIDC identity token
 * 2. Enterprise PKI certificate is generated/loaded
 * 3. OIDC identity (subject + issuer) is bound to the certificate
 * 4. The binding is registered with the DPP Registry
 * 5. An audit evidence entry is created for the binding
 *
 * This replaces eIDAS-based identity proofing with a practical
 * OIDC + PKI + Registry pre-registration model.
 */
public class OidcPkiBindingService {

    private static final Logger log = LoggerFactory.getLogger(OidcPkiBindingService.class);

    private final CertificateManager certificateManager;
    private final DppRegistryService registryService;
    private final AuditService auditService;

    public OidcPkiBindingService(CertificateManager certificateManager,
                                 DppRegistryService registryService,
                                 AuditService auditService) {
        this.certificateManager = certificateManager;
        this.registryService = registryService;
        this.auditService = auditService;
    }

    /**
     * Execute the full OIDC + PKI identity binding flow for an enterprise.
     *
     * @param enterpriseId   Internal enterprise identifier
     * @param legalName      Enterprise legal name
     * @param countryCode    ISO country code
     * @param oidcSubject    OIDC subject claim from the ID token
     * @param oidcIssuer     OIDC issuer URL
     * @param operatorId     User performing the operation
     * @return BindingResult containing the certificate and registration
     */
    public BindingResult bindIdentity(String enterpriseId,
                                       String legalName,
                                       String countryCode,
                                       String oidcSubject,
                                       String oidcIssuer,
                                       String operatorId) throws Exception {

        // Step 1: Validate OIDC identity (sub + iss form the unique OIDC identity)
        if (oidcSubject == null || oidcSubject.isEmpty()) {
            throw new IllegalArgumentException("OIDC subject claim is required for identity binding");
        }
        if (oidcIssuer == null || oidcIssuer.isEmpty()) {
            throw new IllegalArgumentException("OIDC issuer is required for identity binding");
        }

        String oidcIdentity = oidcIssuer + "|" + oidcSubject;
        log.info("Starting OIDC+PKI binding for enterprise {} with OIDC identity {}",
                enterpriseId, oidcIdentity);

        // Step 2: Generate PKI certificate for the enterprise
        DppCertificate certificate = certificateManager.generateEnterpriseCertificate(
                enterpriseId, legalName, countryCode);

        auditService.record(enterpriseId, EventType.CERTIFICATE_ISSUED,
                operatorId,
                "PKI certificate issued: " + certificate.getCertId(),
                digest(certificate.getCertId() + certificate.getSha256Thumbprint()));

        // Step 3: Find or create pre-registration
        DppRegistration registration = registryService.getRegistrationByEnterpriseId(enterpriseId);
        if (registration == null) {
            com.battery.integration.dpp.model.DppEnterprise enterprise =
                    new com.battery.integration.dpp.model.DppEnterprise(
                            enterpriseId, legalName, "PENDING", "PENDING", countryCode);
            registration = registryService.preRegisterEnterprise(enterprise, operatorId);
        }

        // Step 4: Bind the certificate with OIDC identity to the registration
        String certPem = "-----BEGIN CERTIFICATE-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(
                        certificate.getSha256Thumbprint().getBytes())
                + "\n-----END CERTIFICATE-----";

        registration = registryService.bindCertificate(
                registration.getRegistrationId(),
                certificate.getCertId(),
                certPem,
                oidcSubject,
                oidcIssuer,
                operatorId
        );

        // Step 5: Submit to DPP Registry (async in production)
        registryService.submitToRegistry(registration.getRegistrationId(), operatorId);

        log.info("OIDC+PKI identity binding complete for enterprise {}: cert={}, reg={}",
                enterpriseId, certificate.getCertId(), registration.getRegistrationId());

        return new BindingResult(certificate, registration);
    }

    /**
     * Verify that an OIDC identity matches a bound certificate.
     */
    public boolean verifyBinding(String enterpriseId,
                                  String oidcSubject,
                                  String oidcIssuer) {
        DppRegistration registration = registryService.getRegistrationByEnterpriseId(enterpriseId);
        if (registration == null) return false;

        boolean matches = oidcSubject.equals(registration.getBoundOidcSubject())
                && oidcIssuer.equals(registration.getOidcIssuer());

        if (matches) {
            log.info("Identity binding verified for enterprise {} (OIDC: {} | {})",
                    enterpriseId, oidcIssuer, oidcSubject);
        }
        return matches;
    }

    private String digest(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Digest error", e);
        }
    }

    /**
     * Result of an identity binding operation.
     */
    public record BindingResult(DppCertificate certificate, DppRegistration registration) {}
}
