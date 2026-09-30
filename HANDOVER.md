# HANDOVER.md

## Current Branch
feature/search-library

## Completed Task
Phase 9 — Local Search and persistent Library.

Implemented:
- Added local catalog search across all currently installed add-ons.
- Search catalog loading runs each installed add-on fetch in parallel inside `supervisorScope`; one add-on failure does not discard successful catalogs.
- The search use-case loads the add-on media catalogs; `SearchViewModel` filters the loaded catalog locally by title as the user query changes, avoiding a network request for every keystroke.
- Added `SearchResult`, `SearchAddonFailure`, and `SearchCatalog` domain models.
- Added `SearchViewModel` with `StateFlow` UI state, dedicated query state, refresh support, and a manual `SearchViewModelFactory`.
- Added Compose `SearchScreen` with a search `TextField`, adaptive result grid, Coil 3 artwork, empty/error states, refresh, partial add-on failure display, and bookmark controls.
- Search result selection launches playback through the existing global `PlayerViewModel` / `PlayerController`.
- Search result cards size to their adaptive grid cell so narrow phone widths do not overflow.
- Added `LibraryEntity` and `LibraryDao` for durable bookmarked media snapshots.
- Expanded `VideraDatabase` from version 2 to version 3 with an explicit 2->3 Room migration creating `library_items`.
- Preserved the existing 1->2 migration for `installed_addons.mediaItemsUrl`.
- Added `LibraryRepository` and `LibraryRepositoryImpl`.
- Library saves use a stable key of `addonId::mediaId`, persist media metadata and the first available stream URL/quality, and retain insertion/update time.
- Added `LibraryItem` domain model and entity-to-domain mapping.
- Added `LibraryViewModel` with `StateFlow` loading/empty/success states and manual `LibraryViewModelFactory`.
- Added Compose `LibraryScreen` with an adaptive saved-media grid, Coil 3 artwork, playback selection, and remove controls.
- Injected `LibraryRepository` through `VideraAppContainer`.
- Wired Search and Library into the existing bottom navigation destinations.
- Search and Library playback commands reuse the single global Media3 player; no second player was introduced.
- Existing Home, Shorts, Player, and Settings routes were preserved.
- No scrapers, provider-specific extraction logic, or new dependencies were added.
- CI workflow remains unchanged and uses exactly:
  `./gradlew assembleDebug -x lint -x test`

## Room Database
Current schema:
- Version: 3
- Entities:
  - `InstalledAddonEntity`
  - `LibraryEntity`
- Migrations:
  - 1 -> 2: adds `installed_addons.mediaItemsUrl`
  - 2 -> 3: creates `library_items`
- Database creation uses the application context and registers both migrations explicitly.
- Library bookmarks survive app restarts because data is stored in Room.
- Saved library media is a metadata snapshot. When a remote stream was available at save time, its first URL and quality are stored for later playback. Additional streams/subtitles are not persisted in this phase.

## Search Semantics
- The repository has no provider-specific search/extraction layer; add-ons remain remote JSON sources.
- All currently installed add-ons are treated as active because the existing `InstalledAddon` model has no enabled/disabled field.
- The search catalog is fetched from each add-on's existing `mediaItemsUrl` endpoint through `AddonRepository`.
- Title matching is case-insensitive and ignores leading/trailing query whitespace.
- Blank query shows the search hint instead of returning the entire catalog.
- Successful add-on responses are retained when another add-on fails.
- If every installed add-on fails, the Search UI exposes the overall error state.

## Compose / ViewModel Verification
- Search and Library use `collectAsStateWithLifecycle()`.
- ViewModels use `viewModelScope` only for lifecycle-bound work.
- CancellationException is rethrown instead of being swallowed.
- `SearchViewModelFactory` and `LibraryViewModelFactory` use the same manual factory pattern already used by Home, Shorts, Settings, and Player.
- `ColumnScope.weight()` is used without importing the scoped `weight` symbol; an explicit import caused CI failure under the current Compose API because it resolved to an internal symbol.
- Search and Library cards use `fillMaxWidth()` inside adaptive grid cells rather than fixed widths.
- Coil 3 continues to use `AsyncImage`.
- No XML or Hilt/Dagger was added.

## CI Validation
The first candidate build exposed real Compose defects and was corrected before completion:
1. Run #30 was superseded by subsequent commits.
2. Run #34 failed during Kotlin compilation because `androidx.compose.foundation.layout.weight` was explicitly imported in Search/Library files; current Compose resolves that import to an internal symbol.
3. Removed the explicit `weight` imports and kept the valid scoped `Modifier.weight(1f)` calls inside the parent `Column` content.
4. Run #35 completed successfully.
5. Run #35 validated the source head:
   `7bf9ccda2bf7c6a7f934fb926f12c12096bc43b8`
6. `Assemble Debug` succeeded.
7. The workflow command remained exactly:
   `./gradlew assembleDebug -x lint -x test`
8. Lint and tests are explicitly excluded by the workflow, so this phase has not been separately lint/test validated by CI.

## Phase 8 Baseline
Phase 8 / PR #7 is merged into `main`.
- Merge commit / current main baseline:
  `d1e9040486445c38244ad969c98e4e0b203742f0`
- Global Media3 player remains app-scoped with one `ExoPlayer`.
- Big Buck Bunny fallback is the official ExoPlayer test stream:
  `https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4`
- In-app playback diagnostics remain available for device debugging.
- `android:usesCleartextTraffic="true"` remains enabled for development.
- Physical-device validation remains required for real add-on stream behavior and decoder/network differences.

## Documentation Note
A recursive GitHub tree check of current `main` found `HANDOVER.md` but no `BLUEPRINT.md` anywhere in the repository. Phase 9 was therefore implemented against the latest repository architecture available in `HANDOVER.md` and the existing source tree without inventing missing Blueprint requirements.

## Pull Request
PR #8:
- `feature/search-library` -> `main`
- Open, not merged.
- Current branch head:
  `707e4e9bb0aba7ae38d6dbab37d42441708517e4`
- Last source-code validation head:
  `7bf9ccda2bf7c6a7f934fb926f12c12096bc43b8`
- Current `main`:
  `d1e9040486445c38244ad969c98e4e0b203742f0`
- Feature branch is 0 commits behind `main`.
- One PR only for this Phase 9 feature branch.
- No supporting branch was created.

## Git Workflow Rule
- `main` is the permanent integration branch.
- One feature branch per PR.
- No unrelated feature branches.
- No mixing independent features into one PR.

## Next Immediate Step
After PR #8 is reviewed and merged, continue to the next Blueprint-defined phase while preserving the single-player architecture, remote add-on JSON boundary, Room persistence, and lifecycle-safe StateFlow patterns.
