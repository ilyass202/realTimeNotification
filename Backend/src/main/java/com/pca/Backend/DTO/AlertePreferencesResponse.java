package com.pca.Backend.DTO;

/**
 * Réponse pour les switches React (préférences alertes par client).
 */
public record AlertePreferencesResponse(
    Long id,
    Long clientId,
    boolean alerteTransaction,
    boolean alerteFraude,
    boolean alerteCarte,
    boolean active,
    boolean blackList,
    String telephone
) {
}
