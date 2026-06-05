package com.pca.Backend.Service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pca.Backend.DTO.AlerteCreateRequest;
import com.pca.Backend.DTO.AlertePreferencesResponse;
import com.pca.Backend.DTO.AlertePreferencesUpdateRequest;
import com.pca.Backend.Entity.ReferentielAlerte;
import com.pca.Backend.Entity.ReferentielUser;
import com.pca.Backend.Repo.ReferentielAlerteRepo;
import com.pca.Backend.Repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReferentielAlerteService {

    private final ReferentielAlerteRepo alerteRepo;
    private final UserRepo userRepo;
    private final ReferentielAlerteCache alerteCache;

    public AlertePreferencesResponse getByClientId(Long clientId) {
       
        ReferentielAlerte alerte = alerteRepo.findByClientId(clientId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Aucun référentiel alerte pour clientId=" + clientId
            ));
        return toResponse(alerte);
    }

    public AlertePreferencesResponse create(AlerteCreateRequest request) {
        
        if (alerteRepo.findByClientId(request.clientId()).isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Référentiel déjà existant pour clientId=" + request.clientId()
            );
        }
        ReferentielAlerte alerte = ReferentielAlerte.builder()
            .clientId(request.clientId())
            .alerteTransaction(boolOrDefault(request.alerteTransaction(), true))
            .alerteFraude(boolOrDefault(request.alerteFraude(), true))
            .alerteCarte(boolOrDefault(request.alerteCarte(), true))
            .isActive(boolOrDefault(request.active(), true))
            .isBlackList(boolOrDefault(request.blackList(), false))
            .telephone(request.telephone())
            .build();
        ReferentielAlerte saved = alerteRepo.save(alerte);
        alerteCache.refresh(request.clientId(), saved);
        return toResponse(saved);
    }

    public AlertePreferencesResponse update(Long clientId, AlertePreferencesUpdateRequest request) {

        ReferentielAlerte alerte = alerteRepo.findByClientId(clientId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Créez d'abord le référentiel (POST /api/alerts)"
            ));
        if (request.alerteTransaction() != null) {
            alerte.setAlerteTransaction(request.alerteTransaction());
        }
        if (request.alerteFraude() != null) {
            alerte.setAlerteFraude(request.alerteFraude());
        }
        if (request.alerteCarte() != null) {
            alerte.setAlerteCarte(request.alerteCarte());
        }
        if (request.active() != null) {
            alerte.setActive(request.active());
        }
        if (request.blackList() != null) {
            alerte.setBlackList(request.blackList());
        }
        if (request.telephone() != null) {
            alerte.setTelephone(request.telephone());
        }
        ReferentielAlerte saved = alerteRepo.save(alerte);
        alerteCache.refresh(clientId, saved);
        return toResponse(saved);
    }

    public AlertePreferencesResponse replace(Long clientId, AlertePreferencesUpdateRequest request) {
        ReferentielAlerte alerte = alerteRepo.findByClientId(clientId)
            .orElseGet(() -> ReferentielAlerte.builder().clientId(clientId).build());
        alerte.setAlerteTransaction(boolOrDefault(request.alerteTransaction(), alerte.isAlerteTransaction()));
        alerte.setAlerteFraude(boolOrDefault(request.alerteFraude(), alerte.isAlerteFraude()));
        alerte.setAlerteCarte(boolOrDefault(request.alerteCarte(), alerte.isAlerteCarte()));
        alerte.setActive(boolOrDefault(request.active(), alerte.isActive()));
        alerte.setBlackList(boolOrDefault(request.blackList(), alerte.isBlackList()));
        if (request.telephone() != null) {
            alerte.setTelephone(request.telephone());
        }
        ReferentielAlerte saved = alerteRepo.save(alerte);
        alerteCache.refresh(clientId, saved);
        return toResponse(saved);
    }

    private static boolean boolOrDefault(Boolean value, boolean defaultValue) {
        return value != null ? value : defaultValue;
    }

    private static AlertePreferencesResponse toResponse(ReferentielAlerte a) {
        return new AlertePreferencesResponse(
            a.getId(),
            a.getClientId(),
            a.isAlerteTransaction(),
            a.isAlerteFraude(),
            a.isAlerteCarte(),
            a.isActive(),
            a.isBlackList(),
            a.getTelephone()
        );
    }
}
