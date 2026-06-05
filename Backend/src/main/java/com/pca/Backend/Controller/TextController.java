package com.pca.Backend.Controller;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pca.Backend.Entity.Transaction;
import com.pca.Backend.Repo.TransactionRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Benchmark synchrone (API classique) : insertions JPA en boucle.
 * Les inserts alimentent ensuite le pipeline Kafka via CDC Debezium → topic transactions.
 */
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TextController {

    private final TransactionRepo repo;

    @GetMapping("/{count}")
    public ResponseEntity<Map<String, Object>> testBenchMark(@PathVariable("count") int count) {
        if (count <= 0 || count > 50_000) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "count doit être entre 1 et 50000"
            ));
        }
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            Transaction tx = new Transaction();
            tx.setUserId(27L);
            tx.setDestinataireId(2L);
            tx.setAmount((long) ((i + 1) % 100));
            repo.save(tx);
        }
        long durationMs = System.currentTimeMillis() - start;
        double durationSec = durationMs / 1000.0;
        double throughputPerSec = count / durationSec;
        double capacityPerDay = throughputPerSec * 86_400;

        log.info("Benchmark sync: count={}, durationMs={}, throughput/s={}", count, durationMs, throughputPerSec);

        return ResponseEntity.ok(Map.of(
            "count", count,
            "durationMs", durationMs
        ));
    }

  
    @GetMapping("/stream")
    public ResponseEntity<Map<String, Object>> streamBenchMark() throws InterruptedException {
        int batchSize = 100;
        int intervalSec = 10;
        int durationMin = 10;
        int cycles = (int) TimeUnit.MINUTES.toSeconds(durationMin) / intervalSec;
        int total = cycles * batchSize;

        long start = System.currentTimeMillis();
        for (int c = 0; c < cycles; c++) {
            for (int i = 0; i < batchSize; i++) {
                Transaction tx = new Transaction();
                tx.setUserId(27L);
                tx.setDestinataireId(2L);
                tx.setAmount((long) ((c * batchSize + i + 1) % 100));
                repo.save(tx);
            }
            if (c < cycles - 1) {
                Thread.sleep(TimeUnit.SECONDS.toMillis(intervalSec));
            }
        }

        long durationMs = System.currentTimeMillis() - start;
        double durationSec = durationMs / 1000.0;
        double throughputPerSec = total / durationSec;
        double capacityPerDay = throughputPerSec * 86_400;

        log.info(
            "Benchmark stream: total={}, batchSize={}, every={}s, durationMin={}, durationMs={}, throughput/s={}",
            total,
            batchSize,
            intervalSec,
            durationMin,
            durationMs,
            throughputPerSec
        );

        Map<String, Object> response = new HashMap<>();
        response.put("mode", "stream_api_jpa");
        response.put("batchSize", batchSize);
        response.put("intervalSec", intervalSec);
        response.put("durationMin", durationMin);
        response.put("cycles", cycles);
        response.put("total", total);
        response.put("durationMs", durationMs);
        response.put("durationSec", durationSec);
        response.put("throughputPerSec", throughputPerSec);
        return ResponseEntity.ok(response);
    }
}
