package com.nexuspay.webhookprocessing.service;

import com.nexuspay.webhookprocessing.model.WebhookEvent;
import com.nexuspay.webhookprocessing.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WebhookService {
    private final SignatureVerifier verifier;
    private final WebhookEventRepository repo;
    private final KafkaTemplate<String,String> kafka;

    public void process(String signature, String idempotencyKey, byte[] raw) {
        verifier.verifyOrThrow(signature, raw);                    // HMAC + timestamp window
        boolean exists = repo.existsByTenantIdAndEventId(tenantOf(signature), idempotencyKey);
        if (exists) return;                                        // idempotent no-op
        WebhookEvent e = WebhookEvent.received(tenantOf(signature), idempotencyKey, raw);
        repo.save(e);
        kafka.send("payment-updates", idempotencyKey, e.compactJson()); // internal event
    }

    private String tenantOf(String signatureHeader) {
        // Example: "t=1699999999,v1=abcdef...,kid=tenant123"
        for (String part : signatureHeader.split(",")) {
            if (part.startsWith("kid=")) {
                return part.substring(4); // tenant ID from key id
            }
        }
        return "default-tenant"; // fallback if none
    }

}

