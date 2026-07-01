package com.battery.integration.dpp.metadata;

import com.battery.integration.dpp.model.DppJwkSet;
import com.battery.integration.dpp.model.DppMetadataEntry;
import com.battery.integration.dpp.model.DppSchema;
import com.battery.integration.dpp.registry.DppRegistryClient;
import com.battery.integration.dpp.registry.DppRegistryException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for EU DPP Registry metadata operations.
 * Wraps DppRegistryClient calls for metadata/v1 endpoints with local caching.
 */
@Service
public class DppMetadataService {

    private static final Logger log = LoggerFactory.getLogger(DppMetadataService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final DppRegistryClient registryClient;
    private final ConcurrentMap<String, DppMetadataEntry> metadataCache = new ConcurrentHashMap<>();
    private DppJwkSet cachedJwks;

    public DppMetadataService(DppRegistryClient registryClient) {
        this.registryClient = registryClient;
    }

    /**
     * Fetch JWKS public keys from the EU DPP Registry.
     */
    public DppJwkSet fetchJwks() {
        try {
            JsonNode response = registryClient.fetchJwks();
            DppJwkSet jwkSet = MAPPER.treeToValue(response, DppJwkSet.class);
            cachedJwks = jwkSet;
            log.info("Fetched JWKS with {} key(s)", jwkSet.getKeys() != null ? jwkSet.getKeys().size() : 0);
            return jwkSet;
        } catch (Exception e) {
            throw new DppRegistryException("Failed to fetch JWKS", e);
        }
    }

    public DppJwkSet getCachedJwks() { return cachedJwks; }

    /**
     * Submit a DPP metadata entry (POST /metadata/v1).
     */
    public DppMetadataEntry submitMetadata(DppMetadataEntry entry) {
        try {
            ObjectNode body = MAPPER.valueToTree(entry);
            JsonNode response = registryClient.submitMetadata(body);
            DppMetadataEntry result = MAPPER.treeToValue(response, DppMetadataEntry.class);
            if (result.getEntryId() != null) {
                metadataCache.put(result.getEntryId(), result);
            }
            log.info("Submitted metadata entry: {}", result.getEntryId());
            return result;
        } catch (Exception e) {
            throw new DppRegistryException("Failed to submit metadata", e);
        }
    }

    /**
     * Register a DPP via the alternate path (POST /metadata/v1/registerDPP).
     */
    public DppMetadataEntry registerDpp(DppMetadataEntry entry) {
        try {
            ObjectNode body = MAPPER.valueToTree(entry);
            JsonNode response = registryClient.submitMetadataRegisterDpp(body);
            DppMetadataEntry result = MAPPER.treeToValue(response, DppMetadataEntry.class);
            if (result.getEntryId() != null) {
                metadataCache.put(result.getEntryId(), result);
            }
            log.info("Registered DPP entry: {}", result.getEntryId());
            return result;
        } catch (Exception e) {
            throw new DppRegistryException("Failed to register DPP", e);
        }
    }

    /**
     * Fetch the registration proof JWT for a given registry ID.
     */
    public String fetchRegistrationProof(String registryId) {
        try {
            String jwt = registryClient.fetchRegistrationProof(registryId);
            log.info("Fetched registration proof for registryId: {}", registryId);
            return jwt;
        } catch (Exception e) {
            throw new DppRegistryException("Failed to fetch registration proof", e);
        }
    }

    public DppMetadataEntry getCachedEntry(String entryId) {
        return metadataCache.get(entryId);
    }

    public List<DppMetadataEntry> getAllCachedEntries() {
        return new ArrayList<>(metadataCache.values());
    }
}
