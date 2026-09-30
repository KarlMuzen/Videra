# HANDOVER.md

## Current Branch
feature/media3-player

## Completed Task
Phase 8 — Global Media3 Player.

Implemented:
- Created a single `PlayerController` that owns one `ExoPlayer` instance.
- Injected the global player through `VideraAppContainer` using the application context.
- Added `PlayerPlaybackState` values:
  - IDLE
  - LOADING
  - PLAYING
  - PAUSED
  - BUFFERING
  - ERROR
- Added `PlayerState` with playback state, current domain `MediaItem`, current `Stream`, and error message.
- Added `PlayerViewModel` as the UI playback command/state boundary.
- Added a reusable Compose `PlayerSurface` using Media3 `PlayerView`.
- `PlayerSurface` explicitly detaches the shared player when its AndroidView leaves the composition.
- Added full-screen `PlayerScreen` with Media3 controls, loading/buffering feedback, playback errors, retry, and back navigation.
- Replaced the previous global empty mini-player slot with `MiniPlayer`.
- `MiniPlayer` observes the global player state and shows the current title, play/pause control, and dismiss control only while media is loaded and not in IDLE or ERROR.
- Added a secondary `Player` navigation destination; the five existing top-level destinations remain unchanged.
- Home media cards are now clickable and launch the Player destination.
- Home calls the shared `PlayerViewModel`; playback is not embedded in the card Composable.
- Extended `MediaItem` and `MediaItemDto` with optional `streams`.
- Existing media JSON remains compatible because `streams` defaults to an empty list.
- `AddonRepositoryImpl` maps `StreamDto` values into domain `Stream` values without exposing DTOs to Compose.
- `PlayerViewModel` selects the first add-on-provided stream when available.
- When an add-on does not provide a stream yet, the approved Big Buck Bunny development MP4 is used as the temporary test stream.
- Shorts now attach the same global player to the currently active `MICRO_DRAMA` item without navigation.
- Shorts render one shared `PlayerSurface` for the active item instead of creating a player/view per pager page.
- Shorts retain their artwork as the pre-playback fallback and keep their existing partial-failure and empty states.
- Added playback error/retry feedback inside Shorts.
- Added Compose lifecycle observation with `LifecycleEventObserver`:
  - `ON_STOP` pauses playback;
  - `ON_DESTROY` releases the player;
  - the observer is removed in `onDispose`.
- Full-screen Player and Shorts routes use the full Scaffold content area.
- The navigation bar is hidden on the Player route so the PlayerView can occupy the full screen.
- Settings and existing Home/Shorts aggregation behavior remain otherwise unchanged.
- No scraper or provider-specific extraction logic was added.
- No second playback engine or additional playback framework was added.

## Build Fixes Found During Phase 8 Validation
- CI Run #16: `./gradlew` was missing from the repository. Added an executable `gradlew` launcher with Git mode `100755`.
- CI Run #17: Kotlin compilation failed on a missing `androidx.compose.ui.unit.dp` import in `MainScaffold.kt`. Added the import.
- CI Run #18: full debug assembly and APK upload passed.
- CI Run #19: documentation synchronization build passed.
- CI Run #20: final handover synchronization build passed.

## CI Workflow
Updated `.github/workflows/pr-debug-build.yml`:
- Debug build command is exactly:
  `./gradlew assembleDebug -x lint -x test`
- Lint and test tasks are explicitly excluded from the PR debug assembly.
- APK upload behavior remains unchanged.

## Build Fixes Found During Phase 8 Validation
1. CI Run #16 failed before Gradle execution because `./gradlew` did not exist. Added executable `gradlew` with mode `100755`.
2. CI Run #17 reached Kotlin compilation and found one compiler error in `MainScaffold.kt`: missing `androidx.compose.ui.unit.dp` import. Added the import.
3. CI Run #18 completed successfully through `Assemble Debug` and Debug APK upload.

## Current Bugs / Product Boundaries
- Real add-ons can provide playback streams through the optional `streams` field on each media item.
- Existing add-ons that omit `streams` currently use the approved Big Buck Bunny development stream so the player path can be exercised without provider-specific extraction.
- Stream subtitles are carried in the domain model but subtitle rendering is not separately wired in this phase.
- Player state is owned by the single global controller, preventing multiple ExoPlayer instances from Home/Shorts.
- Playback is intentionally foreground-only: `ON_STOP` pauses it and `ON_DESTROY` releases it.
- Mini-player state is global for the activity and survives navigation between Home, Shorts, and other non-Player destinations.

## API Self-Verification
Verified against current Android/Media3 documentation:
- Media3 1.11.1 is the current stable Media3 release used by this project.
- `Player.Listener.onPlaybackStateChanged`, `onIsPlayingChanged`, and `onPlayerError` are valid current callbacks.
- Valid player states include `STATE_IDLE`, `STATE_BUFFERING`, `STATE_READY`, and `STATE_ENDED`; the implementation maps these to the app's UI state.
- `PlayerView.player` supports attaching/detaching a `Player`.
- Compose `AndroidView` provides `onRelease`.
- `DisposableEffect` is the current Compose pattern for adding/removing a `LifecycleEventObserver`.
- Material 3 card click handling uses the clickable `Card(onClick = ...)` API.
- No invalid `ColorScheme.onInverseSurface` reference remains; the Phase 7 fix uses `inverseOnSurface`.

## Pull Request
PR #7:
- `feature/media3-player` -> `main`
- Open, not merged.
- Current PR head: `696ce743969811d2db21e9011a770265770e1a1d`
- Current `main`: `f3746992ab0aa6b1fd317d8fc27c2cb98aa7fa62`
- Feature branch is 0 commits behind `main`.
- Phase 7 PR #6 was merged before Phase 8 work.
- `main` also contains a separate workflow-only update commit (`f3746992ab0aa6b1fd317d8fc27c2cb98aa7fa62`); the Phase 8 branch intentionally keeps the requested exact workflow command.

## Git Workflow Rule
- `main` is the permanent integration branch.
- One feature branch per PR.
- No unrelated feature branches.
- No mixing independent features into one PR.

## CI Status
Latest completed application-code validation:
- Workflow: PR Debug APK
- Run: #18
- Tested PR head: `325c9750ecf2fb901eebb4f1e80bb16c9835c641`
- Result: success
- Assemble Debug: success
- Upload Debug APK: success
- Lint and tests were explicitly excluded by the workflow command as requested.

Run #19 is a documentation-only synchronization build for the handover update and does not change application build inputs. Phase 8 application code is build-verified.

## Next Immediate Step
Phase 9 — Continue according to the latest `BLUEPRINT.md`; preserve the one-player architecture and verify real add-on stream playback on a physical Android device before adding more playback features.
