package com.api.pako.service.geocoding;

import com.api.pako.config.GeocodingProperties;
import com.api.pako.repository.BusinessRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Backfills coordinates for businesses that are missing them, on application startup.
 *
 * <p>Phase 2 of issue #19 / ADR-001. Each coordinate-less business has its address geocoded
 * and its location persisted. The work is:
 * <ul>
 *   <li><b>idempotent</b> — only businesses still missing coordinates are processed, so a
 *       re-run (e.g. another boot) does no redundant work;</li>
 *   <li><b>self-throttling</b> — {@link GeocodingService} already serialises outbound calls to
 *       the configured rate (≤1 req/s), so iterating here cannot breach the Nominatim policy;</li>
 *   <li><b>fault-tolerant</b> — a business that cannot be geocoded is left untouched and retried
 *       on the next startup; one save per business keeps partial progress.</li>
 * </ul>
 */
@Component
@Slf4j
public class BusinessCoordinateBackfillRunner implements ApplicationRunner {

    private final GeocodingProperties properties;
    private final BusinessRepository businessRepository;
    private final GeocodingService geocodingService;

    public BusinessCoordinateBackfillRunner(GeocodingProperties properties,
                                            BusinessRepository businessRepository,
                                            GeocodingService geocodingService) {
        this.properties = properties;
        this.businessRepository = businessRepository;
        this.geocodingService = geocodingService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.getBackfill().isEnabled()) {
            log.info("Business coordinate backfill is disabled; skipping.");
            return;
        }

        var pending = businessRepository.findByLatitudeIsNullOrLongitudeIsNull();
        if (pending.isEmpty()) {
            log.info("Business coordinate backfill: no businesses need coordinates.");
            return;
        }

        log.info("Business coordinate backfill: {} business(es) need coordinates.", pending.size());
        var succeeded = 0;
        var failed = 0;
        for (var business : pending) {
            var coordinates = geocodingService.geocode(business.getAddress());
            if (coordinates.isPresent()) {
                business.setLatitude(coordinates.get().latitude());
                business.setLongitude(coordinates.get().longitude());
                businessRepository.save(business);
                succeeded++;
                log.debug("Backfilled coordinates for business {} ({}).", business.getId(), business.getName());
            } else {
                failed++;
                log.warn("Could not geocode address for business {} ({}); will retry on next startup.",
                        business.getId(), business.getName());
            }
        }
        log.info("Business coordinate backfill complete: {} succeeded, {} failed.", succeeded, failed);
    }
}
