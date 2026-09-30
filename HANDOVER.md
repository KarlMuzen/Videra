# HANDOVER.md

## Current Branch
feature/micro-drama-pivot

## Completed Task
Phase 12 — Videra Micro-Drama pivot to a dedicated 9:16 vertical playback platform.

Implemented:
- Removed the `MiniPlayer` composable and deleted `ui/player/MiniPlayer.kt`.
- Removed the mini-player from `MainScaffold`; no mini-player is rendered above bottom navigation.
- Bottom navigation is hidden on immersive vertical player routes so the player can occupy the complete window.
- Replaced the legacy horizontal `PlayerScreen` with an edge-to-edge vertical `VerticalPager` player.
- Reused one shared `ShortsOverlay` composable for both Shorts and the full-screen Player route.
- The shared overlay contains title, add-on name, previous/next episode controls, and the `EP. X / N` episode pill with the same episode grid bottom sheet.
- Home, Search, and Library now expose only `MICRO_DRAMA` content.
- Home now passes the source `InstalledAddon` with each selected media item so episode progress is scoped correctly.
- Search continues to retain the source add-on and now filters the catalog to `MICRO_DRAMA`.
- Library filters legacy non-micro-drama entries out of the active UI.
- Library playback resolves the current installed add-on catalog before starting playback so fresh episode metadata can be used when the saved library snapshot does not contain episode details.
- Library snapshots also receive the five-episode development fallback locally for saved `MICRO_DRAMA` items when an add-on cannot be resolved.
- The single `PlayerViewModel` now owns player selection, episode selection, watch-progress restore, and progress persistence for both Player and Shorts routes.
- Manual DI now injects `AddonRepository`, `EpisodeProgressRepository`, and the single global `PlayerController` into `PlayerViewModel`.
- `PlayerSurface` no longer exposes Media3's built-in playback controller and uses zoom/crop rendering for full-bleed vertical presentation.
- The global `PlayerController` explicitly supports `pauseAndClearVideoSurface()`, `clearVideoSurface()`, and controller-owned resume for player re-entry.
- The navigation lifecycle clears the video surface when leaving the active vertical player routes.
- Activity `ON_STOP` pauses playback and clears the video surface; `ON_DESTROY` releases the single global `ExoPlayer`.
- `PlayerSurface.onRelease` also clears the Media3 video surface before detaching the `PlayerView`.
- `enableEdgeToEdge()` remains active in `MainActivity`, while player overlays use `safeDrawingPadding()` so system gesture areas do not cover controls.
- Home, Search, Library, Settings, Add-on Manager, Room v5, Retrofit/Kotlinx Serialization, and the single global Media3 player architecture remain intact.
- No second player, mini-player, scraper, provider-specific extraction, Hilt/Dagger, XML, Gson, or Moshi was introduced.

## Micro-Drama Playback Semantics

- `MediaItem.episodes` remains the source of episode metadata.
- `MICRO_DRAMA` items without explicit episodes continue to receive five synthetic development episodes.
- Synthetic development episodes use the approved Big Buck Bunny stream:
  `https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4`
- Episode-aware playback remains one Media3 `ExoPlayer`; episode transitions replace the current media item.
- `Player.STATE_ENDED` continues to auto-advance to the next episode.
- Saved progress restores both episode number and playback position.
- `PlayerState.currentEpisodeIndex` is zero-based internally; UI displays one-based episode numbers.
- Progress remains keyed by `addonId::mediaId`.

## Surface and Lifecycle Semantics

- There is still exactly one app-scoped `ExoPlayer`.
- The player surface is attached only by the active vertical player route.
- Leaving Player or Shorts calls `pauseAndClearVideoSurface()` through the navigation disposal effect.
- Leaving the app pauses playback and clears the surface through the activity lifecycle observer.
- `PlayerSurface.onRelease` also clears the Media3 video surface before detaching the `PlayerView`.
- The old horizontal Media3 controller UI is disabled.

## Reactive Add-on Boundary

- `AddonRepository.observeEnabledAddons()` remains the active source for Home, Search, and Shorts.
- Remote media still arrives through Retrofit + Kotlinx Serialization JSON.
- Videra still performs no provider scraping.
- Disabled add-ons do not feed the Micro-Drama surfaces.

## Compose / ViewModel Verification

- `ShortsOverlay` is a `BoxScope` extension, so `Modifier.align(Alignment.BottomCenter)` is resolved inside the correct scope.
- `PlayerPlaybackError` is also a `BoxScope` extension for the same reason.
- `ModalBottomSheet` and `rememberModalBottomSheetState` remain covered by `@OptIn(ExperimentalMaterial3Api::class)` in the shared overlay.
- `VerticalPager`, `PlayerSurface`, overlays, and system-inset handling all use `Modifier.fillMaxSize()` with `safeDrawingPadding()` only on controls that need inset protection.
- `PlayerViewModelFactory` is wired with all required dependencies.
- Progress persistence catches ordinary database failures without breaking playback and always rethrows `CancellationException`.
- The active player is matched by both add-on id and media id, avoiding cross-add-on media id collisions.
- No `MiniPlayer` references remain in `MainScaffold`.

## Room Database

Current schema remains version 5.

Entities:

- `InstalledAddonEntity`
- `LibraryEntity`
- `EpisodeProgressEntity`

Migrations:

- 1 -> 2: adds `installed_addons.mediaItemsUrl`
- 2 -> 3: creates `library_items`
- 3 -> 4: adds manifest URL, custom name, enabled state, and cached manifest metadata
- 4 -> 5: creates `episode_progress`

No new Room migration was required for the Phase 12 pivot.

## CI Workflow

`.github/workflows/pr-debug-build.yml` remains unchanged.

Exact build command:

`./gradlew assembleDebug -x lint -x test`

GitHub Actions remains the authoritative build validation because local Gradle execution is unavailable in the model container.

## Main Baseline

Phase 11 / PR #10 is merged into `main`.

Current main baseline:

`902e85dc7ffdd63f6d7e5f0f414102e7b35e4897`

## Documentation Note

No `BLUEPRINT.md` was found in the live repository tree during the Phase 11 verification. Phase 12 is therefore based on the latest merged source and `HANDOVER.md` without inventing missing Blueprint requirements.

## Pull Request

Phase 12 branch:

`feature/micro-drama-pivot`

Target:

`main`

One feature branch and one PR are used for the pivot. No supporting branch is required.

## Git Workflow Rule

- `main` is the permanent integration branch.
- One feature branch per PR.
- No unrelated features are mixed into the Phase 12 branch.
- The Phase 12 PR should be merged only after the newest PR Debug APK run on the final branch head succeeds.

## Next Immediate Step

Verify the final PR Debug APK run for the Phase 12 branch, review the changed navigation/player wiring, then merge the single Phase 12 PR into `main`.
