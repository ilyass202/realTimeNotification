package com.pca.Backend.DTO;

import java.time.LocalDateTime;

public record FraudeDtoResponse(String message, Long clinetId, Long fraudeId, LocalDateTime dateFraude) {

}
