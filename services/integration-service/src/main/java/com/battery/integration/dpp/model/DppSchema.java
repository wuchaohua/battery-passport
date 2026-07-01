package com.battery.integration.dpp.model;

import java.time.LocalDateTime;

/**
 * Represents a DPP metadata JSON Schema used to validate metadata entries.
 * Managed via the /schema/v1 endpoints.
 */
public class DppSchema {

    private String schemaId;
    private String version;
    private String schemaJson;       // The actual JSON Schema content
    private String description;
    private String status;           // "CURRENT", "PREVIOUS", "DEPRECATED"
    private String previousSchemaId; // Link to previous schema for rollback
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DppSchema() {}

    public String getSchemaId() { return schemaId; }
    public void setSchemaId(String schemaId) { this.schemaId = schemaId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getSchemaJson() { return schemaJson; }
    public void setSchemaJson(String schemaJson) { this.schemaJson = schemaJson; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPreviousSchemaId() { return previousSchemaId; }
    public void setPreviousSchemaId(String previousSchemaId) { this.previousSchemaId = previousSchemaId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
