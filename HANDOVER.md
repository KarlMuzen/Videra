# HANDOVER.md

## Current Branch
feature/home-aggregator

## Completed Task
Phase 6 — Home Aggregator Feed.

Implemented:
- Created `feature/home-aggregator` directly from merged Phase 5 `main`.
- Extended the add-on manifest contract with optional `mediaItemsUrl`.
- Extended domain `AddonManifest` and `InstalledAddon` models with optional `mediaItemsUrl`.
- Extended `InstalledAddonEntity` with nullable `mediaItemsUrl`.
- Added Room migration `1 -> 2` for the new nullable media endpoint column.
- Updated `AddonRepositoryImpl` to:
  - validate add-on URLs;
  - resolve relative `mediaItemsUrl` values against the manifest URL;
  - persist the resolved media endpoint;
  - expose `getMediaItems(InstalledAddon)`;
  - map media DTOs into domain models;
  - preserve coroutine cancellation.
- Added `GetHomeFeedUseCase` as the domain aggregation boundary.
- Aggregation behavior:
  - reads all installed add-ons;
  - launches one `async` request per add-on;
  - uses `supervisorScope` so one failed add-on does not cancel successful siblings;
  - keeps successful non-empty sections;
  - records per-add-on failures for partial-failure UI;
  - returns a global error only when every installed add-on fails.
- Added `HomeViewModel` with UI-safe `StateFlow<HomeUiState>`.
- Home states:
  - Loading
  - Empty
  - Success
  - Error
- Home automatically refreshes when installed add-ons change and also supports manual refresh.
- Added manual `HomeViewModelFactory`.
- Added native Material 3 `HomeScreen`:
  - top-level Home header;
  - refresh action;
  - partial-failure warning;
  - `LazyColumn` containing one `LazyRow` per installed add-on;
  - media cards;
  - Coil 3 `AsyncImage`;
  - empty/error/loading states.
- Updated `MainScaffold` only enough to inject and render `HomeViewModel` for the existing Home destination.
- Preserved the existing five-tab navigation and global mini-player slot.
- No playback/player implementation, scraper, provider-specific extraction, or real catalog integration was added.
- No Room entities or Retrofit DTOs are exposed to Compose.

## Current Bugs / Known Boundary
No known Phase 6 static code defect remains after diff review.

Important compatibility boundary:
- The Phase 3 manifest contract did not originally define a catalog endpoint.
- Phase 6 therefore adds optional `mediaItemsUrl` to the manifest contract and persists it through Room migration 1 -> 2.
- Existing installed add-ons created before this migration will have a null `mediaItemsUrl`. They need to be reinstalled from a manifest that supplies `mediaItemsUrl` before Home can fetch their media catalog.
- Home does not invent provider-specific endpoint paths.

## Pull Request
PR #5:
- `feature/home-aggregator` -> `main`
- Open, not merged.

## Git Workflow Rule
- `main` is the permanent integration branch.
- One feature branch per PR.
- At most one additional supporting branch besides `main`, only when genuinely required.
- No unrelated feature branches.
- No mixing independent features into one PR.

## CI Status
The PR workflow is triggered by the repository's existing PR Debug APK workflow.
The latest run must be checked after the final PR head is created before treating Phase 6 as build-verified.

## Next Immediate Step
Phase 7 — The Shorts / Vertical Feed.

Use this exact prompt:

"Act as an Expert Android Architect.

Continue Project Videra following `BLUEPRINT.md` and the latest `HANDOVER.md`. I am developing from my mobile phone, so output complete, copy-ready files only.

Phase 6 has been implemented on `feature/home-aggregator`. Before starting Phase 7, assume PR #5 has been reviewed and merged into `main`.

Git workflow:
- Create and switch to `feature/shorts-feed` from `main`.
- Maintain the strict rule: 1 feature = 1 branch = 1 PR.
- At most one extra supporting branch beyond `main`, only if genuinely required.
- Do not mix player implementation or unrelated settings/home work into this branch.

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

Existing components:
- AddonManifestDto with optional `mediaItemsUrl`
- MediaTypeDto
- MediaItemDto
- StreamDto
- VideraAddonApi
- FailoverInterceptor
- Domain models and mapper extensions
- InstalledAddonEntity
- AddonDao
- VideraDatabase version 2
- Resource<T>
- AddonRepository / AddonRepositoryImpl
- SettingsViewModel
- SettingsViewModelFactory
- SettingsScreen
- VideraAppContainer
- GetHomeFeedUseCase
- HomeViewModel
- HomeViewModelFactory
- HomeScreen

Phase 7 goals:
1. Build the Shorts / vertical feed on top of installed user add-ons.
2. Reuse the existing repository/domain boundaries instead of adding provider-specific networking to Compose.
3. Expose a UI-safe StateFlow for vertical-feed loading, content, empty, and error states.
4. Fetch content across eligible installed add-ons in parallel where appropriate.
5. Handle partial add-on failures without crashing or blanking the entire feed when successful content exists.
6. Create a Compose vertical paging/feed experience using native Material 3 components and Coil 3.
7. Keep the global mini-player slot and top-level navigation unchanged.
8. Do not modify Settings behavior except for unavoidable shared repository/domain changes.
9. Avoid introducing a new paging/networking framework unless a concrete requirement justifies it.
10. Use only safe synthetic/dummy content until a real user-provided add-on supplies content.
11. Add focused unit-testable boundaries where practical.
12. Inspect the merged Phase 6 implementation on `main` before coding and preserve existing public contracts unless a concrete compile or architectural defect requires correction.

At the end, provide the updated `HANDOVER.md` block for the next session."