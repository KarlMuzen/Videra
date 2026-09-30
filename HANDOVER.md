# HANDOVER.md

## Current Branch
feature/addon-manager-ui

## Completed Task
Phase 5 — Add-on Manager MVVM & Settings UI.

Implemented:
- Created `feature/addon-manager-ui` directly from merged Phase 4 `main`.
- Added `SettingsViewModel` with:
  - `StateFlow<List<InstalledAddon>>` for installed add-ons.
  - `SettingsUiState` for installation progress, success, and error feedback.
  - `SettingsEvent.InstallAddon(url)`.
  - `SettingsEvent.RemoveAddon(id)`.
  - HTTP/HTTPS URL validation before repository access.
  - User-facing handling for DNS, timeout, HTTP, and invalid JSON failures.
  - Coroutine cancellation rethrow.
- Added manual `SettingsViewModelFactory` for repository injection.
- Added native Material 3 `SettingsScreen`:
  - Add-on URL TextField.
  - Install Button.
  - CircularProgressIndicator during installation.
  - Inline success/error messages.
  - Installed add-on LazyColumn.
  - Name, status, version, and base URL display.
  - Remove IconButton.
  - Empty state.
- Added `VideraAppContainer` for manual construction of:
  - Room `VideraDatabase`.
  - `VideraAddonApi`.
  - `AddonRepositoryImpl`.
- Wired the Settings destination in `MainScaffold` without changing the five top-level destinations or global mini-player slot.
- Updated `MainActivity` to provide the repository through the app container.
- Added `androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0` for Compose ViewModel factory access.
- Preserved the Phase 4 Coil network artifact coordinate after diff audit.
- No scraper, provider-specific extraction, playback, or real catalog integration was introduced.

## Current Bugs
No known Phase 5 code defect has been observed from static diff review.

CI boundary:
- PR #4 was opened against `main`.
- GitHub Actions run #7 for the initial PR head was observed in progress.
- This handover update changes the PR head, so the latest run must be checked after this commit. Do not treat an older run as final build verification.

## Pull Request
PR #4:
- `feature/addon-manager-ui` → `main`
- Open, not merged.

## Git Workflow Rule
- `main` is the permanent integration branch.
- One feature branch per PR.
- At most one additional supporting branch besides `main`, only when genuinely required.
- No unrelated feature branches.
- No mixing independent features into one PR.

## Next Immediate Step
Phase 6 — The Home Aggregator Feed.

Use this exact prompt:

"Act as an Expert Android Architect.

Continue Project Videra following `BLUEPRINT.md` and the latest `HANDOVER.md`. I am developing from my mobile phone, so output complete, copy-ready files only.

Phase 5 has been implemented on `feature/addon-manager-ui`. Before starting Phase 6, assume PR #4 has been reviewed and merged into `main`.

Git workflow:
- Create and switch to `feature/home-aggregator` from `main`.
- Maintain the strict rule: 1 feature = 1 branch = 1 PR.
- At most one extra supporting branch beyond `main`, only if genuinely required.
- Do not mix playback/player implementation or unrelated settings work into this branch.

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

Existing Phase 3/4/5 components:
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
- SettingsViewModel
- SettingsViewModelFactory
- SettingsScreen
- VideraAppContainer

Phase 6 goals:
1. Build the Home Aggregator Feed on top of installed user add-ons.
2. Keep all provider/network logic behind AddonRepository or a new domain-level aggregation contract.
3. Expose a UI-safe StateFlow for Home feed loading, success, empty, and error states.
4. Aggregate results across multiple installed add-ons without leaking Retrofit DTOs or Room entities into Compose.
5. Handle one failed add-on without taking down the whole feed where practical.
6. Create the Home Compose screen using native Material 3 components and Coil 3 for images.
7. Use only safe synthetic/dummy content until a real user-provided add-on supplies content.
8. Preserve the existing bottom navigation and global mini-player slot.
9. Keep Settings/Add-on Manager behavior unchanged except for shared repository wiring required by the aggregator.
10. Add focused unit-testable aggregation boundaries where practical.
11. Inspect the merged Phase 5 implementation on `main` before coding and preserve existing public contracts unless a concrete defect requires correction.

At the end, provide the updated `HANDOVER.md` block for the next session."