# HANDOVER.md

## Current Branch
feature/addon-repository

## Completed Task
Phase 4 — Add-on Repository, Domain Mapping, Room Persistence & PR Debug APK CI.

Implemented:
- Created `feature/addon-repository` from the Phase 3 `main`.
- Added clean domain models:
  - `MediaType`
  - `AddonManifest`
  - `MediaItem`
  - `Stream`
  - `InstalledAddon`
- Added DTO-to-domain mapper extensions for all Phase 3 remote DTOs.
- Added `InstalledAddonEntity` for Room persistence:
  - `id`
  - `name`
  - `baseUrl`
  - `version`
- Added `AddonDao` with:
  - insert/upsert
  - delete by id
  - `Flow<List<InstalledAddonEntity>>` observation
- Added `VideraDatabase` Room database and manual `create(context)` factory.
- Added `Resource<T>` sealed interface:
  - `Loading`
  - `Success<T>`
  - `Error`
- Added `AddonRepository` interface.
- Added `AddonRepositoryImpl`.
- Repository installation flow:
  1. Validate base/manifest URLs.
  2. Fetch `AddonManifestDto` through `VideraAddonApi`.
  3. Map DTO to domain.
  4. Persist the installed add-on endpoint metadata through `AddonDao`.
  5. Return `Resource.Success`.
- Cancellation is rethrown so coroutine cancellation is not converted into a normal repository error.
- Added minimal root Gradle project scaffolding required for CI:
  - `build.gradle.kts`
  - `settings.gradle.kts`
  - `gradle.properties`
  - Android manifest retained from the Phase 1 contract.
- Added `.github/workflows/pr-debug-build.yml`.
- CI triggers only for `pull_request` events targeting `main`, on `opened`, `reopened`, and `synchronize`.
- CI uses:
  - `actions/checkout@v6`
  - `actions/setup-java@v5`
  - JDK 17 / Temurin
  - `gradle/actions/setup-gradle@v6`
  - Gradle 9.5.0
  - `assembleDebug`
  - `actions/upload-artifact@v4`
- Debug APK artifact name: `videra-debug-apk`.
- Artifact retention: 7 days.
- No navigation or player UI work was introduced.
- No scraper, provider-specific extractor, or real catalog integration was introduced.

## Current Bugs
No known Phase 4 domain, Room, or repository logic defects.

Known repository/build boundary:
- The repository did not contain root Gradle settings or a Gradle wrapper in the GitHub state inspected before Phase 4. Minimal root Gradle files were therefore added so the requested CI job has an executable project structure.
- CI intentionally uses an installed Gradle distribution through `setup-gradle` rather than `./gradlew`, because the repository did not contain a Gradle wrapper. Gradle's official action supports this configuration through its `gradle-version` input. citeturn196047search0turn196047search1
- AGP 9.3.x requires Gradle 9.5.0, so the workflow pins Gradle 9.5.0. citeturn464218search0turn464218search1
- A workflow run has not been observed for the latest PR head yet. CI status must therefore be verified from the GitHub Actions run before treating the PR as build-verified.

## Pull Request
PR #3:
- `feature/addon-repository` → `main`
- Open, not merged.

## Next Immediate Step
Phase 5 — Add-on Manager MVVM & Settings UI.

Use this exact prompt:

"Act as an Expert Android Architect.

Continue Project Videra following BLUEPRINT.md and the latest HANDOVER.md. I am developing from my mobile phone, so output complete, copy-ready files only.

Phase 4 has been implemented on feature/addon-repository. Before starting Phase 5, assume PR #3 has been reviewed and merged into main.

Git workflow:
- Create and switch to `feature/addon-manager` from `main`.
- Maintain the strict rule: 1 feature = 1 branch = 1 PR.
- Do not mix playback/player implementation into this branch.

Architecture constraints:
- Dumb Frontend, Smart API.
- Videra contains NO scrapers and NO service-specific extraction logic.
- Add-ons are user-provided remote JSON endpoints.
- Kotlin 100%.
- Jetpack Compose only.
- MVVM.
- Coroutines + StateFlow.
- Retrofit + Kotlinx Serialization.
- Coil 3.
- Room.
- Media3 / ExoPlayer.
- No Hilt/Dagger.
- No Gson/Moshi.
- No real copyrighted catalog titles or pirate URLs.
- Use clearly synthetic dummy data or Big Buck Bunny only.

Existing Phase 3/4 components:
- AddonManifestDto
- MediaTypeDto
- MediaItemDto
- StreamDto
- VideraAddonApi
- FailoverInterceptor
- Domain models and mapper extensions
- InstalledAddonEntity
- AddonDao
- VideraDatabase
- Resource<T>
- AddonRepository / AddonRepositoryImpl

Phase 5 goals:
1. Build an AddonManagerViewModel using StateFlow.
2. Expose installed add-ons from AddonRepository as UI-safe domain models.
3. Implement install/remove actions through the ViewModel using Resource.Loading/Success/Error.
4. Create a Compose Add-on Manager screen under the existing Settings navigation destination.
5. Provide an input flow for a user-provided add-on manifest URL/base URL.
6. Display installed add-ons with name, version, endpoint status, install success/error, and remove action.
7. Do not leak Room entities or Retrofit DTOs into Compose.
8. Keep networking/repository logic outside the Composables.
9. Add lightweight previews or dummy UI state without real provider content.
10. Keep the global mini-player slot and existing navigation shell unchanged.
11. Add focused unit-testable ViewModel/repository boundaries where practical.

Before writing code, inspect the merged Phase 4 implementation on main and preserve its public contracts unless a concrete compile or architectural defect requires correction.

At the end, provide the updated HANDOVER.md block for the next session."
