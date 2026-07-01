package com.battery.integration.dpp.proof;

import com.battery.integration.dpp.model.DppJwkSet;
import com.battery.integration.dpp.model.DppJwkSet.JwkKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * Service for verifying registration proof JWTs from the EU DPP Registry.
 * Uses JWKS public keys to verify RS256 JWT signatures.
 */
@Service
public class DppProofService {

    private static final Logger log = LoggerFactory.getLogger(DppProofService.class);

    /**
     * Parse a registration proof JWT into its three parts (header.payload.signature).
     */
    public String[] parseJwt(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format: expected 3 parts, got " + parts.length);
        }
        return parts;
    }

    /**
     * Decode the JWT header (Base64URL) to extract the key ID (kid).
     */
    public String extractKeyId(String jwt) {
        String[] parts = parseJwt(jwt);
        byte[] decoded = Base64.getUrlDecoder().decode(parts[0]);
        String header = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
        // Simple JSON parsing to extract kid
        int kidStart = header.indexOf("\"kid\"");
        if (kidStart < 0) return null;
        kidStart = header.indexOf('"', kidStart + 6);
        if (kidStart < 0) return null;
        int kidEnd = header.indexOf('"', kidStart + 1);
        if (kidEnd < 0) return null;
        return header.substring(kidStart + 1, kidEnd);
    }

    /**
     * Decode the JWT payload (Base64URL) to extract the subject (sub).
     */
    public String extractSubject(String jwt) {
        String[] parts = parseJwt(jwt);
        byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
        String payload = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
        int subStart = payload.indexOf("\"sub\"");
        if (subStart < 0) return null;
        subStart = payload.indexOf('"', subStart + 5);
        if (subStart < 0) return null;
        int subEnd = payload.indexOf('"', subStart + 1);
        if (subEnd < 0) return null;
        return payload.substring(subStart + 1, subEnd);
    }

    /**
     * Reconstruct an RSA public key from a JWK key entry (n, e).
     * This can be used to verify the JWT signature.
     */
    public PublicKey buildPublicKeyFromJwk(JwkKey jwkKey) throws Exception {
        if (!"RSA".equals(jwkKey.getKty())) {
            throw new IllegalArgumentException("Unsupported key type: " + jwkKey.getKty());
        }
        byte[] nBytes = Base64.getUrlDecoder().decode(jwkKey.getN());
        byte[] eBytes = Base64.getUrlDecoder().decode(jwkKey.getE());
        RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(1, nBytes), new BigInteger(1, eBytes));
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    /**
     * Verify a JWT signature using a JWKS public key identified by the key ID (kid).
     * Returns true if the signature can be verified, false otherwise.
     */
    public boolean verifyJwtSignature(String jwt, DppJwkSet jwkSet) {
        try {
            String kid = extractKeyId(jwt);
            if (kid == null && (jwkSet.getKeys() == null || jwkSet.getKeys().isEmpty())) {
                return false;
            }
            JwkKey matchingKey = null;
            if (kid != null) {
                matchingKey = jwkSet.getKeys().stream()
                        .filter(k -> kid.equals(k.getKid()))
                        .findFirst().orElse(null);
            }
            if (matchingKey == null && jwkSet.getKeys() != null && !jwkSet.getKeys().isEmpty()) {
                matchingKey = jwkSet.getKeys().get(0);
            }
            if (matchingKey == null) return false;

            PublicKey publicKey = buildPublicKeyFromJwk(matchingKey);
            String[] parts = parseJwt(jwt);
            String signedContent = parts[0] + "." + parts[1];
            byte[] signature = Base64.getUrlDecoder().decode(parts[2]);

            java.security.Signature sig = java.security.Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(signedContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            boolean valid = sig.verify(signature);
            log.info("JWT signature verification: {}", valid ? "PASSED" : "FAILED");
            return valid;
        } catch (Exception e) {
            log.warn("JWT verification failed: {}", e.getMessage());
            return false;
        }
    }
}
