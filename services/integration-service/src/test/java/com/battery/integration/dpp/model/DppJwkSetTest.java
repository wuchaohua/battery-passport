package com.battery.integration.dpp.model;

import com.battery.integration.dpp.model.DppJwkSet.JwkKey;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DppJwkSetTest {

    @Test
    void testDefaultConstructor() {
        DppJwkSet set = new DppJwkSet();
        assertNull(set.getKeys());
    }

    @Test
    void testSingleKey() {
        JwkKey key = new JwkKey();
        key.setKty("RSA");
        key.setAlg("RS256");
        key.setKid("key-2026-001");
        key.setN("base64url-encoded-modulus");
        key.setE("AQAB");
        key.setUse("sig");

        assertEquals("RSA", key.getKty());
        assertEquals("RS256", key.getAlg());
        assertEquals("key-2026-001", key.getKid());
        assertEquals("sig", key.getUse());

        DppJwkSet set = new DppJwkSet();
        set.setKeys(List.of(key));
        assertEquals(1, set.getKeys().size());
    }

    @Test
    void testMultipleKeys() {
        JwkKey k1 = new JwkKey(); k1.setKid("key-1");
        JwkKey k2 = new JwkKey(); k2.setKid("key-2");
        JwkKey k3 = new JwkKey(); k3.setKid("key-3");

        DppJwkSet set = new DppJwkSet();
        set.setKeys(List.of(k1, k2, k3));
        assertEquals(3, set.getKeys().size());
    }
}
