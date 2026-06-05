package com.pca.Backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pca.Backend.DTO.AlerteCreateRequest;
import com.pca.Backend.DTO.AlertePreferencesResponse;
import com.pca.Backend.DTO.AlertePreferencesUpdateRequest;
import com.pca.Backend.Service.ReferentielAlerteService;

import lombok.RequiredArgsConstructor;

/**
 * Préférences alertes (switches mobile) — {@code clientId} = {@code users.id} / {@code user_id} Kafka.
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class ReferentielAlerteController {

    private final ReferentielAlerteService service;

    @GetMapping("/{clientId}")
    public ResponseEntity<AlertePreferencesResponse> get(@PathVariable Long clientId) {
        return ResponseEntity.ok(service.getByClientId(clientId));
    }

    @PostMapping
    public ResponseEntity<AlertePreferencesResponse> create(@RequestBody AlerteCreateRequest request) {
        return ResponseEntity.ok(service.create(request));
    }


    @PatchMapping("/{clientId}")
    public ResponseEntity<AlertePreferencesResponse> patch(
        @PathVariable Long clientId,
        @RequestBody AlertePreferencesUpdateRequest request
    ) {
        return ResponseEntity.ok(service.update(clientId, request));
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<AlertePreferencesResponse> put(
        @PathVariable Long clientId,
        @RequestBody AlertePreferencesUpdateRequest request
    ) {
        return ResponseEntity.ok(service.replace(clientId, request));
    }
}
