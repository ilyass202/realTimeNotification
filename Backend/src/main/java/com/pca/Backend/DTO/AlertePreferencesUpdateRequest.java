package com.pca.Backend.DTO;

public record AlertePreferencesUpdateRequest(
    Boolean alerteTransaction,
    Boolean alerteFraude,
    Boolean alerteCarte,
    Boolean active,
    Boolean blackList,
    String telephone
) {
}
