package com.battery.integration.dpp.controller;

import com.battery.common.Result;
import com.battery.integration.dpp.identity.OidcPkiBindingService;
import com.battery.integration.dpp.identity.OidcPkiBindingService.BindingResult;
import com.battery.integration.dpp.model.DppRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for OIDC + PKI enterprise identity binding.
 *
 * Implements the enterprise identity binding flow:
 * 1. Enterprise operator authenticates via OIDC
 * 2. System generates/binds PKI certificate
 * 3. Binding is submitted to DPP Registry
 * 4. Evidence chain is recorded
 */
@RestController
@RequestMapping("/api/v1/dpp/identity")
public class DppIdentityBindingController {

    private static final Logger log = LoggerFactory.getLogger(DppIdentityBindingController.class);

    private final OidcPkiBindingService bindingService;

    public DppIdentityBindingController(OidcPkiBindingService bindingService) {
        this.bindingService = bindingService;
    }

    /**
     * Execute the full OIDC + PKI identity binding flow.
     *
     * Body parameters:
     * - enterpriseId: Internal enterprise identifier
     * - legalName: Enterprise legal name
     * - countryCode: ISO country code (e.g., CN, DE, FR)
     * - oidcSubject: The 'sub' claim from the OIDC ID token
     * - oidcIssuer: The 'iss' claim from the OIDC ID token
     */
    @PostMapping("/bind")
    public Result<BindingResult> bindIdentity(@RequestBody Map<String, String> request) {
        try {
            String enterpriseId = request.get("enterpriseId");
            String legalName = request.get("legalName");
            String countryCode = request.get("countryCode");
            String oidcSubject = request.get("oidcSubject");
            String oidcIssuer = request.get("oidcIssuer");
            String operatorId = resolveOperatorId();

            log.info("Identity bind request for enterprise {} (OIDC: {} | {})",
                    enterpriseId, oidcIssuer, oidcSubject);

            BindingResult result = bindingService.bindIdentity(
                    enterpriseId, legalName, countryCode,
                    oidcSubject, oidcIssuer, operatorId);

            return Result.ok(result);
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("Identity binding failed", e);
            return Result.fail("Identity binding failed: " + e.getMessage());
        }
    }

    /**
     * Verify that an OIDC identity is bound to an enterprise.
     */
    @PostMapping("/verify")
    public Result<Map<String, Object>> verifyBinding(@RequestBody Map<String, String> request) {
        String enterpriseId = request.get("enterpriseId");
        String oidcSubject = request.get("oidcSubject");
        String oidcIssuer = request.get("oidcIssuer");

        boolean valid = bindingService.verifyBinding(enterpriseId, oidcSubject, oidcIssuer);

        return Result.ok(Map.of(
                "enterpriseId", enterpriseId,
                "bindingVerified", valid
        ));
    }

    private String resolveOperatorId() {
        try {
            return com.battery.common.TenantContext.getUserId();
        } catch (Exception e) {
            return "system";
        }
    }
}
