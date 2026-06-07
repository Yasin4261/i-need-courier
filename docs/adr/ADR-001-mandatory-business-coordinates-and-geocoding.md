# ADR-001: Mandatory business coordinates and server-side geocoding

- **Status:** Accepted
- **Date:** 2026-06-07
- **Related:** GitHub issue [#19](https://github.com/Yasin4261/i-need-courier/issues/19)
- **Deciders:** Project owner

## Context

`BusinessOrderServiceImpl.createOrder()` fabricates **random coordinates** around
Kadıköy (40.9907, 29.0245) for orders:

- **Pickup**: uses the business's `latitude`/`longitude` when present, otherwise a
  random point near Kadıköy.
- **Delivery**: *always* a random point near Kadıköy.

`V18__Add_order_coordinates.sql` does the same (via SQL `random()`) for existing
order rows and for coordinate-less businesses.

This was a stop-gap to give the map feature something to render. Problems:

- The data is fabricated and misleading — couriers/businesses see fake locations.
- It is non-deterministic (`Math.random()` / SQL `random()`).
- There is no geocoding integration, so there is no real source of truth for
  coordinates.

Current constraints in the codebase:

- `Business.latitude` / `longitude` are nullable (`@Column private Double`); DB
  columns are nullable (added in `V3`).
- `BusinessRegistrationRequest` collects an `address` but **no** coordinates, and
  registration never sets lat/lng.
- There is no HTTP client, geocoding code, or related configuration yet.
- Real coordinate data for existing businesses is **not yet in the database** —
  this is the blocker the random fallback was papering over.

## Decision

Make business coordinates mandatory and source all coordinates from real data via
server-side geocoding, removing every random fallback.

### 1. Geocoding abstraction (`com.api.pako.service.geocoding`)

- `GeocodingService` interface: `Optional<Coordinates> geocode(String address)`
  (`Coordinates` = lat/lng record).
- `NominatimGeocodingService` implementation using Spring's `RestClient`
  (synchronous, fits the project's virtual threads):
  - Configurable base URL and identifying `User-Agent` header (required by the
    Nominatim usage policy).
  - Query params `format=jsonv2&limit=1&countrycodes=tr`.
  - A 1 request/second throttle to honor the public-instance policy.
- Cache `geocode(...)` results in Redis (already in the stack, via Spring Cache);
  delivery addresses repeat, and this is the issue's recommended mitigation.
- Configuration in `application.properties`: `geocoding.nominatim.base-url`,
  `geocoding.user-agent`, `geocoding.rate-limit-per-second`, plus an enable toggle.

### 2. Coordinate source — **always server-side geocode**

- No latitude/longitude fields are added to request DTOs.
- Business registration/update geocodes the `address`.
- Order delivery geocodes the `deliveryAddress`.
- Order pickup is derived from the business location (now guaranteed present).

### 3. Failure handling — **reject with a clear error**

- If geocoding yields no result and no coordinates are available, fail the
  operation with a clear `4xx` error. A business or order is never persisted
  without real coordinates. No random/placeholder data is ever stored.

### 4. Business coordinates become mandatory

- Registration/update: geocode `address`; on failure throw a clear `400`.
- Entity: `Business.latitude` / `longitude` become `@Column(nullable = false)`.
- A new migration adds the `NOT NULL` constraint at the DB level — shipped **only
  after** the backfill has populated real data.

### 5. Order creation

- Pickup ← business coordinates (defensive throw if somehow null).
- Delivery ← geocode `deliveryAddress`; reject creation on failure.
- Remove the random block and the `// TODO(#19)` from `createOrder()`.

### 6. Backfill — **idempotent startup runner**

- An `ApplicationRunner` finds businesses with null coordinates, geocodes their
  `address` at ≤1 req/s, and saves them. The same pass re-geocodes the random
  `orders` rows. It is idempotent, logged, and safe to re-run.
- Flyway SQL cannot call Nominatim, so the backfill lives in Java rather than in a
  migration.

### 7. Migrations

- `V18` is already applied and its checksum is immutable, so it is **not** edited;
  a new migration supersedes its random values where needed.
- The `NOT NULL` migration ships only after the backfill runner has verified real
  data is present.

## Rollout phasing

1. Geocoding service + config + tests (no behavior change).
2. Backfill runner; run it; verify real coordinates landed.
3. Wire pickup/delivery into `createOrder()`, remove random, enforce on registration.
4. `NOT NULL` migration + registration/update enforcement.

## Consequences

**Positive**

- Coordinates reflect real locations; the map shows truthful data.
- Deterministic — no more values that change between reads.
- A single source of truth (`GeocodingService`) for all coordinate resolution.
- The mandatory non-null constraint is enforced at validation **and** DB level.

**Negative / trade-offs**

- Runtime dependency on Nominatim for registration and order creation. Mitigated
  by Redis caching and confined to address-changing operations.
- Registration/order creation can now fail if an address cannot be geocoded;
  surfaced as a clear `4xx` so the client can correct the address.
- The public Nominatim instance caps at 1 req/s and forbids bulk geocoding; the
  throttle keeps us compliant while volume is low.

**Future / revisit when volume grows**

- Self-host Nominatim, or move to a paid provider (Google Maps, Mapbox, OpenCage,
  HERE, LocationIQ) for higher throughput and SLAs.
- Consider re-introducing a client-supplied-coordinates path (e.g. a frontend map
  picker) to reduce geocoding load if needed.

## Alternatives considered

- **Client-supplied coordinates (with geocode fallback / only):** rejected for now
  in favor of a single server-side source of truth; can be revisited as a
  throughput mitigation.
- **Allow null coordinates and resolve asynchronously:** rejected — it weakens the
  mandatory constraint and needs a retry/queue mechanism.
- **Flyway Java migration for backfill:** rejected — network calls inside the
  migration pipeline are riskier and harder to retry than an idempotent runner.
