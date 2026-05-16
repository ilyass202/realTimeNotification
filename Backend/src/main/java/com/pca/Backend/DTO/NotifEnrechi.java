package com.pca.Backend.DTO;

public record NotifEnrechi(Long userId, Long destinationId, Long amount ,String eventId, boolean isCritical, boolean isSuspected, String message) {

}
