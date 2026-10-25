# El Adviento de LuisMi 🎄

An interactive Advent Calendar app featuring Luis Miguel's songs, now native on **Android and iOS**.

Website and downloads: https://luismigueladviento.nikicoding.com

## Branches

The 2026 rewrite replaces the old Android-only app. **`2026_prod_test_enabled` is the new main branch.**

| Branch | What it is |
|---|---|
| `2026_prod_test_enabled` | **Current version (default).** Kotlin Multiplatform app for Android and iOS, with the test mode available in Settings. |
| `2026_prod` | Final 2026 release. It will be published here once the app is finished. |
| `master`, `2025_prod`, `2025_test_enabled` | Legacy 2025 version: Android only, YouTube-based. Kept for reference. |

## Description

El Adviento de LuisMi combines the magic of Christmas with Luis Miguel's music. Every day from December 1st to December 25th a new door opens with a song and its album cover. Door 25 is a full-screen Christmas surprise.

## Features

- **Native on Android and iOS**: Kotlin Multiplatform with Compose Multiplatform. No web views.
- **Calendar grid**: doors 1–24 in a 4 × 6 grid; swipe down for door 25, which fills the whole screen.
- **Daily unlock**: door N opens on December N and every door stays open through January.
- **Offline songs**: 25 songs bundled with the app, played with the native players (`MediaPlayer` on Android, `AVAudioPlayer` on iOS).
- **Album covers**: each door shows its song's cover, blurred until the door is opened.
- **Persistent progress**: local SQLite database (SQLDelight) stores opened doors, play counts and settings.
- **Native notifications**: an optional reminder every morning from December 1st to 25th, at a time you choose.
- **Spanish and English**: switch the language from Settings.
- **Reset days**: close every door again from Settings.
- **Test mode**: simulate any day of December, send a test notification and preview every door.

## Project structure

```
composeApp/
  src/commonMain/      Shared UI (calendar, player, settings), SQLite store, translations
  src/androidMain/     MainActivity, MediaPlayer, AlarmManager notifications
  src/iosMain/         AVAudioPlayer, UNUserNotificationCenter notifications
  src/commonMain/composeResources/files/songs/   Songs, covers and songs.json (not in git)
iosApp/                Xcode project (SwiftUI wrapper)
tools/download_songs.py   Downloads the 25 songs and covers
```

## Songs

The songs are copyrighted, so they are **not** stored in this repository. Download them before building:

```sh
pip install -U "yt-dlp[default]"    # also needs ffmpeg on the PATH
python tools/download_songs.py      # random selection; door 25 always comes from "Navidades"
python tools/download_songs.py --seed 7   # reproducible selection
```

The script picks 25 random Luis Miguel songs from the iTunes catalogue, downloads the audio with yt-dlp + ffmpeg and the covers from iTunes, and writes `songs.json`. Without the songs the app still runs, but the doors are empty.

## Development setup

### Android

1. Clone the repository and download the songs.
2. Open the project in Android Studio, or run `./gradlew :composeApp:installDebug`.

### iOS

Needs a Mac with Xcode 16 or newer.

1. Clone the repository and download the songs.
2. Open `iosApp/iosApp.xcodeproj`.
3. Choose your team under *Signing & Capabilities* and run. The *Compile Kotlin Framework* build phase runs Gradle for you.

## Requirements

- JDK 17 or newer
- Android: minimum Android 8.0 (API 26)
- iOS: iOS 16 or newer

## Testing mode

Open **Settings → Test mode** and turn it on:
- **Simulated day**: pretend it is any day from December 1st to 25th.
- **Send test notification**: posts a notification after 5 seconds.

## Contributing

Contributions are welcome! Please feel free to submit pull requests.

## License

[MIT License](https://choosealicense.com/licenses/mit/)

## Credits

- Application developed by NNKtv28
- Songs by Luis Miguel

## Contact

- Email: nnktv28@gmail.com
- Github: https://github.com/nnktv28
- LinkedIn: https://www.linkedin.com/in/nikicoding/
- Portfolio: https://nikicoding.com
- Project website: https://luismigueladviento.nikicoding.com
