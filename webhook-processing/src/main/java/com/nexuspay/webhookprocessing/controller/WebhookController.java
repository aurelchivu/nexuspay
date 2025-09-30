package com.nexuspay.webhookprocessing.controller;

import com.nexuspay.webhookprocessing.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final WebhookService service;

    @PostMapping
    public ResponseEntity<Void> receive(@RequestHeader("X-Signature") String sig,
                                        @RequestHeader("Idempotency-Key") String idem,
                                        @RequestBody byte[] rawBody) {
        service.process(sig, idem, rawBody); // validate HMAC (over raw body + timestamp), persist, publish
        return ResponseEntity.ok().build();  // fast ACK (processing async)
    }
}

