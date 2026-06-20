package com.cropplanner.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates, signs, and validates JWT access tokens — hand-rolled with only
 * JDK classes (javax.crypto) and Jackson (already a transitive dependency
 * via spring-boot-starter-web), so there's nothing extra to download.
 *
 * This replaces an earlier version that used the JJWT library. JJWT
 * consistently failed to resolve in one deployment environment
 * ("Unresolved dependency" in Maven, even though the artifact coordinates
 * were confirmed valid and downloadable directly) — rather than keep
 * debugging an environment we don't have direct access to, JWT support
 * was rewritten using only what's already guaranteed to be on the classpath.
 *
 * Implements the standard JWT structure: base64url(header).base64url(payload).base64url(signature),
 * signed with HMAC-SHA256 (alg: "HS256") — interoperable with any standard
 * JWT library/tool if you ever need to inspect or verify a token externally
 * (e.g. paste one into jwt.io to decode it).
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private static final String ALG_HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    @Value("${jwt.secret:change-me-in-production-must-be-at-least-32-chars}")
    private String secret;

    @Value("${jwt.expiry-ms:3600000}") // default 1 hour
    private long expiryMs;

    private byte[] secretBytes;

    @PostConstruct
    public void init() {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secretBytes.length < 32) {
            log.warn("jwt.secret is shorter than 32 bytes — this is insecure for HMAC-SHA256. " +
                    "Set a longer jwt.secret in application.properties before deploying.");
        }
    }

    public String generateToken(String email, String role) {
        long now = System.currentTimeMillis();

        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", email);
        payload.put("role", role);
        payload.put("iat", now / 1000);
        payload.put("exp", (now + expiryMs) / 1000);

        String headerB64 = ENCODER.encodeToString(ALG_HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = encodeJson(payload);
        String signingInput = headerB64 + "." + payloadB64;
        String signatureB64 = sign(signingInput);

        return signingInput + "." + signatureB64;
    }

    /** Returns the email (subject) from a token, or null if the token is malformed/invalid. */
    public String extractEmail(String token) {
        Map<String, Object> claims = parseAndVerify(token);
        return claims != null ? (String) claims.get("sub") : null;
    }

    /** Returns the role claim from a token, or null if the token is malformed/invalid. */
    public String extractRole(String token) {
        Map<String, Object> claims = parseAndVerify(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    public boolean validateToken(String token) {
        return parseAndVerify(token) != null;
    }

    /**
     * Splits the token, verifies the signature matches, checks expiry, and
     * returns the decoded claims map — or null if anything is invalid
     * (malformed structure, bad signature, or expired).
     */
    private Map<String, Object> parseAndVerify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String signingInput = parts[0] + "." + parts[1];
            String expectedSignature = sign(signingInput);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                log.debug("JWT signature mismatch");
                return null;
            }

            byte[] payloadJson = DECODER.decode(parts[1]);
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = MAPPER.readValue(payloadJson, Map.class);

            Object exp = claims.get("exp");
            if (exp instanceof Number expSeconds) {
                long nowSeconds = System.currentTimeMillis() / 1000;
                if (nowSeconds > expSeconds.longValue()) {
                    log.debug("JWT token expired");
                    return null;
                }
            }
            return claims;
        } catch (Exception e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    private String sign(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            byte[] signatureBytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return ENCODER.encodeToString(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // HmacSHA256 is guaranteed available on every standard JVM, so this
            // should never actually happen — fail loudly if it somehow does.
            throw new IllegalStateException("HmacSHA256 unavailable on this JVM", e);
        }
    }

    private String encodeJson(Map<String, Object> map) {
        try {
            byte[] json = MAPPER.writeValueAsBytes(map);
            return ENCODER.encodeToString(json);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode JWT payload", e);
        }
    }

    /** Avoids timing-attack-prone String.equals() for signature comparison. */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
