package com.battery.integration.dpp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DppSchemaTest {

    @Test
    void testDefaultConstructor() {
        DppSchema s = new DppSchema();
        assertNull(s.getSchemaId());
    }

    @Test
    void testFullSchema() {
        DppSchema s = new DppSchema();
        s.setSchemaId("SCHEMA-EU-001");
        s.setVersion("2.0");
        s.setSchemaJson("{\"type\":\"object\",\"properties\":{}}");
        s.setDescription("DPP Battery Metadata Schema v2");
        s.setStatus("CURRENT");
        s.setPreviousSchemaId("SCHEMA-EU-000");

        assertEquals("SCHEMA-EU-001", s.getSchemaId());
        assertEquals("2.0", s.getVersion());
        assertTrue(s.getSchemaJson().contains("type"));
        assertEquals("CURRENT", s.getStatus());
        assertEquals("SCHEMA-EU-000", s.getPreviousSchemaId());
    }

    @Test
    void testSchemaStatuses() {
        DppSchema s = new DppSchema();
        s.setStatus("CURRENT");
        s.setStatus("PREVIOUS");
        assertEquals("PREVIOUS", s.getStatus());
        s.setStatus("DEPRECATED");
        assertEquals("DEPRECATED", s.getStatus());
    }
}
