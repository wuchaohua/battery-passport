package com.battery.integration.dpp.registry;

import com.battery.integration.dpp.certificate.CertificateManager;
import com.battery.integration.dpp.config.DppRegistryProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;

public class DppRegistryClient {

    private static final Logger log = LoggerFactory.getLogger(DppRegistryClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final DppRegistryProperties properties;
    private final CertificateManager certificateManager;

    public DppRegistryClient(DppRegistryProperties properties, CertificateManager certificateManager) {
        this.properties = properties;
        this.certificateManager = certificateManager;
    }

    public JsonNode registerEnterprise(String enterpriseId,
                                       String legalName,
                                       String vatNumber,
                                       String eoriNumber,
                                       String countryCode) throws Exception {
        HttpClient client = buildMtlsClient(enterpriseId);

        ObjectNode body = MAPPER.createObjectNode();
        body.put("enterpriseId", enterpriseId);
        body.put("legalName", legalName);
        body.put("vatNumber", vatNumber);
        body.put("eoriNumber", eoriNumber);
        body.put("countryCode", countryCode);
        body.put("registrationType", "BATTERY_PASSPORT_OPERATOR");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/enterprises"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("DPP Registry registerEnterprise status: {}", response.statusCode());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return MAPPER.readTree(response.body());
        }
        throw new DppRegistryException("Registration failed: HTTP " + response.statusCode()
                + " - " + response.body());
    }

    public JsonNode bindCertificate(String dppRegistryId, String certificatePem) throws Exception {
        HttpClient client = buildPlatformClient();

        ObjectNode body = MAPPER.createObjectNode();
        body.put("certificatePem", certificatePem);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/enterprises/" + dppRegistryId + "/certificates"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("DPP Registry bindCertificate status: {}", response.statusCode());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return MAPPER.readTree(response.body());
        }
        throw new DppRegistryException("Certificate binding failed: HTTP " + response.statusCode()
                + " - " + response.body());
    }

    public JsonNode getRegistrationStatus(String dppRegistryId) throws Exception {
        HttpClient client = buildPlatformClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/enterprises/" + dppRegistryId))
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return MAPPER.readTree(response.body());
        }
        throw new DppRegistryException("Query failed: HTTP " + response.statusCode());
    }

    public JsonNode revokeRegistration(String dppRegistryId, String reason) throws Exception {
        HttpClient client = buildPlatformClient();

        ObjectNode body = MAPPER.createObjectNode();
        body.put("reason", reason);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/enterprises/" + dppRegistryId + "/revoke"))
                .header("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("DPP Registry revokeRegistration status: {}", response.statusCode());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return MAPPER.readTree(response.body());
        }
        throw new DppRegistryException("Revocation failed: HTTP " + response.statusCode());
    }

    private HttpClient buildMtlsClient(String enterpriseId) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
                certificateManager.getKeyManagers(enterpriseId),
                certificateManager.getTrustManagers(),
                new SecureRandom()
        );

        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .build();
    }

    private HttpClient buildPlatformClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .build();
    }

    // ========================================================================
    // EU DPP Registry API v1 - Metadata endpoints
    // ========================================================================

    public JsonNode fetchJwks() throws Exception {
        HttpClient client = buildPlatformClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/.well-known/jwks.json"))
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return MAPPER.readTree(response.body());
        }
        throw new DppRegistryException("JWKS fetch failed: HTTP " + response.statusCode());
    }

    public JsonNode submitMetadata(ObjectNode metadataBody) throws Exception {
        HttpClient client = buildPlatformClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/metadata/v1"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(metadataBody)))
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return MAPPER.readTree(response.body());
        }
        throw new DppRegistryException("Metadata submit failed: HTTP " + response.statusCode());
    }

    public JsonNode submitMetadataRegisterDpp(ObjectNode metadataBody) throws Exception {
        HttpClient client = buildPlatformClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/metadata/v1/registerDPP"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(metadataBody)))
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return MAPPER.readTree(response.body());
        }
        throw new DppRegistryException("RegisterDPP failed: HTTP " + response.statusCode());
    }

    public String fetchRegistrationProof(String registryId) throws Exception {
        HttpClient client = buildPlatformClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/metadata/v1/" + registryId + "/proof"))
                .header("Accept", "application/jwt")
                .GET()
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body().trim();
        }
        throw new DppRegistryException("Proof fetch failed: HTTP " + response.statusCode());
    }

    // ========================================================================
    // EU DPP Registry API v1 - Schema endpoints
    // ========================================================================

    public JsonNode createOrUpdateSchema(ObjectNode schemaBody) throws Exception {
        HttpClient client = buildPlatformClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/schema/v1"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(schemaBody)))
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return MAPPER.readTree(response.body());
        }
        throw new DppRegistryException("Schema create failed: HTTP " + response.statusCode());
    }

    public JsonNode getCurrentSchema() throws Exception {
        HttpClient client = buildPlatformClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/schema/v1/current"))
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return MAPPER.readTree(response.body());
        }
        throw new DppRegistryException("Schema fetch failed: HTTP " + response.statusCode());
    }

    public void deleteCurrentSchema() throws Exception {
        HttpClient client = buildPlatformClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/schema/v1/current"))
                .method("DELETE", HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new DppRegistryException("Schema delete failed: HTTP " + response.statusCode());
        }
    }
}
