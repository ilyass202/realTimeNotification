package com.pca.Backend.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record NotifEnrechi(
    Long userId,
    Long destinationId,
    Long amount,
    String eventId,
    boolean isCritical,
    boolean isSuspected,
    String message,
    @JsonProperty("created_at")
    @JsonAlias("createdAt")
    Long createdAt
) {
}
