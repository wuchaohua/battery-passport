package com.battery.integration.dpp.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class DppMetadataEntryTest {

    @Test
    void testDefaultConstructor() {
        DppMetadataEntry e = new DppMetadataEntry();
        assertNull(e.getEntryId());
    }

    @Test
    void testFullMetadataEntry() {
        DppMetadataEntry e = new DppMetadataEntry();
        e.setEntryId("META-001");
        e.setRegistryId("REG-EU-001");
        e.setDppType("BATTERY");
        e.setProductName("Li-ion Battery Pack 48V");
        e.setBrandName("TrinaPower");
        e.setModelNumber("TP-48-200");
        e.setBatchNumber("B2026-001");
        e.setIssuerLei("LEI-9876543210ABCDEF12");
        e.setIssuerVatNumber("CN123456789");
        e.setProductCategory("BATTERY_STORAGE");
        e.setSchemaVersion("1.0.0");
        e.setStatus("DRAFT");
        e.setAutocompleteBy(List.of("productName", "brandName", "modelNumber"));

        assertEquals("META-001", e.getEntryId());
        assertEquals("TrinaPower", e.getBrandName());
        assertEquals("BATTERY", e.getDppType());
        assertEquals(3, e.getAutocompleteBy().size());
        assertTrue(e.getAutocompleteBy().contains("productName"));
    }

    @Test
    void testCustomAttributes() {
        DppMetadataEntry e = new DppMetadataEntry();
        e.setCustomAttributes(Map.of("weight", "45kg", "voltage", "48V", "capacity", "200Ah"));
        assertEquals("45kg", e.getCustomAttributes().get("weight"));
        assertEquals("48V", e.getCustomAttributes().get("voltage"));
        assertEquals(3, e.getCustomAttributes().size());
    }

    @Test
    void testTimestamps() {
        DppMetadataEntry e = new DppMetadataEntry();
        e.setCreatedAt(LocalDateTime.of(2026, 6, 1, 10, 0));
        e.setUpdatedAt(LocalDateTime.of(2026, 6, 15, 14, 30));
        assertTrue(e.getUpdatedAt().isAfter(e.getCreatedAt()));
    }

    @Test
    void testStatusTransitions() {
        DppMetadataEntry e = new DppMetadataEntry();
        e.setStatus("DRAFT");
        e.setStatus("PUBLISHED");
        assertEquals("PUBLISHED", e.getStatus());
        e.setStatus("REVOKED");
        assertEquals("REVOKED", e.getStatus());
    }

    @Test
    void testOptionalFields() {
        DppMetadataEntry e = new DppMetadataEntry();
        assertNull(e.getSchemaVersion());
        assertNull(e.getBatchNumber());
        e.setBatchNumber("BATCH-001");
        assertEquals("BATCH-001", e.getBatchNumber());
    }
}
