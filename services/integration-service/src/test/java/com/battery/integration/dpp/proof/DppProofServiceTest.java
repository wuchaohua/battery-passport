package com.battery.integration.dpp.proof;

import com.battery.integration.dpp.model.DppJwkSet;
import com.battery.integration.dpp.model.DppJwkSet.JwkKey;
import org.junit.jupiter.api.Test;
import java.security.*;
import java.util.Base64;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DppProofServiceTest {

    private final DppProofService proofService = new DppProofService();

    @Test
    void testParseValidJwt() {
        String jwt = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImtleS0xIn0.eyJzdWIiOiJSRUctRVUtMDAxIn0.signature";
        String[] parts = proofService.parseJwt(jwt);
        assertEquals(3, parts.length);
    }

    @Test
    void testParseInvalidJwt() {
        assertThrows(IllegalArgumentException.class, () -> proofService.parseJwt("invalid.jwt"));
        assertThrows(IllegalArgumentException.class, () -> proofService.parseJwt("too.many.parts.here"));
    }

    @Test
    void testExtractKeyId() {
        String jwt = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImtleS0xIn0.eyJzdWIiOiJSRUctRVUtMDAxIn0.signature";
        assertEquals("key-1", proofService.extractKeyId(jwt));
    }

    @Test
    void testExtractSubject() {
        String jwt = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImtleS0xIn0.eyJzdWIiOiJSRUctRVUtMDAxIn0.signature";
        assertEquals("REG-EU-001", proofService.extractSubject(jwt));
    }

    @Test
    void testBuildPublicKeyFromJwk() throws Exception {
        // Generate a real RSA key pair to get valid modulus and exponent
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        java.security.interfaces.RSAPublicKey pubKey = (java.security.interfaces.RSAPublicKey) keyPair.getPublic();

        JwkKey key = new JwkKey();
        key.setKty("RSA");
        key.setKid("test-key-1");
        key.setN(Base64.getUrlEncoder().withoutPadding().encodeToString(pubKey.getModulus().toByteArray()));
        key.setE(Base64.getUrlEncoder().withoutPadding().encodeToString(pubKey.getPublicExponent().toByteArray()));

        PublicKey publicKey = proofService.buildPublicKeyFromJwk(key);
        assertNotNull(publicKey);
        assertEquals("RSA", publicKey.getAlgorithm());
    }

    @Test
    void testBuildPublicKeyUnsupportedType() {
        JwkKey key = new JwkKey();
        key.setKty("EC");
        assertThrows(IllegalArgumentException.class, () -> proofService.buildPublicKeyFromJwk(key));
    }

    @Test
    void testFullJwtRoundTrip() throws Exception {
        String jwt = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImtleS0xIn0.eyJzdWIiOiJSRUctRVUtMDAxIiwiaXNzIjoiRVUtRFBQLVJFR0lTVFJZIn0.fakeSignature";
        assertEquals("key-1", proofService.extractKeyId(jwt));
        assertEquals("REG-EU-001", proofService.extractSubject(jwt));

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        java.security.interfaces.RSAPublicKey pubKey = (java.security.interfaces.RSAPublicKey) keyPair.getPublic();

        JwkKey key = new JwkKey();
        key.setKty("RSA");
        key.setKid("key-1");
        key.setN(Base64.getUrlEncoder().withoutPadding().encodeToString(pubKey.getModulus().toByteArray()));
        key.setE(Base64.getUrlEncoder().withoutPadding().encodeToString(pubKey.getPublicExponent().toByteArray()));

        DppJwkSet jwkSet = new DppJwkSet();
        jwkSet.setKeys(List.of(key));
        assertFalse(proofService.verifyJwtSignature(jwt, jwkSet));
    }

    @Test
    void testVerifySignatureWithEmptyJwkSet() {
        assertFalse(proofService.verifyJwtSignature("header.payload.sig", new DppJwkSet()));
    }
}
