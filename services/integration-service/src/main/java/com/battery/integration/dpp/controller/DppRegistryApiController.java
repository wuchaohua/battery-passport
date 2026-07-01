package com.battery.integration.dpp.controller;

import com.battery.common.Result;
import com.battery.integration.dpp.metadata.DppMetadataService;
import com.battery.integration.dpp.model.DppJwkSet;
import com.battery.integration.dpp.model.DppMetadataEntry;
import com.battery.integration.dpp.model.DppSchema;
import com.battery.integration.dpp.proof.DppProofService;
import com.battery.integration.dpp.schema.DppSchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for EU DPP Registry API v1 operations.
 * Provides access to the following DPP Registry endpoints:
 *   - JWKS public key retrieval
 *   - Metadata entry management (create/update)
 *   - Registration proof JWT retrieval
 *   - JSON Schema management (create/get/delete/rollback)
 */
@RestController
@RequestMapping("/api/v1/dpp/registry-api")
public class DppRegistryApiController {

    private static final Logger log = LoggerFactory.getLogger(DppRegistryApiController.class);

    private final DppMetadataService metadataService;
    private final DppProofService proofService;
    private final DppSchemaService schemaService;

    public DppRegistryApiController(DppMetadataService metadataService,
                                     DppProofService proofService,
                                     DppSchemaService schemaService) {
        this.metadataService = metadataService;
        this.proofService = proofService;
        this.schemaService = schemaService;
    }

    // ========================================================================
    // GET /.well-known/jwks.json — Fetch JWKS public key set
    // ========================================================================
    @GetMapping("/jwks")
    public Result<DppJwkSet> getJwks() {
        log.info("Fetching JWKS from EU DPP Registry");
        DppJwkSet jwkSet = metadataService.fetchJwks();
        return Result.ok(jwkSet);
    }

    // ========================================================================
    // POST /metadata/v1 — Create or update a DPP metadata entry
    // ========================================================================
    @PostMapping("/metadata")
    public Result<DppMetadataEntry> submitMetadata(@RequestBody DppMetadataEntry entry) {
        log.info("Submitting metadata entry: {}", entry.getProductName());
        DppMetadataEntry result = metadataService.submitMetadata(entry);
        return Result.ok(result);
    }

    // ========================================================================
    // POST /metadata/v1/registerDPP — Create or update DPP (alternate path)
    // ========================================================================
    @PostMapping("/metadata/register-dpp")
    public Result<DppMetadataEntry> registerDpp(@RequestBody DppMetadataEntry entry) {
        log.info("Registering DPP entry: {}", entry.getProductName());
        DppMetadataEntry result = metadataService.registerDpp(entry);
        return Result.ok(result);
    }

    // ========================================================================
    // GET /metadata/v1/{registryId}/proof — Get registration proof JWT
    // ========================================================================
    @GetMapping("/metadata/{registryId}/proof")
    public Result<Map<String, String>> getRegistrationProof(@PathVariable String registryId) {
        log.info("Fetching registration proof for registryId: {}", registryId);
        String jwt = metadataService.fetchRegistrationProof(registryId);

        // Optionally verify the JWT using cached JWKS
        DppJwkSet jwkSet = metadataService.getCachedJwks();
        boolean verified = false;
        if (jwkSet != null) {
            verified = proofService.verifyJwtSignature(jwt, jwkSet);
        }

        return Result.ok(Map.of(
                "jwt", jwt,
                "signatureVerified", String.valueOf(verified)
        ));
    }

    // ========================================================================
    // POST /schema/v1 — Create or update the DPP metadata JSON Schema
    // ========================================================================
    @PostMapping("/schema")
    public Result<DppSchema> createOrUpdateSchema(@RequestBody DppSchema schema) {
        log.info("Creating/updating schema: version={}", schema.getVersion());
        DppSchema result = schemaService.createOrUpdateSchema(schema);
        return Result.ok(result);
    }

    // ========================================================================
    // GET /schema/v1/current — Get the current DPP metadata JSON Schema
    // ========================================================================
    @GetMapping("/schema/current")
    public Result<DppSchema> getCurrentSchema() {
        log.info("Fetching current schema");
        DppSchema schema = schemaService.getCurrentSchema();
        return Result.ok(schema);
    }

    // ========================================================================
    // DELETE /schema/v1/current — Delete current schema, revert to previous
    // ========================================================================
    @DeleteMapping("/schema/current")
    public Result<DppSchema> deleteCurrentSchema() {
        log.info("Deleting current schema, reverting to previous");
        DppSchema reverted = schemaService.deleteCurrentSchema();
        return Result.ok(reverted);
    }
}
