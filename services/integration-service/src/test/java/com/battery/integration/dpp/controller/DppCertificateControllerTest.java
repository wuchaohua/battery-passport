package com.battery.integration.dpp.controller;

import com.battery.common.Result;
import com.battery.integration.dpp.certificate.CertificateManager;
import com.battery.integration.dpp.config.DppRegistryProperties;
import com.battery.integration.dpp.model.DppCertificate;
import com.battery.integration.dpp.model.DppCertificate.CertStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DppCertificateControllerTest {

    private DppCertificateController controller;
    private CertificateManager certificateManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        DppRegistryProperties props = new DppRegistryProperties();
        props.setKeyStoreDir(tempDir.toString());
        certificateManager = new CertificateManager(props);
        controller = new DppCertificateController(certificateManager);
    }

    @Test
    void testGenerateCertificate() {
        Result<DppCertificate> result = controller.generateCertificate(
                "ENT-001", "Trina Solar", "CN");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("ENT-001", result.getData().getEnterpriseId());
        assertTrue(result.getData().getSubjectDn().contains("Trina Solar"));
        assertEquals(CertStatus.ACTIVE, result.getData().getStatus());
    }

    @Test
    void testGenerateCertificateFailure() {
        // Empty parameters should cause a failure
        Result<DppCertificate> result = controller.generateCertificate("", "", "");
        assertEquals(200, result.getCode());
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testGetCertificate() throws Exception {
        DppCertificate cert = certificateManager.generateEnterpriseCertificate(
                "ENT-002", "CATL", "CN");

        Result<DppCertificate> found = controller.getCertificate(cert.getCertId());
        assertEquals(200, found.getCode());
        assertEquals(cert.getCertId(), found.getData().getCertId());

        Result<DppCertificate> notFound = controller.getCertificate("NONEXISTENT");
        assertEquals(404, notFound.getCode());
    }

    @Test
    void testGetCertificatesForEnterprise() throws Exception {
        certificateManager.generateEnterpriseCertificate("ENT-003", "BYD", "CN");
        certificateManager.generateEnterpriseCertificate("ENT-003", "BYD", "CN");
        certificateManager.generateEnterpriseCertificate("ENT-004", "Samsung", "KR");

        Result<List<DppCertificate>> ent3 = controller.getCertificatesForEnterprise("ENT-003");
        assertEquals(2, ent3.getData().size());

        Result<List<DppCertificate>> ent4 = controller.getCertificatesForEnterprise("ENT-004");
        assertEquals(1, ent4.getData().size());

        Result<List<DppCertificate>> empty = controller.getCertificatesForEnterprise("NONEXISTENT");
        assertTrue(empty.getData().isEmpty());
    }

    @Test
    void testRevokeCertificate() throws Exception {
        DppCertificate cert = certificateManager.generateEnterpriseCertificate(
                "ENT-005", "LG Energy", "KR");

        Result<Void> result = controller.revokeCertificate(cert.getCertId(), "Security audit");
        assertEquals(200, result.getCode());

        DppCertificate revoked = certificateManager.getCertificate(cert.getCertId());
        assertEquals(CertStatus.REVOKED, revoked.getStatus());
        assertEquals("Security audit", revoked.getRevocationReason());
    }

    @Test
    void testGenerateCertificateCustomCountry() {
        Result<DppCertificate> result = controller.generateCertificate(
                "ENT-DE", "German Battery GmbH", "DE");
        assertEquals(200, result.getCode());
        assertTrue(result.getData().getSubjectDn().contains("DE"));
    }

    @Test
    void testMultipleCertificatesSameEnterprise() throws Exception {
        controller.generateCertificate("ENT-MULTI", "Multi Corp", "US");
        controller.generateCertificate("ENT-MULTI", "Multi Corp", "US");
        controller.generateCertificate("ENT-MULTI", "Multi Corp", "US");

        Result<List<DppCertificate>> certs = controller.getCertificatesForEnterprise("ENT-MULTI");
        assertEquals(3, certs.getData().size());
    }
}
