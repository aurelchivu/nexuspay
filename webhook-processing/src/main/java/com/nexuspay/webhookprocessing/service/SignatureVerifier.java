package com.nexuspay.webhookprocessing.service;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Component
public class SignatureVerifier {

    private final String secret = System.getenv().getOrDefault("WEBHOOK_SECRET", "dev-secret");

    public void verifyOrThrow(String header, byte[] rawBody) {
        // header format: t=1699999999,v1=abcdef1234...
        String[] parts = header.split(",");
        long ts = 0; String sig = null;
        for (String part : parts) {
            if (part.startsWith("t=")) ts = Long.parseLong(part.substring(2));
            if (part.startsWith("v1=")) sig = part.substring(3);
        }
        if (ts == 0 || sig == null) {
            throw new SecurityException("Invalid signature header");
        }
        long now = Instant.now().getEpochSecond();
        // 5 minutes
        long toleranceSec = 300;
        if (Math.abs(now - ts) > toleranceSec) {
            throw new SecurityException("Stale signature");
        }
        String payload = ts + "." + new String(rawBody, StandardCharsets.UTF_8);
        String expected = hmacSha256(secret, payload);
        if (!MessageDigest.isEqual(sig.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("Signature mismatch");
        }
    }

    private static String hmacSha256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
