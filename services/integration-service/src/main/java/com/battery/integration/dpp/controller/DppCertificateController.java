package com.battery.integration.dpp.controller;

import com.battery.common.Result;
import com.battery.integration.dpp.certificate.CertificateManager;
import com.battery.integration.dpp.model.DppCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for enterprise PKI certificate management.
 */
@RestController
@RequestMapping("/api/v1/dpp/certificates")
public class DppCertificateController {

    private static final Logger log = LoggerFactory.getLogger(DppCertificateController.class);

    private final CertificateManager certificateManager;

    public DppCertificateController(CertificateManager certificateManager) {
        this.certificateManager = certificateManager;
    }

    /**
     * Generate a new PKI certificate for an enterprise.
     */
    @PostMapping("/generate")
    public Result<DppCertificate> generateCertificate(@RequestParam String enterpriseId,
                                                       @RequestParam String legalName,
                                                       @RequestParam String countryCode) {
        try {
            DppCertificate cert = certificateManager.generateEnterpriseCertificate(
                    enterpriseId, legalName, countryCode);
            log.info("Generated certificate {} for enterprise {}", cert.getCertId(), enterpriseId);
            return Result.ok(cert);
        } catch (Exception e) {
            log.error("Failed to generate certificate for enterprise {}", enterpriseId, e);
            return Result.fail("Certificate generation failed: " + e.getMessage());
        }
    }

    /**
     * Get certificate details.
     */
    @GetMapping("/{certId}")
    public Result<DppCertificate> getCertificate(@PathVariable String certId) {
        DppCertificate cert = certificateManager.getCertificate(certId);
        if (cert == null) {
            return Result.fail(404, "Certificate not found: " + certId);
        }
        return Result.ok(cert);
    }

    /**
     * Get all certificates for an enterprise.
     */
    @GetMapping("/enterprise/{enterpriseId}")
    public Result<List<DppCertificate>> getCertificatesForEnterprise(@PathVariable String enterpriseId) {
        return Result.ok(certificateManager.getCertificatesForEnterprise(enterpriseId));
    }

    /**
     * Revoke a certificate.
     */
    @PostMapping("/{certId}/revoke")
    public Result<Void> revokeCertificate(@PathVariable String certId,
                                          @RequestParam String reason) {
        certificateManager.revokeCertificate(certId, reason);
        log.info("Revoked certificate {}: {}", certId, reason);
        return Result.ok();
    }
}
