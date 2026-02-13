package com.gbm.taskapi.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ScheduledJobs {

    private final MeterRegistry meterRegistry;

    @Scheduled(fixedRate = 10000)
    public void cacheMetrics() {
        var hitCounter = meterRegistry
                .find("cache.gets")
                .tag("cache", "users")
                .tag("result", "hit")
                .functionCounter();

        var missCounter = meterRegistry
                .find("cache.gets")
                .tag("cache", "users")
                .tag("result", "miss")
                .functionCounter();

        double hits = hitCounter != null ? hitCounter.count() : 0;
        double misses = missCounter != null ? missCounter.count() : 0;
        double total = hits + misses;
        double hitRatio = total > 0 ? (hits / total) * 100 : 0;

        log.info("Cache [users] - hits: {}, misses: {}, hit ratio: {}", hits, misses, hitRatio);
    }
}
