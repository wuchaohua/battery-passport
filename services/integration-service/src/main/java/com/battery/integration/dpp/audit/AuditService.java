package com.battery.integration.dpp.audit;

import com.battery.integration.dpp.config.DppRegistryProperties;
import com.battery.integration.dpp.model.AuditEvidence;
import com.battery.integration.dpp.model.AuditEvidence.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Immutable audit evidence chain service.
 *
 * Each audit entry is linked to the previous entry via a SHA-256 digest,
 * forming a tamper-evident chain. Entries can be verified for integrity.
 *
 * The chain structure is:
 *   entry[i] = { ..., previousDigest = hash(entry[i-1]), digest = hash(entry[i]) }
 *   entry[0] has previousDigest = "0" (genesis entry)
 */
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private static final String GENESIS_PREVIOUS_DIGEST = "0";

    private final DppRegistryProperties properties;
    private final Map<String, List<AuditEvidence>> evidenceChain = new ConcurrentHashMap<>();

    public AuditService(DppRegistryProperties properties) {
        this.properties = properties;
    }

    /**
     * Record a new audit evidence entry in the chain for an enterprise.
     * Automatically links to the previous entry.
     */
    public AuditEvidence record(String enterpriseId,
                                 EventType eventType,
                                 String operatorId,
                                 String description,
                                 String payloadDigest) {

        List<AuditEvidence> chain = evidenceChain.computeIfAbsent(
                enterpriseId, k -> new CopyOnWriteArrayList<>());

        // Enforce maximum entries per enterprise
        if (chain.size() >= properties.getMaxAuditEntriesPerEnterprise()) {
            chain.remove(0);
        }

        String previousDigest = chain.isEmpty()
                ? GENESIS_PREVIOUS_DIGEST
                : computeEntryDigest(chain.get(chain.size() - 1));

        String evidenceId = UUID.randomUUID().toString().replace("-", "");

        AuditEvidence evidence = new AuditEvidence(
                evidenceId, enterpriseId, eventType, operatorId,
                description, payloadDigest, previousDigest);

        chain.add(evidence);

        log.debug("Audit evidence recorded: {} for enterprise {} by {}",
                eventType, enterpriseId, operatorId);

        return evidence;
    }

    /**
     * Verify the integrity of the entire audit chain for an enterprise.
     * Returns a ChainVerification result with any discrepancies found.
     */
    public ChainVerification verifyChain(String enterpriseId) {
        List<AuditEvidence> chain = evidenceChain.get(enterpriseId);
        if (chain == null || chain.isEmpty()) {
            return new ChainVerification(true, Collections.emptyList());
        }

        List<String> discrepancies = new ArrayList<>();

        for (int i = 0; i < chain.size(); i++) {
            AuditEvidence current = chain.get(i);

            // Verify genesis entry
            if (i == 0) {
                if (!GENESIS_PREVIOUS_DIGEST.equals(current.getPreviousDigest())) {
                    discrepancies.add("Entry " + i + " (genesis): expected previousDigest="
                            + GENESIS_PREVIOUS_DIGEST + " but got " + current.getPreviousDigest());
                }
                continue;
            }

            // Verify link to previous entry
            AuditEvidence previous = chain.get(i - 1);
            String expectedPreviousDigest = computeEntryDigest(previous);

            if (!expectedPreviousDigest.equals(current.getPreviousDigest())) {
                discrepancies.add("Entry " + i + " (evidenceId=" + current.getEvidenceId()
                        + "): link broken. Expected previousDigest=" + expectedPreviousDigest
                        + " but got " + current.getPreviousDigest());
            }
        }

        boolean valid = discrepancies.isEmpty();
        if (valid) {
            log.info("Audit chain verified for enterprise {}: {} entries intact", enterpriseId, chain.size());
        } else {
            log.warn("Audit chain verification FAILED for enterprise {}: {} discrepancies",
                    enterpriseId, discrepancies.size());
        }

        return new ChainVerification(valid, discrepancies);
    }

    /**
     * Get the audit chain for an enterprise.
     */
    public List<AuditEvidence> getChain(String enterpriseId) {
        return evidenceChain.getOrDefault(enterpriseId, Collections.emptyList());
    }

    /**
     * Get all enterprises with audit chains.
     */
    public Set<String> getTrackedEnterprises() {
        return evidenceChain.keySet();
    }

    /**
     * Compute the SHA-256 digest of an evidence entry for chain linking.
     */
    private String computeEntryDigest(AuditEvidence entry) {
        String data = entry.getEvidenceId()
                + entry.getEnterpriseId()
                + entry.getEventType().name()
                + entry.getOperatorId()
                + entry.getDescription()
                + entry.getPayloadDigest()
                + entry.getPreviousDigest()
                + entry.getOccurredAt().toString();

        return sha256(data);
    }

    private String sha256(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 computation failed", e);
        }
    }

    /**
     * Result of an audit chain integrity verification.
     */
    public record ChainVerification(boolean valid, List<String> discrepancies) {}
}
