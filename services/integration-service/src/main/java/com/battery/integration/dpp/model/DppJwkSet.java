package com.battery.integration.dpp.model;

import java.util.List;

/**
 * JWKS (JSON Web Key Set) returned by GET /.well-known/jwks.json
 * Used to verify the RS256 signature of registration proof JWTs.
 */
public class DppJwkSet {

    private List<JwkKey> keys;

    public DppJwkSet() {}

    public List<JwkKey> getKeys() { return keys; }
    public void setKeys(List<JwkKey> keys) { this.keys = keys; }

    /**
     * A single JWK key entry in the JWKS set.
     */
    public static class JwkKey {
        private String kty;  // "RSA"
        private String alg;  // "RS256"
        private String kid;  // Key ID
        private String n;    // RSA modulus (Base64URL encoded)
        private String e;    // RSA exponent (Base64URL encoded)
        private String use;  // "sig"

        public JwkKey() {}

        public String getKty() { return kty; }
        public void setKty(String kty) { this.kty = kty; }
        public String getAlg() { return alg; }
        public void setAlg(String alg) { this.alg = alg; }
        public String getKid() { return kid; }
        public void setKid(String kid) { this.kid = kid; }
        public String getN() { return n; }
        public void setN(String n) { this.n = n; }
        public String getE() { return e; }
        public void setE(String e) { this.e = e; }
        public String getUse() { return use; }
        public void setUse(String use) { this.use = use; }
    }
}
