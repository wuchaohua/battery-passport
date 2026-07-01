package com.battery.integration.dpp.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DPP metadata entry for the EU DPP Registry.
 * Corresponds to the metadata/v1 endpoints.
 */
public class DppMetadataEntry {

    private String entryId;
    private String registryId;
    private String dppType;          // "BATTERY", "TEXTILE", etc.
    private String dppVersion;
    private String issuerLei;        // Legal Entity Identifier of issuer
    private String issuerVatNumber;
    private String productCategory;
    private String productName;
    private String brandName;
    private String modelNumber;
    private String batchNumber;
    private List<String> autocompleteBy;  // Fields used for autocomplete search
    private Map<String, Object> customAttributes;
    private String schemaVersion;
    private String status;           // "DRAFT", "PUBLISHED", "REVOKED"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DppMetadataEntry() {}

    public String getEntryId() { return entryId; }
    public void setEntryId(String entryId) { this.entryId = entryId; }
    public String getRegistryId() { return registryId; }
    public void setRegistryId(String registryId) { this.registryId = registryId; }
    public String getDppType() { return dppType; }
    public void setDppType(String dppType) { this.dppType = dppType; }
    public String getDppVersion() { return dppVersion; }
    public void setDppVersion(String dppVersion) { this.dppVersion = dppVersion; }
    public String getIssuerLei() { return issuerLei; }
    public void setIssuerLei(String issuerLei) { this.issuerLei = issuerLei; }
    public String getIssuerVatNumber() { return issuerVatNumber; }
    public void setIssuerVatNumber(String issuerVatNumber) { this.issuerVatNumber = issuerVatNumber; }
    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getModelNumber() { return modelNumber; }
    public void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public List<String> getAutocompleteBy() { return autocompleteBy; }
    public void setAutocompleteBy(List<String> autocompleteBy) { this.autocompleteBy = autocompleteBy; }
    public Map<String, Object> getCustomAttributes() { return customAttributes; }
    public void setCustomAttributes(Map<String, Object> customAttributes) { this.customAttributes = customAttributes; }
    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
