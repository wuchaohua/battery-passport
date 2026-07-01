package com.battery.integration.dpp.model;

import com.battery.integration.dpp.model.DppEnterprise.RegistrationStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DppEnterpriseTest {

    @Test
    void testDefaultConstructor() {
        DppEnterprise e = new DppEnterprise();
        assertNull(e.getEnterpriseId());
        assertNull(e.getStatus());
    }

    @Test
    void testParameterizedConstructor() {
        DppEnterprise e = new DppEnterprise("ENT-001", "Trina Solar Co., Ltd",
                "CN123456789", "EORI123456", "CN");
        assertEquals("ENT-001", e.getEnterpriseId());
        assertEquals("Trina Solar Co., Ltd", e.getLegalName());
        assertEquals("CN123456789", e.getVatNumber());
        assertEquals("EORI123456", e.getEoriNumber());
        assertEquals("CN", e.getCountryCode());
        assertEquals(RegistrationStatus.PENDING, e.getStatus());
    }

    @Test
    void testFullLifecycle() {
        DppEnterprise e = new DppEnterprise();
        e.setEnterpriseId("ENT-002");
        e.setLegalName("CATL");
        e.setVatNumber("CN987654321");
        e.setEoriNumber("EORI987654");
        e.setCountryCode("CN");
        e.setStatus(RegistrationStatus.BOUND);
        e.setDppRegistryId("REG-EU-001");
        e.setOidcIssuer("https://iam.catl.com");
        e.setCertificateThumbprint("a1b2c3d4e5f6...");
        e.setRegisteredAt(LocalDateTime.of(2026, 6, 1, 10, 0));
        e.setUpdatedAt(LocalDateTime.of(2026, 6, 15, 14, 30));

        assertEquals("ENT-002", e.getEnterpriseId());
        assertEquals("CN987654321", e.getVatNumber());
        assertEquals("REG-EU-001", e.getDppRegistryId());
        assertEquals("https://iam.catl.com", e.getOidcIssuer());
        assertEquals("a1b2c3d4e5f6...", e.getCertificateThumbprint());
        assertEquals(RegistrationStatus.BOUND, e.getStatus());
        assertEquals(LocalDateTime.of(2026, 6, 1, 10, 0), e.getRegisteredAt());
        assertEquals(LocalDateTime.of(2026, 6, 15, 14, 30), e.getUpdatedAt());
    }

    @Test
    void testAllRegistrationStatuses() {
        assertNotNull(RegistrationStatus.valueOf("PENDING"));
        assertNotNull(RegistrationStatus.valueOf("BOUND"));
        assertNotNull(RegistrationStatus.valueOf("VERIFIED"));
        assertNotNull(RegistrationStatus.valueOf("REVOKED"));
        assertNotNull(RegistrationStatus.valueOf("FAILED"));
    }

    @Test
    void testDefaultStatusIsNull() {
        DppEnterprise e = new DppEnterprise();
        assertNull(e.getStatus());
    }

    @Test
    void testLeiCodeOptional() {
        DppEnterprise e = new DppEnterprise("ENT-003", "Test GmbH",
                "DE123456", "EORI-DE-001", "DE");
        assertNull(e.getLeiCode());
        e.setLeiCode("LEI-9876543210ABCDEF12");
        assertEquals("LEI-9876543210ABCDEF12", e.getLeiCode());
    }

    @Test
    void testRegisteredAddressOptional() {
        DppEnterprise e = new DppEnterprise("ENT-004", "Test SAS",
                "FR123456", "EORI-FR-001", "FR");
        assertNull(e.getRegisteredAddress());
        e.setRegisteredAddress("123 Avenue des Champs-Élysées, Paris");
        assertEquals("123 Avenue des Champs-Élysées, Paris", e.getRegisteredAddress());
    }
}
