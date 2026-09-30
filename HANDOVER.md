# HANDOVER.md

## Current Branch
feature/shorts-feed

## Completed Task
Phase 7 — Shorts / Vertical Feed.

Implemented:
- Created `feature/shorts-feed` from the reconciled Phase 6 `main`.
- Added domain-level `GetShortsFeedUseCase`.
- Added parallel add-on fetching using `async` + `awaitAll` inside `supervisorScope`.
- Filters fetched domain media to `MediaType.MICRO_DRAMA` only.
- Preserves successful Shorts content when another installed add-on fails.
- Records per-add-on failures for partial-failure UI.
- Returns a global error only when all installed add-ons fail.
- Added `ShortsFeed` and `ShortsAddonFailure` domain models.
- Added `ShortsViewModel` with UI-safe `StateFlow<ShortsUiState>`.
- Shorts UI states:
  - Loading
  - Empty
  - Success
  - Error
- Added manual `ShortsViewModelFactory`.
- Added `ShortsScreen` with:
  - Compose `VerticalPager`;
  - full-screen immersive content;
  - Coil 3 `AsyncImage` for user-provided artwork;
  - refresh action;
  - partial add-on failure display;
  - empty state;
  - error state;
  - safe fallback when an item has no artwork.
- Updated `MainScaffold` only to:
  - create/inject `ShortsViewModel`;
  - render `ShortsScreen` in the existing Shorts destination;
  - let Shorts use the full Scaffold content area so the pager can extend behind the existing bottom navigation layer.
- Existing five top-level navigation destinations remain unchanged.
- Existing global mini-player / Now Playing slot remains unchanged.
- Settings and Home behavior were not changed.
- No provider-specific networking, scraping, player implementation, or new paging/network framework was added.
- No real copyrighted titles or pirate URLs were introduced.

## Current Bugs
No known Phase 7 static code defect remains after review.

Known product boundary:
- Shorts content is currently artwork + metadata only.
- Playback is intentionally deferred to Phase 8 Media3 integration.
- An add-on must return media items whose domain `type` is `MICRO_DRAMA` for them to appear in Shorts.
- An add-on that returns no MICRO_DRAMA items is ignored for content purposes; its network failure is still surfaced when applicable.

## Pull Request
PR #6:
- `feature/shorts-feed` -> `main`
- Open, not merged.

## Git Workflow Rule
- `main` is the permanent integration branch.
- One feature branch per PR.
- At most one additional supporting branch besides `main`, only when genuinely required.
- No unrelated feature branches.
- No mixing independent features into one PR.

## CI Status
The existing PR Debug APK workflow is triggered for this PR.
Final CI status must be checked from the latest PR head after GitHub Actions completes.
Do not call Phase 7 build-verified until that run succeeds.

## Next Immediate Step
Phase 8 — Media3 Integration.

Use this exact prompt:

"Act as an Expert Android Architect.

Continue Project Videra following `BLUEPRINT.md` and the latest `HANDOVER.md`. I am developing from my mobile phone, so output complete, copy-ready files only.

Phase 7 has been implemented on `feature/shorts-feed`. Before starting Phase 8, assume PR #6 has been reviewed and merged into `main`.

Git workflow:
- Create and switch to `feature/media3-player` from `main`.
- Maintain the strict rule: 1 feature = 1 branch = 1 PR.
- At most one extra supporting branch beyond `main`, only if genuinely required.
- Do not mix unrelated Home, Shorts, or Settings features into this branch.
- Automatically open one PR to `main` on completion.

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
- Use clearly synthetic/dummy data or Big Buck Bunny only.

Existing components:
- AddonManifestDto
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
- GetShortsFeedUseCase
- ShortsViewModel
- ShortsViewModelFactory
- ShortsScreen

Phase 8 goals:
1. Integrate AndroidX Media3 / ExoPlayer for user-provided stream playback.
2. Keep playback orchestration behind a ViewModel/use-case boundary rather than embedding business logic in Composables.
3. Expose UI-safe player state through StateFlow.
4. Support a selected `Stream` from an existing `MediaItem` without adding provider-specific extraction.
5. Build a reusable Compose player surface using Media3 UI components.
6. Preserve the global mini-player / Now Playing slot architecture.
7. Avoid introducing a second player engine or unnecessary playback framework.
8. Handle playback errors, loading, buffering, and release lifecycle correctly.
9. Keep Home, Shorts, and Settings behavior unchanged except for the minimum wiring needed to launch playback.
10. Use only safe synthetic streams or Big Buck Bunny for development/testing until a real user add-on supplies a stream.
11. Inspect the merged Phase 7 implementation on `main` before coding and preserve existing public contracts unless a concrete compile or architectural defect requires correction.
12. Add focused testable boundaries where practical.

At the end, provide the updated `HANDOVER.md` block for the next session."