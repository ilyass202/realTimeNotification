package com.pca.Backend.DTO;

import java.time.LocalDateTime;

public record CardResponse(String message, LocalDateTime creationDate, Long id ) {

}
