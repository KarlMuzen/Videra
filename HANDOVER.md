# HANDOVER.md

## Current Branch
feature/web-addon-client

## Completed Task
Phase 3 — Web-Addon Data Contract & Failover Client.

Implemented:
- Created `feature/web-addon-client` from the merged `main` branch.
- Added `data/remote/dto/AddonDtos.kt`.
- Added `MediaTypeDto` enum with:
  - `MOVIE`
  - `SERIES`
  - `ANIME`
  - `MICRO_DRAMA`
- Added `AddonManifestDto`:
  - `id`
  - `name`
  - `version`
  - `supportedTypes`
- Added `MediaItemDto`:
  - `id`
  - `title`
  - `posterUrl`
  - `bannerUrl`
  - `type`
- Added `StreamDto`:
  - `url`
  - `quality`
  - `subtitles`
- Added `data/remote/api/VideraAddonApi.kt`.
- All Retrofit endpoints use absolute dynamic `@Url` values.
- Added manifest, media-item, and stream endpoint methods.
- Added a small Retrofit factory configured with Kotlinx Serialization and `ignoreUnknownKeys`.
- Added `data/remote/network/FailoverInterceptor.kt`.
- Failover starts with the primary absolute URL.
- On network `IOException`, the interceptor silently tries each configured backup URL.
- Retryable HTTP statuses are `408`, `429`, and all `5xx` responses, including `503`.
- Non-retryable responses are returned immediately.
- The original request method, headers, body, path, and query are preserved; backup endpoints replace only scheme/host/port.
- Exhausting all attempts throws one final `IOException` with the last failure context.
- Added an explicit OkHttp `4.12.0` dependency, matching the OkHttp version Retrofit 3.0.0 was upgraded to.

## Current Bugs
No known contract or interceptor logic bug identified.

Known boundary:
- `FailoverInterceptor` treats configured backup URLs as alternate origins. It preserves the original request path/query and replaces scheme, host, and port.
- Therefore primary and backup add-on endpoints should expose the same path structure. A future version can introduce explicit base-path mapping if the ecosystem requires mirrors with different path prefixes.
- The Retrofit factory uses `https://videra.invalid/` only as Retrofit's required placeholder base URL because all API methods use absolute `@Url` requests. The placeholder is never contacted by these API methods.
- No real add-on provider or scraper has been added.
- A local Android Gradle build has not been executed in this session.

## Next Immediate Step
Phase 4 — Add-on Repository, Endpoint Management & MVVM Integration.

Use this exact prompt:

"Act as an Expert Android Architect.

Continue Project Videra following BLUEPRINT.md and the latest HANDOVER.md. I am developing from my mobile phone, so output complete, copy-ready files only.

Phase 3 has been implemented on feature/web-addon-client. Before starting Phase 4, assume the Phase 3 PR has been reviewed and merged into main.

Git workflow:
- Create and switch to feature/addon-repository from main.
- Maintain the strict rule: 1 feature = 1 branch = 1 PR.
- Do not mix navigation, player, or unrelated UI work into this branch.

Architecture constraints:
- Dumb Frontend, Smart API.
- Videra contains NO scrapers and NO service-specific extraction logic.
- Add-ons are user-provided remote JSON APIs.
- Kotlin 100%.
- Jetpack Compose only.
- MVVM.
- Coroutines.
- Retrofit + Kotlinx Serialization.
- Coil 3.
- Room.
- Media3 / ExoPlayer.
- No Hilt/Dagger.
- No Gson or Moshi.
- Do not introduce real copyrighted catalog titles or pirate URLs.
- Use Big Buck Bunny or clearly synthetic dummy data only.

Current Phase 3 contract already exists:
- AddonManifestDto
- MediaTypeDto
- MediaItemDto
- StreamDto
- VideraAddonApi with absolute @Url methods
- FailoverInterceptor

Phase 4 goals:
1. Build the domain model layer from the Phase 3 DTOs without leaking Retrofit DTOs into Compose.
2. Create an AddonRepository that can load a manifest and catalog/media data through VideraAddonApi.
3. Add user-installable add-on endpoint configuration using Room for persistence.
4. Model add-on states such as loading, success, empty, unavailable, and parse/network error.
5. Integrate failover cleanly so one broken endpoint does not break the rest of the configured add-ons.
6. Expose repository state through a ViewModel using StateFlow.
7. Keep the navigation shell unchanged except for the minimum wiring required to prove the data layer works.
8. Provide unit-testable repository and failover boundaries with safe dummy data.
9. Do not implement a real provider integration.

Before writing code, inspect the existing Phase 3 files on main and preserve their public contract unless a concrete compile or architectural defect requires a small correction.

At the end, provide the updated HANDOVER.md block for the next session."
