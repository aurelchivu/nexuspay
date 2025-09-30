package com.nexuspay.webhookprocessing.repository;

import com.nexuspay.webhookprocessing.model.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
    boolean existsByTenantIdAndEventId(String tenantId, String eventId);
}

