package com.battery.integration.dpp.schema;

import com.battery.integration.dpp.model.DppSchema;
import com.battery.integration.dpp.registry.DppRegistryClient;
import com.battery.integration.dpp.registry.DppRegistryException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing DPP metadata JSON Schemas in the EU DPP Registry.
 * Wraps /schema/v1 endpoints with local schema state tracking.
 */
@Service
public class DppSchemaService {

    private static final Logger log = LoggerFactory.getLogger(DppSchemaService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final DppRegistryClient registryClient;
    private DppSchema currentSchema;
    private DppSchema previousSchema;

    public DppSchemaService(DppRegistryClient registryClient) {
        this.registryClient = registryClient;
    }

    /**
     * Create or update the DPP metadata JSON Schema (POST /schema/v1).
     */
    public DppSchema createOrUpdateSchema(DppSchema schema) {
        try {
            ObjectNode body = MAPPER.createObjectNode();
            body.put("schemaId", schema.getSchemaId() != null ? schema.getSchemaId() : UUID.randomUUID().toString());
            body.put("version", schema.getVersion() != null ? schema.getVersion() : "1.0");
            body.put("schemaJson", schema.getSchemaJson());
            if (schema.getDescription() != null) body.put("description", schema.getDescription());

            JsonNode response = registryClient.createOrUpdateSchema(body);

            // Track schema versions locally for rollback support
            if (currentSchema != null) {
                previousSchema = currentSchema;
            }
            currentSchema = MAPPER.treeToValue(response, DppSchema.class);
            currentSchema.setUpdatedAt(LocalDateTime.now());

            log.info("Schema {} created/updated (version: {})", currentSchema.getSchemaId(), currentSchema.getVersion());
            return currentSchema;
        } catch (Exception e) {
            throw new DppRegistryException("Failed to create/update schema", e);
        }
    }

    /**
     * Get the current DPP metadata JSON Schema (GET /schema/v1/current).
     */
    public DppSchema getCurrentSchema() {
        if (currentSchema != null) {
            return currentSchema; // Return cached version
        }
        try {
            JsonNode response = registryClient.getCurrentSchema();
            currentSchema = MAPPER.treeToValue(response, DppSchema.class);
            return currentSchema;
        } catch (Exception e) {
            throw new DppRegistryException("Failed to get current schema", e);
        }
    }

    /**
     * Delete the current schema and revert to the previous one (DELETE /schema/v1/current).
     */
    public DppSchema deleteCurrentSchema() {
        try {
            registryClient.deleteCurrentSchema();
            DppSchema reverted = previousSchema;
            currentSchema = previousSchema;
            previousSchema = null;
            log.info("Schema deleted, reverted to previous: {}", reverted != null ? reverted.getSchemaId() : "none");
            return reverted;
        } catch (Exception e) {
            throw new DppRegistryException("Failed to delete schema", e);
        }
    }

    public DppSchema getPreviousSchema() { return previousSchema; }
}
