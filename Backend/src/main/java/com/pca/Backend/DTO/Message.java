package com.pca.Backend.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(
    @JsonAlias("user_id") Long userId,
    @JsonAlias({"destinataire_id", "destinataire"}) Long destinataireId,
    Long amount
) {

}
