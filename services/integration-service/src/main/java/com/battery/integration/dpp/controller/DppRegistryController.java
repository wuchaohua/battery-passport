package com.battery.integration.dpp.controller;

import com.battery.common.Result;
import com.battery.integration.dpp.model.DppEnterprise;
import com.battery.integration.dpp.model.DppRegistration;
import com.battery.integration.dpp.registry.DppRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for EU DPP Registry registration management.
 * Exposes endpoints for enterprise pre-registration, certificate binding,
 * and registration lifecycle management.
 */
@RestController
@RequestMapping("/api/v1/dpp/registry")
public class DppRegistryController {

    private static final Logger log = LoggerFactory.getLogger(DppRegistryController.class);

    private final DppRegistryService registryService;

    public DppRegistryController(DppRegistryService registryService) {
        this.registryService = registryService;
    }

    /**
     * Pre-register an enterprise for DPP Registry access.
     */
    @PostMapping("/enterprises")
    public Result<DppRegistration> preRegisterEnterprise(@RequestBody DppEnterprise enterprise) {
        log.info("Pre-register enterprise: {}", enterprise.getEnterpriseId());
        DppRegistration registration = registryService.preRegisterEnterprise(
                enterprise,
                resolveOperatorId());
        return Result.ok(registration);
    }

    /**
     * Submit a pre-registration to the EU DPP Registry.
     */
    @PostMapping("/registrations/{registrationId}/submit")
    public Result<DppRegistration> submitToRegistry(@PathVariable String registrationId) {
        log.info("Submit registration {} to DPP Registry", registrationId);
        DppRegistration registration = registryService.submitToRegistry(
                registrationId, resolveOperatorId());
        return Result.ok(registration);
    }

    /**
     * Bind a certificate to a registration with OIDC identity.
     */
    @PostMapping("/registrations/{registrationId}/bind")
    public Result<DppRegistration> bindCertificate(
            @PathVariable String registrationId,
            @RequestParam String certId,
            @RequestParam String oidcSubject,
            @RequestParam String oidcIssuer) {
        log.info("Bind certificate {} to registration {} (OIDC: {} | {})",
                certId, registrationId, oidcIssuer, oidcSubject);

        // In production, cert PEM would be retrieved from CertificateManager
        String certPem = "placeholder";

        DppRegistration registration = registryService.bindCertificate(
                registrationId, certId, certPem, oidcSubject, oidcIssuer, resolveOperatorId());
        return Result.ok(registration);
    }

    /**
     * Get registration details.
     */
    @GetMapping("/registrations/{registrationId}")
    public Result<DppRegistration> getRegistration(@PathVariable String registrationId) {
        DppRegistration registration = registryService.getRegistration(registrationId);
        if (registration == null) {
            return Result.fail(404, "Registration not found: " + registrationId);
        }
        return Result.ok(registration);
    }

    /**
     * Get registration by enterprise ID.
     */
    @GetMapping("/enterprises/{enterpriseId}/registration")
    public Result<DppRegistration> getRegistrationByEnterprise(@PathVariable String enterpriseId) {
        DppRegistration registration = registryService.getRegistrationByEnterpriseId(enterpriseId);
        if (registration == null) {
            return Result.fail(404, "No registration found for enterprise: " + enterpriseId);
        }
        return Result.ok(registration);
    }

    /**
     * List all registrations.
     */
    @GetMapping("/registrations")
    public Result<List<DppRegistration>> listRegistrations() {
        return Result.ok(registryService.getAllRegistrations());
    }

    /**
     * Confirm a registration (called by DPP Registry callback or admin).
     */
    @PostMapping("/registrations/{registrationId}/confirm")
    public Result<DppRegistration> confirmRegistration(@PathVariable String registrationId) {
        DppRegistration registration = registryService.confirmRegistration(
                registrationId, resolveOperatorId());
        return Result.ok(registration);
    }

    /**
     * Revoke a registration.
     */
    @PostMapping("/registrations/{registrationId}/revoke")
    public Result<DppRegistration> revokeRegistration(
            @PathVariable String registrationId,
            @RequestParam String reason) {
        DppRegistration registration = registryService.revokeRegistration(
                registrationId, reason, resolveOperatorId());
        return Result.ok(registration);
    }

    /**
     * Resolve operator ID from security context.
     * In production this would come from the authenticated JWT token.
     */
    private String resolveOperatorId() {
        try {
            return com.battery.common.TenantContext.getUserId();
        } catch (Exception e) {
            return "system";
        }
    }
}
