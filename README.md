# Videra

**Videra** is a decentralized, open-source 9:16 micro-drama player for Android.

Videra is a lightweight streaming shell: the Android application provides the presentation, playback, local persistence, and navigation layers, while catalogs and stream metadata are supplied by Web Add-ons through a small JSON-based protocol. No media catalog is hard-coded into the application.

## Architecture

Videra is built around a small Kotlin-first Android stack:

- **Jetpack Compose** for the UI and navigation surface.
- **A single Media3 ExoPlayer engine** owned by the application-scoped player controller.
- **Room database v5** for local persistence, including episode playback progress.
- **Dark-only themes** with two supported visual systems: **Velvet Red** and **OLED Black**.
- **Coroutines and StateFlow** for lifecycle-aware state propagation.
- **Retrofit + Kotlin Serialization** for Web Add-on manifests and media metadata.
- **Coil 3** for remote artwork.

The application is intentionally a client shell rather than a bundled content catalog. Add-ons provide the media metadata that the app renders and plays.

## Web Add-on JSON Manifest

An installed Web Add-on is described by a JSON manifest.

### Manifest fields

| Field | Description |
| --- | --- |
| `id` | Stable add-on identifier. |
| `name` | Human-readable add-on name. |
| `version` | Add-on manifest version. |
| `supportedTypes` | Media types supported by the add-on. Videra currently supports `MICRO_DRAMA`. |
| `mediaItemsUrl` | Endpoint returning the add-on's media-item JSON collection. |

### Supported media type

The current application-level media type is:

`MICRO_DRAMA`

A media item may expose its streams directly and/or expose an ordered episode list.

### Episode schema

Each episode uses the following fields:

| Field | Description |
| --- | --- |
| `number` | Positive episode number used for ordering and progress restoration. |
| `title` | Display title for the episode. |
| `durationSeconds` | Optional duration in seconds. |
| `streams` | Available playback streams for the episode. |

Each stream contains:

| Field | Description |
| --- | --- |
| `url` | HTTP or HTTPS playback URL. |
| `quality` | Human-readable stream quality label. |

A simplified manifest shape is conceptually:

```json
{
  "id": "example-addon",
  "name": "Example Add-on",
  "version": "1.0.0",
  "supportedTypes": ["MICRO_DRAMA"],
  "mediaItemsUrl": "https://example.invalid/media.json"
}
```

The manifest identifies the add-on and its media endpoint; individual media and episode objects returned by `mediaItemsUrl` carry the titles, artwork, episode metadata, and playback streams.

## Playback and progress

Playback is centralized through one Media3 ExoPlayer instance so the UI does not create competing player engines. The player controller exposes state through `StateFlow`, translates Media3 failures into explicit UI error states, and tracks playback position while media is playing.

Room schema version 5 adds the episode-progress store. Progress is keyed by add-on and media identifier and records the last watched episode number, playback position, and update timestamp. This allows a micro-drama to resume from the last saved episode and position.

## Themes

Videra is dark-only. The available theme selections are:

- **Velvet Red** — a dark red-accent visual system.
- **OLED Black** — a near-black visual system optimized for OLED displays.

Theme preference and onboarding state are persisted locally.

## Project structure

The codebase separates responsibilities into:

- **data** — Room persistence, preferences, remote add-on API, DTOs, and mappers.
- **domain** — media, episode, add-on, library, and playback models plus repository contracts.
- **media/player** — the application-level Media3 playback engine and playback state.
- **ui** — Compose screens, navigation, onboarding, library, settings, and player presentation.
- **di** — application-scoped repository and service construction.
- **diagnostics** — crash capture and startup diagnostics.

## Development principles

Videra keeps the Android client thin and predictable:

- No provider-specific web scraping inside the Android application.
- Add-ons are remote JSON providers for catalogs and streams.
- Playback errors are represented as application state rather than uncaught initialization exceptions.
- Local progress persistence is isolated behind a repository interface.
- UI code observes state rather than owning playback or persistence infrastructure.

## Status

Videra is an actively developed Android project focused on the 9:16 micro-drama experience and its Web Add-on protocol.
