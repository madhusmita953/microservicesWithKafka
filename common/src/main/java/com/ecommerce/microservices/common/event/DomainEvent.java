package com.ecommerce.microservices.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base event class for all domain events in the system.
 * Follows event sourcing pattern with correlation ID for tracking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public abstract class DomainEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_id")
    private String eventId = UUID.randomUUID().toString();

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @JsonProperty("source")
    private String source;

    public abstract String getEventType();
}