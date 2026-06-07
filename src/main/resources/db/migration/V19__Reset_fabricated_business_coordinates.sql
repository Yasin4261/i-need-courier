-- V19: Reset fabricated (random) business coordinates so they can be backfilled with real data.
--
-- V18 filled coordinate-less businesses with random points around Kadıköy as a map stop-gap.
-- No real coordinate data existed yet (see issue #19 / docs/adr/ADR-001), so every existing
-- business currently holds fabricated coordinates that cannot be told apart from real ones.
--
-- We null them here so that BusinessCoordinateBackfillRunner repopulates them on the next
-- startup by geocoding each business's address (≤1 req/s, Nominatim). This supersedes V18's
-- random business fill without editing the already-applied V18 (whose checksum is immutable).
UPDATE businesses
SET latitude  = NULL,
    longitude = NULL;
