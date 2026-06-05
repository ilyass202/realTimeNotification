package com.pca.Backend.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.pca.Backend.Entity.ReferentielAlerte;
import com.pca.Backend.Repo.ReferentielAlerteRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReferentielAlerteCache {

    private final ReferentielAlerteRepo repo;
    private final ConcurrentHashMap<Long, CacheEntry> cache = new ConcurrentHashMap<>();
    public Optional<ReferentielAlerte> getForClientId(Long clientId) {
        if (clientId == null || clientId <= 0) {
            return Optional.empty();
        }
        CacheEntry cached = cache.get(clientId);
        if (cached != null && cached.matches(clientId)) {
            return cached.toOptional();
        }
        Optional<ReferentielAlerte> fromDb = repo.findByClientId(clientId);
        cache.put(clientId, CacheEntry.from(fromDb, clientId));
        return fromDb;
    }

    public Optional<ReferentielAlerte> findByClientId(Long clientId) {
        return getForClientId(clientId);
    }

    public boolean isTransactionEnabled(Long clientId) {
        return allows(getForClientId(clientId), ReferentielAlerte::isAlerteTransaction);
    }

    public boolean isFraudeEnabled(Long clientId) {
        return allows(getForClientId(clientId), ReferentielAlerte::isAlerteFraude);
    }

    public boolean isCarteEnabled(Long clientId) {
        return allows(getForClientId(clientId), ReferentielAlerte::isAlerteCarte);
    }

 
    public void refresh(Long clientId, ReferentielAlerte saved) {
        if (clientId == null || saved == null) {
            return;
        }
        if (!Objects.equals(saved.getClientId(), clientId)) {
            invalidate(clientId);
            return;
        }
        cache.put(clientId, CacheEntry.present(saved));
    }

    public void invalidate(Long clientId) {
        if (clientId != null) {
            cache.remove(clientId);
        }
    }

    public void clear() {
        cache.clear();
    }

    private static boolean allows(
        Optional<ReferentielAlerte> ref,
        java.util.function.Predicate<ReferentielAlerte> flag
    ) {
        return ref
            .filter(r -> r.isActive() && !r.isBlackList())
            .filter(flag)
            .isPresent();
    }

    private record CacheEntry(ReferentielAlerte value, boolean absent, long clientId) {

        static CacheEntry present(ReferentielAlerte ref) {
            return new CacheEntry(ref, false, ref.getClientId());
        }

        static CacheEntry absent(long clientId) {
            return new CacheEntry(null, true, clientId);
        }

        static CacheEntry from(Optional<ReferentielAlerte> fromDb, long requestedClientId) {
            return fromDb.map(CacheEntry::present).orElseGet(() -> absent(requestedClientId));
        }

        boolean matches(long requestedClientId) {
            return clientId == requestedClientId;
        }

        Optional<ReferentielAlerte> toOptional() {
            return absent ? Optional.empty() : Optional.of(value);
        }
    }
}
