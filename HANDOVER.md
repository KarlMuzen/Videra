# HANDOVER.md

## Current Branch
feature/micro-drama-episodes

## Completed Task
Phase 11 — Micro-drama episodes, episode queueing, Shorts episode controls, and watch progress.

Implemented:
- Extended `MediaItem` and `MediaItemDto` with `episodes`.
- Added `Episode` / `EpisodeDto` with `number`, `title`, `streams`, and optional `durationSeconds`.
- Existing stream payloads continue to map through the same Retrofit + Kotlinx Serialization remote boundary.
- `MICRO_DRAMA` items with no explicit episodes receive five synthetic development episodes using the approved Big Buck Bunny test stream.
- Synthetic development episodes use numbered titles (`Episode 1` through `Episode 5`) and a 60-second development duration.
- Added `EpisodeProgressEntity`, `EpisodeProgressDao`, `EpisodeProgressRepository`, and its implementation.
- Expanded Room from version 4 to version 5 with an explicit `4 -> 5` migration creating `episode_progress`.
- Progress is keyed by `addonId::mediaId` and stores last watched episode number, playback position, and update time.
- Extended the single global `PlayerController` with an internal episode queue.
- `PlayerState` now exposes `currentEpisodeIndex`, `totalEpisodeCount`, and `currentPositionMs`.
- `PlayerController` detects `Player.STATE_ENDED`, emits an episode-completed event, and automatically starts the next episode when one exists.
- Previous/next episode playback remains inside the same global `ExoPlayer`; no second player or playlist engine was introduced.
- `PlayerViewModel` automatically uses episode-aware playback when a media item contains episodes and exposes episode control methods.
- `ShortsViewModel` now receives the existing global `PlayerController` plus `EpisodeProgressRepository` through the manual factory path.
- `ShortsViewModel` restores the saved episode and saved position, persists progress on episode completion and pause, and saves progress before manual episode switches.
- Replacing the current Shorts item cancels the previous selection job before loading the new item's saved progress.
- `ShortsFeed` now retains the source add-on alongside each Shorts media item so the overlay can display the add-on name and progress can be scoped correctly.
- Reworked `ShortsScreen` to full-bleed video with an overlaid title/add-on block, previous/next controls, and an `EP. X / N` episode pill.
- The episode pill opens a `ModalBottomSheet` grid of all available episodes.
- Selecting an episode immediately starts it through the same global player and closes the sheet.
- Home, Search, Library, Settings, Add-on Manager, and the global Player remain intact.
- No scrapers, provider-specific extraction logic, XML, Hilt/Dagger, Gson, Moshi, or new networking libraries were added.

## Room Database
Current schema:
- Version: 5
- Entities:
  - `InstalledAddonEntity`
  - `LibraryEntity`
  - `EpisodeProgressEntity`
- Migrations:
  - 1 -> 2: adds `installed_addons.mediaItemsUrl`
  - 2 -> 3: creates `library_items`
  - 3 -> 4: adds manifest URL, custom name, enabled state, and cached manifest metadata
  - 4 -> 5: creates `episode_progress`
- Migration 4 -> 5 is explicit and registered together with all previous migrations.
- Episode progress uses a stable composite key in one table instead of changing library snapshot semantics.

## Episode / Playback Semantics
- Explicit episode arrays are sorted by episode number before entering the domain model.
- A `MICRO_DRAMA` without explicit episodes gets five synthetic development episodes.
- Synthetic episode streams use:
  `https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4`
- Episode-aware playback uses one Media3 `ExoPlayer` and swaps the current media item on each episode transition.
- Auto-advance occurs only when the active media has an episode queue.
- The player emits `EpisodeCompleted` before moving to the next episode so the Shorts ViewModel can persist the completed position.
- Saved progress restores both episode number and `positionMs` when the Shorts item is selected.
- `PlayerState.currentEpisodeIndex` is zero-based for internal state; the Shorts UI displays one-based episode numbers.
- `currentPositionMs` is updated whenever player state synchronization occurs and is persisted on pause/final completion.

## Reactive Add-on Boundary
- `AddonRepository.observeEnabledAddons()` remains the sole active-add-on source for Home, Search, and Shorts.
- Add-on install/toggle/remove behavior from Phase 10 remains intact.
- Episode support stays entirely after the remote JSON mapping boundary; Videra performs no provider scraping.

## Compose / ViewModel Verification
- `ShortsScreen` uses lifecycle-aware `StateFlow` collection.
- `ShortsViewModel` uses `viewModelScope` for feed loading, progress collection, and episode selection.
- Selection jobs are cancelled before starting a new media/episode selection.
- `CancellationException` is rethrown by persistence operations.
- `AddonManagerViewModel`, `ShortsViewModel`, and `PlayerViewModel` continue to use manual `ViewModelProvider.Factory` implementations.
- Scoped `Modifier.weight(1f)` is used without importing the internal Compose `weight` symbol.
- `ModalBottomSheet`, `LazyVerticalGrid`, `Surface(onClick = ...)`, and `safeDrawingPadding()` are used within their parent Compose scopes.
- The full-bleed player remains behind the overlay instead of creating a player per pager page.

## CI Workflow
- `.github/workflows/pr-debug-build.yml` remains unchanged.
- Exact build command:
  `./gradlew assembleDebug -x lint -x test`
- CI intentionally excludes lint and tests from the PR debug workflow.
- Local container Gradle validation is unavailable because outbound GitHub DNS/network access is unavailable; GitHub Actions is the authoritative build validation.

## Phase 10 / Main Baseline
- Phase 10 / PR #9 is merged into `main`.
- Current main baseline:
  `3e20d3bf02e0bc7431d7c4caca84a6b0afefe310`
- Room database baseline is version 4.
- Add-on Manager remains available under Settings.
- Single global Media3 player remains app-scoped with one `ExoPlayer`.
- `android:usesCleartextTraffic="true"` remains enabled for development.

## Documentation Note
A recursive GitHub tree check of the repository previously found no `BLUEPRINT.md` in the live tree. Phase 11 was implemented against the latest available `HANDOVER.md` and the live source architecture without inventing missing Blueprint requirements.

## Pull Request
PR #10:
- `feature/micro-drama-episodes` -> `main`
- One feature branch and one PR for Phase 11.
- No supporting branch was created.
- PR is open.
- Final merge should occur only after the newest PR Debug APK run on the final branch head succeeds.

## Git Workflow Rule
- `main` is the permanent integration branch.
- One feature branch per PR.
- No unrelated feature branches.
- No mixing independent features into one PR.

## Next Immediate Step
Verify the final GitHub Actions run for the current branch head, then review and merge PR #10. After merge, continue with the next Blueprint-defined phase while preserving the remote JSON add-on boundary, Room persistence, single-player architecture, and lifecycle-safe StateFlow patterns.

