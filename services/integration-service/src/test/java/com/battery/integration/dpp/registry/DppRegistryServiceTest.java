package com.battery.integration.dpp.registry;

import com.battery.integration.dpp.audit.AuditService;
import com.battery.integration.dpp.config.DppRegistryProperties;
import com.battery.integration.dpp.model.DppEnterprise;
import com.battery.integration.dpp.model.DppEnterprise.RegistrationStatus;
import com.battery.integration.dpp.model.DppRegistration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DppRegistryServiceTest {

    private DppRegistryService registryService;
    private DppRegistryClient mockClient;
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        DppRegistryProperties props = new DppRegistryProperties();
        auditService = new AuditService(props);
        mockClient = new DppRegistryClient(props, null) {
            @Override
            public com.fasterxml.jackson.databind.JsonNode registerEnterprise(
                    String enterpriseId, String legalName, String vatNumber,
                    String eoriNumber, String countryCode) throws Exception {
                com.fasterxml.jackson.databind.node.ObjectNode result =
                        new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
                result.put("registryId", "REG-EU-DEV-" + enterpriseId);
                result.put("status", "pending");
                return result;
            }

            @Override
            public com.fasterxml.jackson.databind.JsonNode bindCertificate(
                    String dppRegistryId, String certificatePem) throws Exception {
                com.fasterxml.jackson.databind.node.ObjectNode result =
                        new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
                result.put("status", "bound");
                return result;
            }

            @Override
            public com.fasterxml.jackson.databind.JsonNode revokeRegistration(
                    String dppRegistryId, String reason) throws Exception {
                com.fasterxml.jackson.databind.node.ObjectNode result =
                        new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
                result.put("status", "revoked");
                return result;
            }
        };
        registryService = new DppRegistryService(props, mockClient, auditService);
    }

    @Test
    void testPreRegisterEnterprise() {
        DppEnterprise enterprise = new DppEnterprise("ENT-001", "Trina Solar",
                "CN123456", "EORI-CN-001", "CN");

        DppRegistration reg = registryService.preRegisterEnterprise(enterprise, "admin");

        assertNotNull(reg.getRegistrationId());
        assertEquals("ENT-001", reg.getEnterpriseId());
        assertEquals("Trina Solar", reg.getEnterpriseLegalName());
        assertEquals(RegistrationStatus.PENDING, reg.getStatus());
        assertNull(reg.getDppRegistryId());
    }

    @Test
    void testSubmitToRegistry() {
        DppEnterprise enterprise = new DppEnterprise("ENT-002", "CATL",
                "CN987654", "EORI-CN-002", "CN");
        DppRegistration reg = registryService.preRegisterEnterprise(enterprise, "admin");

        assertNull(reg.getDppRegistryId());
        DppRegistration submitted = registryService.submitToRegistry(reg.getRegistrationId(), "admin");

        assertEquals("REG-EU-DEV-ENT-002", submitted.getDppRegistryId());
        assertEquals(RegistrationStatus.PENDING, submitted.getStatus());
    }

    @Test
    void testBindCertificate() {
        DppEnterprise enterprise = new DppEnterprise("ENT-003", "BYD",
                "CN555555", "EORI-CN-003", "CN");
        DppRegistration reg = registryService.preRegisterEnterprise(enterprise, "admin");

        DppRegistration bound = registryService.bindCertificate(
                reg.getRegistrationId(), "CERT-001", "cert-pem-content",
                "user@byd.com", "https://iam.byd.com", "admin");

        assertEquals("CERT-001", bound.getBoundCertId());
        assertEquals("user@byd.com", bound.getBoundOidcSubject());
        assertEquals("https://iam.byd.com", bound.getOidcIssuer());
        assertEquals(RegistrationStatus.BOUND, bound.getStatus());
    }

    @Test
    void testConfirmRegistration() {
        DppEnterprise enterprise = new DppEnterprise("ENT-004", "Samsung SDI",
                "KR123456", "EORI-KR-001", "KR");
        DppRegistration reg = registryService.preRegisterEnterprise(enterprise, "admin");
        registryService.submitToRegistry(reg.getRegistrationId(), "admin");
        DppRegistration confirmed = registryService.confirmRegistration(
                reg.getRegistrationId(), "admin");

        assertEquals(RegistrationStatus.VERIFIED, confirmed.getStatus());
    }

    @Test
    void testRevokeRegistration() {
        DppEnterprise enterprise = new DppEnterprise("ENT-005", "Panasonic",
                "JP123456", "EORI-JP-001", "JP");
        DppRegistration reg = registryService.preRegisterEnterprise(enterprise, "admin");
        registryService.submitToRegistry(reg.getRegistrationId(), "admin");

        DppRegistration revoked = registryService.revokeRegistration(
                reg.getRegistrationId(), "Business discontinued", "admin");

        assertEquals(RegistrationStatus.REVOKED, revoked.getStatus());
        assertEquals("Business discontinued", revoked.getFailureReason());
    }

    @Test
    void testGetRegistration() {
        DppEnterprise enterprise = new DppEnterprise("ENT-006", "LG Energy",
                "KR987654", "EORI-KR-002", "KR");
        DppRegistration reg = registryService.preRegisterEnterprise(enterprise, "admin");

        DppRegistration found = registryService.getRegistration(reg.getRegistrationId());
        assertNotNull(found);
        assertEquals(reg.getRegistrationId(), found.getRegistrationId());

        assertNull(registryService.getRegistration("NONEXISTENT"));
    }

    @Test
    void testGetRegistrationByEnterpriseId() {
        DppEnterprise e1 = new DppEnterprise("ENT-007", "Toshiba", "JP333", "EORI-JP-003", "JP");
        DppEnterprise e2 = new DppEnterprise("ENT-008", "Hitachi", "JP444", "EORI-JP-004", "JP");
        registryService.preRegisterEnterprise(e1, "admin");
        registryService.preRegisterEnterprise(e2, "admin");

        assertNotNull(registryService.getRegistrationByEnterpriseId("ENT-007"));
        assertNotNull(registryService.getRegistrationByEnterpriseId("ENT-008"));
        assertNull(registryService.getRegistrationByEnterpriseId("NONEXISTENT"));
    }

    @Test
    void testGetAllRegistrations() {
        assertEquals(0, registryService.getAllRegistrations().size());

        for (int i = 1; i <= 3; i++) {
            DppEnterprise e = new DppEnterprise("ENT-0" + i, "Company " + i,
                    "VAT" + i, "EORI" + i, "DE");
            registryService.preRegisterEnterprise(e, "admin");
        }

        assertEquals(3, registryService.getAllRegistrations().size());
    }

    @Test
    void testSubmitNonexistentRegistration() {
        assertThrows(DppRegistryException.class, () ->
            registryService.submitToRegistry("NONEXISTENT", "admin"));
    }

    @Test
    void testBindCertificateNonexistentRegistration() {
        assertThrows(DppRegistryException.class, () ->
            registryService.bindCertificate("NONEXISTENT", "CERT-X", "pem", "sub", "iss", "admin"));
    }
}
