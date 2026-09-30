# HANDOVER.md

## Current Branch
feature/addon-manager

## Completed Task
Phase 10 — Add-on Manager, persistent add-on state, and reactive enabled-only feeds.

Implemented:
- Added `manifestUrl`, `customName`, `enabled`, and `cachedMetadataJson` to `InstalledAddonEntity`.
- Expanded `VideraDatabase` from version 3 to version 4.
- Added explicit Room migration 3 -> 4; existing add-ons keep their prior base URL as their manifest URL and remain enabled by default.
- Added DAO support for finding add-ons, observing all installed add-ons, observing only enabled add-ons, toggling enabled state, and changing custom names.
- Extended `InstalledAddon` with manifest name, manifest URL, custom name, enabled state, and cached metadata.
- Added `InstallAddonUseCase` for HTTP/HTTPS manifest URL validation and the repository install boundary.
- Kept Retrofit + Kotlinx Serialization as the remote manifest fetch/parse stack; no extra networking dependency was added.
- Manifest validation requires non-blank id, name, version, and at least one supported media type.
- Optional `mediaItemsUrl` is resolved against the manifest URL and must resolve to HTTP or HTTPS.
- Successfully fetched manifests are cached locally as normalized JSON metadata.
- Reinstalling an existing add-on preserves its enabled state and custom name while refreshing its manifest metadata.
- Added `AddonManagerViewModel` with explicit validation, network, HTTP, parsing, and unknown error categories.
- Added manual `AddonManagerViewModelFactory`.
- Added dedicated `AddonManagerScreen` with add-manifest dialog, active/inactive state, enable/disable, rename, delete, and metadata-cache status.
- Settings now opens Add-on Manager through a nested navigation route.
- Add-on Manager ViewModel is scoped to the Add-on Manager navigation destination.
- Home, Search, and Shorts now observe the database-backed enabled-only add-on Flow.
- Room changes from install/remove/toggle propagate through StateFlow and cancel/restart in-flight feed collection via `collectLatest`.
- The existing single global Media3 player remains unchanged and is reused by Home, Shorts, Search, and Library.
- Library snapshots remain independent of installed add-on removal.
- No scrapers, provider-specific extraction logic, XML, Hilt/Dagger, Gson, Moshi, or additional dependencies were added.

## Room Database
Current schema:
- Version: 4
- Entities:
  - `InstalledAddonEntity`
  - `LibraryEntity`
- Migrations:
  - 1 -> 2: adds `installed_addons.mediaItemsUrl`
  - 2 -> 3: creates `library_items`
  - 3 -> 4: adds manifest URL, custom name, enabled state, and cached manifest metadata
- Migration 3 -> 4 defaults existing add-ons to enabled and copies the previous `baseUrl` into the new `manifestUrl` column.
- Database creation still uses the application context and registers all migrations explicitly.

## Add-on Fetch / Validation
- Add-ons remain remote JSON sources; Videra contains no provider scraper.
- Retrofit accepts an absolute manifest URL through the existing `@Url` API.
- Kotlinx Serialization continues to ignore unknown JSON keys while requiring the declared manifest schema fields.
- `InstallAddonUseCase` rejects non-HTTP/HTTPS URLs before network access.
- Repository validation rejects blank required fields and invalid resolved media endpoints.
- Serialization errors remain `SerializationException` so the UI can expose a parsing-specific error state.
- Network/HTTP/database failures are converted into explicit manager UI errors without swallowing `CancellationException`.

## Reactive Feed Semantics
- `AddonRepository.observeEnabledAddons()` is the single database-backed source for active add-ons.
- Home and Search use enabled add-ons only.
- Shorts also uses enabled add-ons only so disabled providers do not continue feeding vertical content.
- Search still filters the loaded enabled catalog locally by title rather than sending a request for every keystroke.
- Installing an add-on creates an enabled row, which immediately changes the enabled Flow and starts feed loading.
- Disabling an add-on removes it from the enabled Flow immediately and cancels the previous feed collection.
- Re-enabling an add-on immediately starts loading it again.
- Removing an add-on updates both installed and enabled Room Flows.

## Compose / ViewModel Verification
- Search, Library, Settings, and Add-on Manager screens use lifecycle-aware StateFlow collection.
- Add-on Manager operations run only in `viewModelScope`.
- `CancellationException` is always rethrown.
- All ViewModels continue to use manual factories; no service locator or Hilt/Dagger was introduced.
- Scoped `Modifier.weight(1f)` is used without importing the internal `weight` symbol.
- Add-on Manager list/empty content is constrained with scoped weight so it does not overflow the header area.
- Destructive add-on removal requires confirmation.

## CI Workflow
- `.github/workflows/pr-debug-build.yml` remains unchanged.
- Exact build command:
  `./gradlew assembleDebug -x lint -x test`
- CI intentionally excludes lint and tests from this PR build workflow.
- The repository cannot be built from the model container because outbound GitHub DNS/network access is unavailable; GitHub Actions is the authoritative build validation.

## Phase 9 / Main Baseline
- Phase 9 / PR #8 is merged into `main`.
- Current Phase 9 main baseline:
  `07d4d5275e27717454703071ce14053f02c1311a`
- Global Media3 player remains app-scoped with one `ExoPlayer`.
- Big Buck Bunny development fallback remains:
  `https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4`
- `android:usesCleartextTraffic="true"` remains enabled for development.

## Documentation Note
A recursive GitHub tree check of current `main` found `HANDOVER.md` but no `BLUEPRINT.md` anywhere in the repository. Phase 10 was therefore implemented against the latest available handover and live source tree without inventing missing Blueprint requirements.

## Pull Request
PR #9:
- `feature/addon-manager` -> `main`
- One feature branch and one PR for Phase 10.
- No supporting branch was created.
- PR is open; final merge should occur only after the newest PR Debug APK run succeeds.

## Git Workflow Rule
- `main` is the permanent integration branch.
- One feature branch per PR.
- No unrelated feature branches.
- No mixing independent features into one PR.

## Next Immediate Step
Verify the newest GitHub Actions run for the final branch head, then review and merge PR #9. After merge, continue with the next Blueprint-defined phase while preserving the remote JSON add-on boundary, Room persistence, single-player architecture, and lifecycle-safe StateFlow patterns.

