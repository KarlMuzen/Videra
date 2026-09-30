# HANDOVER.md

## Current Branch
feature/premium-ui-onboarding

## Completed Task
Phase 13 — Premium UI, theming, onboarding, and transparent floating navigation.

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
- Feed navigation is transparent with a subtle gradient scrim over the edge-to-edge video.
- Other top-level routes use a translucent themed navigation surface.
- Player and onboarding routes do not render floating navigation.
- Settings includes a persisted System/OLED/Velvet appearance selector.
- Existing Micro-Drama-only content, episode progress, lifecycle handling, and single global Media3 player remain intact.
- CI workflow path and exact Gradle command remain unchanged.

## Preferences
SharedPreferences file: videra_preferences

Keys:
- onboardingCompleted
- appTheme

Defaults:
- onboardingCompleted = false
- appTheme = SYSTEM

## CI Workflow
.github/workflows/pr-debug-build.yml remains unchanged.

Exact build command:
./gradlew assembleDebug -x lint -x test

GitHub Actions is authoritative build validation because local Gradle execution is unavailable in the model container.

## Main Baseline
Current main:
2d5b5fffa52d7cc2cb57379e72961ee1d30d0546

## Pull Request
Phase 13:
feature/premium-ui-onboarding -> main

PR #12 is the single Phase 13 pull request.

## Next Immediate Step
Verify the final PR Debug APK workflow on the final branch head. Resolve any CI build errors before merging PR #12.
