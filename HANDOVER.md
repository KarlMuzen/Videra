# HANDOVER.md

## Current Branch
feature/media3-player

## Completed Task
Phase 8 — Global Media3 Player plus in-app playback diagnostics.

Implemented:
- Created a single app-scoped \`PlayerController\` that owns one \`ExoPlayer\` instance.
- Injected the global player through \`VideraAppContainer\` using the application context.
- Added \`PlayerPlaybackState\` values: IDLE, LOADING, PLAYING, PAUSED, BUFFERING, ERROR.
- Added \`PlayerState\` with playback state, current domain \`MediaItem\`, current \`Stream\`, and generic error state.
- Added Media3 diagnostics fields to \`PlayerState\`:
  - \`attemptedStreamUrl\`
  - \`errorCodeName\`
  - \`errorCauseMessage\`
  - \`errorStackTrace\`
- \`PlayerController\` now captures \`PlaybackException.errorCodeName\`, the root-cause expression \`error.cause?.message ?: error.message\`, and the full \`stackTraceToString()\`.
- Added a reusable Compose \`PlayerSurface\` using Media3 \`PlayerView\`.
- \`PlayerSurface\` detaches the shared player when its \`AndroidView\` leaves the composition.
- Added full-screen \`PlayerScreen\` with Media3 controls, loading/buffering feedback, playback errors, retry, and back navigation.
- The Player error UI now exposes the attempted stream URL, Media3 error-code name, and exception/root-cause message in selectable small typography.
- Added \`Copy Diagnostics\` using Compose \`LocalClipboardManager\` / \`ClipboardManager\`; copied diagnostics include the full captured stack trace.
- Replaced the previous global empty mini-player slot with \`MiniPlayer\`.
- \`MiniPlayer\` observes global player state and shows the current title, play/pause control, and dismiss control while media is loaded.
- Added a secondary \`Player\` navigation destination without changing the five existing top-level destinations.
- Home media cards are clickable and launch the Player destination.
- Home playback commands go through the shared \`PlayerViewModel\`, not directly through ExoPlayer from the Composable.
- Extended \`MediaItem\` and \`MediaItemDto\` with optional \`streams\`.
- Existing media JSON remains compatible because \`streams\` defaults to an empty list.
- \`AddonRepositoryImpl\` maps \`StreamDto\` values into domain \`Stream\` values without exposing DTOs to Compose.
- \`PlayerViewModel\` selects the first add-on-provided stream when available.
- Updated the Big Buck Bunny development fallback to:
  \`https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4\`
- Shorts dynamically attach the same global player to the active \`MICRO_DRAMA\` item without navigation.
- Shorts render one shared \`PlayerSurface\` for the active item instead of creating a player/view per pager page.
- Shorts retain existing artwork and partial-failure/empty states.
- Added playback error/retry feedback for Shorts.
- Added \`LifecycleEventObserver\`: \`ON_STOP\` pauses playback, \`ON_DESTROY\` releases the player, and \`onDispose\` removes the observer.
- Player route uses the full Scaffold area and hides bottom navigation.
- Shorts continues to use the full Scaffold area so the vertical player can extend behind the bottom navigation layer.
- No scraper or provider-specific extraction logic was added.
- No second playback engine or additional playback framework was added.

## Development Manifest
- \`app/src/main/AndroidManifest.xml\` already contains:
  \`android:usesCleartextTraffic="true"\`
- No Manifest XML change was necessary in the diagnostic patch because the requested attribute was already present on the live Phase 8 branch.
- The application still declares \`android.permission.INTERNET\`.

## Build Fixes Found During Phase 8 Validation
1. CI Run #16 failed before Gradle execution because \`./gradlew\` was absent. Added an executable \`gradlew\` launcher with Git mode \`100755\`.
2. CI Run #17 reached Kotlin compilation and failed on a missing \`androidx.compose.ui.unit.dp\` import in \`MainScaffold.kt\`. Added the import.
3. CI Run #18 completed \`Assemble Debug\` and Debug APK upload successfully.
4. CI Run #19 completed successfully after synchronization.
5. CI Runs #20, #21, and #22 completed successfully; #22 validated the previous handover head.
6. CI Run #23 completed successfully for the previous Phase 8 head.
7. The diagnostic patch was then committed as three source-file changes only: \`PlayerController.kt\`, \`PlayerScreen.kt\`, and \`PlayerViewModel.kt\`.

## CI Workflow
Updated \`.github/workflows/pr-debug-build.yml\`.
The build command is exactly:
\`./gradlew assembleDebug -x lint -x test\`
Lint and tests are explicitly excluded from the PR debug assembly.
APK upload behavior remains unchanged.

## Current Bugs / Product Boundaries
- Real add-ons can provide playback streams through the optional \`streams\` field on each media item.
- Existing add-ons that omit \`streams\` use the approved Big Buck Bunny development stream.
- The fallback uses the \`commondatastorage.googleapis.com\` Google Storage host.
- Stream subtitles are retained in the domain model but subtitle rendering is not separately wired in this phase.
- One global ExoPlayer instance is used across Home, Shorts, and Player navigation.
- Playback is foreground-only: \`ON_STOP\` pauses and \`ON_DESTROY\` releases.
- Mini-player state is activity-global and remains available on non-Player destinations.
- Physical-device playback validation is still required for real add-on streams and decoder/network behavior.
- The in-app diagnostics are intended to replace unavailable Logcat during device debugging by surfacing the Media3 error code, root-cause message, attempted URL, and copied stack trace.

## API Self-Verification
Checked against current Android/Media3 documentation:
- Media3 1.11.1 is the project version and is a current stable release.
- \`PlaybackException.getErrorCodeName()\` / Kotlin property access \`errorCodeName\` is a valid public Media3 API.
- \`PlaybackException.cause\` and \`message\` are inherited Throwable/Exception data available to the listener.
- \`Player.Listener.onPlaybackStateChanged\`, \`onIsPlayingChanged\`, and \`onPlayerError\` are valid APIs.
- \`Player.STATE_IDLE\`, \`STATE_BUFFERING\`, \`STATE_READY\`, and \`STATE_ENDED\` are valid playback states.
- \`PlayerView.player\` supports attaching and detaching a \`Player\`.
- Compose \`AndroidView\` supports \`onRelease\`.
- Compose \`SelectionContainer\` enables selectable text.
- \`androidx.compose.ui.platform.LocalClipboardManager\` and \`ClipboardManager.setText(AnnotatedString)\` are valid APIs; \`LocalClipboardManager\` is deprecated in current Compose in favor of \`LocalClipboard\`, so the diagnostic screen explicitly suppresses that deprecation while retaining the requested simple clipboard API.
- \`DisposableEffect\` supports clean lifecycle observer registration/removal.
- Material 3 \`Card(onClick = ...)\` is used for Home cards.
- No invalid \`ColorScheme.onInverseSurface\` reference remains; \`inverseOnSurface\` is used.

## Pull Request
PR #7:
- \`feature/media3-player\` -> \`main\`
- Open, not merged.
- Source head containing the diagnostic patch: \`68a088d4c36a8edb3da9ed4ed6fd9c7678db6d44\`
- Current \`main\`: \`f3746992ab0aa6b1fd317d8fc27c2cb98aa7fa62\`
- Feature branch is 0 commits behind \`main\` at the time of the diagnostic patch.
- PR is currently mergeable.
- Phase 7 PR #6 was merged before Phase 8 work.
- A workflow-only \`main\` change was reconciled into the Phase 8 branch while retaining the requested exact CI command.

## CI Status
Before the diagnostic patch, PR Debug APK Run #24 completed successfully on the then-current Phase 8 head.
The diagnostic patch triggered a new PR Debug APK validation run after the three source changes.
The handover document is being updated on the same feature branch; re-check the newest PR run before merging.

## Git Workflow Rule
- \`main\` is the permanent integration branch.
- One feature branch per PR.
- No unrelated feature branches.
- No mixing independent features into one PR.

## Next Immediate Step
Phase 9 — Continue according to the latest \`BLUEPRINT.md\`; preserve the one-player architecture and use the new in-app diagnostic inspector to validate the fallback and real add-on stream behavior on a physical Android device before adding more playback features.
