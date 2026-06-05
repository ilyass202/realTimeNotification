package com.pca.Backend.DTO;

/**
 * Création initiale du référentiel alertes pour un client (ex. après inscription).
 */
public record AlerteCreateRequest(
    Long clientId,
    Boolean alerteTransaction,
    Boolean alerteFraude,
    Boolean alerteCarte,
    Boolean active,
    Boolean blackList,
    String telephone
) {
}