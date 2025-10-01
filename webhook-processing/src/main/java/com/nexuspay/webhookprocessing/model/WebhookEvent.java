package com.nexuspay.webhookprocessing.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@Entity
@Table(
        name = "webhook_events",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "event_id"})
)
@NoArgsConstructor @AllArgsConstructor @Builder
public class WebhookEvent {

    public enum Status { RECEIVED, PUBLISHED, FAILED }

    @Id @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    /** Raw JSON from provider. Kept as String, stored as jsonb. */
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_error")
    private String lastError;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** When publish to Kafka succeeded. */
    @Column(name = "processed_at")
    private Instant processedAt;

    public static WebhookEvent received(String tenant, String eventId, byte[] raw) {
        return WebhookEvent.builder()
                .tenantId(tenant)
                .eventId(eventId)
                .payload(new String(raw, StandardCharsets.UTF_8))
                .status(Status.RECEIVED)
                .attempts(0)
                .build();
    }
}
