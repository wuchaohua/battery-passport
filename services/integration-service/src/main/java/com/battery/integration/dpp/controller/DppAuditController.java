package com.battery.integration.dpp.controller;

import com.battery.common.Result;
import com.battery.integration.dpp.audit.AuditService;
import com.battery.integration.dpp.audit.AuditService.ChainVerification;
import com.battery.integration.dpp.model.AuditEvidence;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST controller for audit evidence chain query and verification.
 */
@RestController
@RequestMapping("/api/v1/dpp/audit")
public class DppAuditController {

    private final AuditService auditService;

    public DppAuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Get the full audit chain for an enterprise.
     */
    @GetMapping("/chain/{enterpriseId}")
    public Result<List<AuditEvidence>> getAuditChain(@PathVariable String enterpriseId) {
        List<AuditEvidence> chain = auditService.getChain(enterpriseId);
        return Result.ok(chain);
    }

    /**
     * Verify the integrity of the audit chain for an enterprise.
     */
    @GetMapping("/chain/{enterpriseId}/verify")
    public Result<ChainVerification> verifyAuditChain(@PathVariable String enterpriseId) {
        ChainVerification verification = auditService.verifyChain(enterpriseId);
        return Result.ok(verification);
    }

    /**
     * List all enterprises with audit chains.
     */
    @GetMapping("/enterprises")
    public Result<Set<String>> getTrackedEnterprises() {
        return Result.ok(auditService.getTrackedEnterprises());
    }
}
