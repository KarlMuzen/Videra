# HANDOVER.md

## Current Branch
feature/premium-ui-onboarding

## Completed Task
Phase 13.1 — UI and architecture hardening after the Phase 13 merge.

Implemented:
- SharedPreferences-backed PreferencesRepository for onboarding completion and app theme.
- SYSTEM, OLED_BLACK, and VELVET_RED theme values with safe defaults.
- PreferencesRepository and InstallAddonUseCase are injected through VideraAppContainer.
- VideraTheme provides dedicated Velvet Red and OLED Black schemes.
- MainActivity observes the persisted theme and applies VideraTheme.
- First-run onboarding accepts an add-on manifest URL, installs it through InstallAddonUseCase, persists completion, and navigates to Feed.
- Cold start goes directly to Feed after onboarding has been completed.
- Navigation names are now Discover and Feed instead of Home and Shorts.
- MainScaffold uses a root Box rather than Scaffold bottomBar.
- FloatingNavigationBar is positioned at BottomCenter with navigationBarsPadding.
- Feed navigation is transparent with a 100.dp bounded gradient scrim over the edge-to-edge video, avoiding unconstrained-height measurement on cold start.
- Other top-level routes use a translucent themed navigation surface.
- Player and onboarding routes do not render floating navigation.
- Settings persists the two supported dark themes: Velvet Red and OLED Black.
- Existing Micro-Drama-only content, episode progress, lifecycle handling, and single global Media3 player remain intact.
- CI workflow path and exact Gradle command remain unchanged.

## Preferences
SharedPreferences file: videra_preferences

Keys:
- onboardingCompleted
- appTheme
- last_crash_log

Defaults:
- onboardingCompleted = false
- appTheme = VELVET_RED

Theme parsing is defensive: blank or unknown values fall back to VELVET_RED.

## CI Workflow
.github/workflows/pr-debug-build.yml remains unchanged.

Exact build command:
./gradlew assembleDebug -x lint -x test

GitHub Actions is authoritative build validation because local Gradle execution is unavailable in the model container.

## Main Baseline
Current main:
91ed4defddbdbc6df5616bfff32b7df352c7442d

## Pull Request
Phase 13 and Phase 13.1 were merged through PR #12 and PR #13 respectively.

## Error Handling
- CrashHandler is isolated under diagnostics/ and is installed before ComponentActivity.onCreate.
- It persists the previous uncaught-exception trace to last_crash_log using commit() so the next launch can surface it.
- MainActivity only presents and clears the previous-session diagnostic; normal UI flows do not own uncaught-exception handling.


## Diagnostics / Insets Hardening
- Added persistent CrashHandler uncaught-exception logging under `last_crash_log`.
- Previous-session crash traces are surfaced on startup with copy and clear actions.
- Settings includes a Diagnostics & Logs card.
- Top-level content applies status bar insets to avoid camera-cutout/status-bar overlap.
- Floating navigation inner vertical padding is tightened to 4.dp.
- ShortsPager exits to its empty state before constructing a pager when the feed has zero items.
