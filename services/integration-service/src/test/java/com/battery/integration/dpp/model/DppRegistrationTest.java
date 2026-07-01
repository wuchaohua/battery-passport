package com.battery.integration.dpp.model;

import com.battery.integration.dpp.model.DppEnterprise.RegistrationStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DppRegistrationTest {

    @Test
    void testDefaultConstructor() {
        DppRegistration r = new DppRegistration();
        assertNull(r.getRegistrationId());
    }

    @Test
    void testFullRegistration() {
        DppRegistration r = new DppRegistration();
        r.setRegistrationId("REG-EU-001");
        r.setEnterpriseId("ENT-001");
        r.setEnterpriseLegalName("Trina Solar Co., Ltd");
        r.setVatNumber("CN123456789");
        r.setEoriNumber("EORI-CN-001");
        r.setDppRegistryId("DPP-REG-98765");
        r.setStatus(RegistrationStatus.VERIFIED);
        r.setBoundCertId("CERT-EU-001");
        r.setBoundOidcSubject("user@trinasolar.com");
        r.setOidcIssuer("https://iam.trinasolar.com");
        r.setCreatedAt(LocalDateTime.of(2026, 6, 1, 10, 0));
        r.setUpdatedAt(LocalDateTime.of(2026, 6, 15, 14, 30));

        assertEquals("REG-EU-001", r.getRegistrationId());
        assertEquals("ENT-001", r.getEnterpriseId());
        assertEquals("DPP-REG-98765", r.getDppRegistryId());
        assertEquals(RegistrationStatus.VERIFIED, r.getStatus());
        assertEquals("CERT-EU-001", r.getBoundCertId());
        assertEquals("user@trinasolar.com", r.getBoundOidcSubject());
        assertEquals("https://iam.trinasolar.com", r.getOidcIssuer());
    }

    @Test
    void testRegistrationFailure() {
        DppRegistration r = new DppRegistration();
        r.setRegistrationId("REG-EU-002");
        r.setStatus(RegistrationStatus.FAILED);
        r.setFailureReason("DPP Registry rejected: invalid VAT number");

        assertEquals(RegistrationStatus.FAILED, r.getStatus());
        assertTrue(r.getFailureReason().contains("invalid VAT number"));
    }

    @Test
    void testPendingRegistrationDefaults() {
        DppRegistration r = new DppRegistration();
        r.setRegistrationId("REG-EU-003");
        r.setEnterpriseId("ENT-003");
        r.setStatus(RegistrationStatus.PENDING);

        assertEquals(RegistrationStatus.PENDING, r.getStatus());
        assertNull(r.getBoundCertId());
        assertNull(r.getBoundOidcSubject());
        assertNull(r.getDppRegistryId());
    }

    @Test
    void testRegistrationUpdate() {
        DppRegistration r = new DppRegistration();
        r.setCreatedAt(LocalDateTime.of(2026, 6, 1, 10, 0));
        r.setUpdatedAt(LocalDateTime.of(2026, 6, 1, 10, 0));

        r.setStatus(RegistrationStatus.BOUND);
        r.setUpdatedAt(LocalDateTime.of(2026, 6, 2, 14, 0));

        assertNotEquals(r.getCreatedAt(), r.getUpdatedAt());
        assertTrue(r.getUpdatedAt().isAfter(r.getCreatedAt()));
    }
}
