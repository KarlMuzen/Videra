# HANDOVER.md

## Current Branch
feature/navigation-shell

## Completed Task
Phase 2 — Navigation Shell & Global Mini-Player Slot.

Implemented:
- Created branch `feature/navigation-shell` from `main`.
- Added Navigation Compose dependency at stable version 2.10.2.
- Added Material icon dependency required by the five top-level navigation destinations.
- Added `ui/navigation/Screen.kt` as the navigation contract.
- Added five destinations:
  - Home
  - Shorts
  - Search
  - Library
  - Settings
- Shorts uses SmartDisplay icons and does not reuse the Library icon.
- Added `ui/navigation/MainScaffold.kt`.
- Added a Material 3 `Scaffold` with a persistent bottom-bar layout.
- Reserved a 64dp empty Box for the future Global Mini-Player / Now Playing Bar outside the NavHost.
- Added NavigationBar with state-aware filled/outlined icons.
- Added state restoration and single-top navigation for top-level tabs.
- Added lightweight placeholder content for all five destinations.
- Updated `MainActivity.kt` to render `MainScaffold` with `enableEdgeToEdge()`.

## Current Bugs
No known Phase 2 logic bugs.

Repository state note:
- GitHub `main` currently contains no committed files at the Phase 1 paths referenced by the previous HANDOVER. The Phase 2 files therefore establish those referenced paths on this feature branch rather than updating an existing tracked Phase 1 commit.
- The local Android project may already contain uncommitted Phase 1 files. Those local files should remain unchanged except where Phase 2 explicitly replaces `MainActivity.kt` and `app/build.gradle.kts`.

## Next Immediate Step
Phase 3 — Web-Addon Data Contract & Failover Client.

Use this exact prompt:

"Act as an Expert Android Architect.
Continue Project Videra following BLUEPRINT.md and the latest HANDOVER.md. I am developing from my mobile phone, so output complete, copy-ready files only.

We are implementing Phase 3: The Web-Addon Data Contract & Failover Client.

Git workflow:
- Continue from branch feature/navigation-shell only after the Phase 2 PR has been reviewed/merged into main.
- Create and switch to a new branch named feature/web-addon-client from main.
- Maintain the strict rule: 1 feature = 1 branch = 1 PR.

Architecture constraints:
- Dumb Frontend, Smart API.
- The Android app contains NO scrapers and NO service-specific extraction logic.
- Add-ons are remote user-provided JSON endpoints.
- Kotlin 100%, Jetpack Compose only, MVVM, Coroutines, Retrofit + Kotlinx Serialization, Coil 3, Room, Media3.
- No Hilt/Dagger.
- Do not introduce real copyrighted catalog titles or pirate URLs in mock data.
- Use Big Buck Bunny or clearly synthetic dummy data only.

Phase 3 goals:
1. Define the versioned Kotlinx Serialization data contract for a remote Videra Web-Addon JSON response.
2. Define models for add-on metadata, catalogs, media items, sources/streams, images, and basic metadata needed by the frontend.
3. Build a Retrofit client that accepts a user-provided HTTPS/HTTP add-on base URL.
4. Implement a repository/client failover strategy so a failed endpoint does not crash the UI.
5. Separate transport DTOs from domain-facing models where that improves architecture.
6. Expose loading, success, empty, and error states cleanly to MVVM.
7. Do not connect the client to real providers or scraping implementations yet.
8. Include unit-testable parsing/failover boundaries and safe dummy JSON examples.

At the end, provide the updated HANDOVER.md block for the next session."
