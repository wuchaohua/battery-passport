package com.battery.integration.dpp.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DppRegistryPropertiesTest {

    @Test
    void testDefaultValues() {
        DppRegistryProperties props = new DppRegistryProperties();
        assertEquals("https://dpp-registry.ec.europa.eu/api/v1", props.getBaseUrl());
        assertEquals("https://dpp-registry.ec.europa.eu/auth/realms/dpp", props.getOidcIssuerUrl());
        assertEquals("battery-passport-platform", props.getOidcClientId());
        assertTrue(props.isMtlsEnabled());
        assertEquals(10000, props.getConnectTimeoutMs());
        assertEquals(30000, props.getReadTimeoutMs());
        assertEquals(10000, props.getMaxAuditEntriesPerEnterprise());
        assertEquals("/etc/battery/dpp/keystores", props.getKeyStoreDir());
        assertEquals("changeit", props.getKeyStorePassword());
    }

    @Test
    void testCustomValues() {
        DppRegistryProperties props = new DppRegistryProperties();
        props.setBaseUrl("https://dpp-registry.test.eu/api/v2");
        props.setOidcIssuerUrl("https://dpp-registry.test.eu/auth/realms/test");
        props.setOidcClientId("test-client");
        props.setOidcClientSecret("secret123");
        props.setMtlsEnabled(false);
        props.setConnectTimeoutMs(5000);
        props.setReadTimeoutMs(15000);
        props.setMaxAuditEntriesPerEnterprise(5000);
        props.setTrustStorePath("/etc/battery/test/truststore.p12");
        props.setKeyStoreDir("/etc/battery/test/keystores");
        props.setKeyStorePassword("testpass");

        assertEquals("https://dpp-registry.test.eu/api/v2", props.getBaseUrl());
        assertEquals("https://dpp-registry.test.eu/auth/realms/test", props.getOidcIssuerUrl());
        assertEquals("test-client", props.getOidcClientId());
        assertEquals("secret123", props.getOidcClientSecret());
        assertFalse(props.isMtlsEnabled());
        assertEquals(5000, props.getConnectTimeoutMs());
        assertEquals(15000, props.getReadTimeoutMs());
        assertEquals(5000, props.getMaxAuditEntriesPerEnterprise());
        assertEquals("/etc/battery/test/truststore.p12", props.getTrustStorePath());
        assertEquals("/etc/battery/test/keystores", props.getKeyStoreDir());
        assertEquals("testpass", props.getKeyStorePassword());
    }

    @Test
    void testEmptyOptionalValues() {
        DppRegistryProperties props = new DppRegistryProperties();
        assertEquals("", props.getOidcClientSecret());
        assertEquals("", props.getTrustStorePath());
        assertEquals("", props.getTrustStorePassword());
    }
}
