# El Adviento de LuisMi 🎄

An interactive Advent Calendar Android application featuring Luis Miguel's songs and special Christmas moments.

## Description

LuismiXmasCalendar is a festive Android application that combines the magic of Christmas with Luis Miguel's music. It presents users with a daily advent calendar experience from December 1st to December 25th, where each day reveals a special song with background audio playback.

## Features

### 🎄 Core Features
- **Interactive Calendar Interface**: 24 clickable days plus a special Christmas Day (25th) celebration
- **Daily Content Unlock**: Days are unlocked progressively as December advances
- **Persistent Progress**: SQLite database tracks opened days with visual indicators (red/green borders)
- **Custom Imagery**: Unique images for each calendar day
- **Responsive Design**: Adapts to different screen sizes
- **Spanish Language Support**: All text and messages in Spanish

### 🎵 Audio Features (2025 Version)
- **Background Music Service**: Foreground service for continuous audio playback
- **MP3 Audio Files**: 25 embedded MP3 files (one for each day)
- **Media Notifications**: Android notification with playback controls (Play/Pause/Next)
- **Auto-Play Next**: Automatically advances to next day's song when current finishes
- **Wake Lock Support**: Music continues playing even when screen is off
- **Media Controls**: In-app play/pause and next buttons

### 🎁 Available Versions
- **Production Version** (`2025_prod` branch): Days unlock based on actual December calendar dates
- **Test Version** (`2025_test_enabled` branch): All days unlocked for testing and demonstrations

## Technical Details

### Architecture Components

1. **MainActivity**
    - Manages the calendar grid interface (25 ImageViews)
    - Handles day unlocking logic based on current date (December only)
    - Implements SQLite database for progress tracking
    - Visual feedback with colored borders (red = locked, green = opened)
    - Toast notifications for locked days

2. **DayViewActivity**
    - Displays individual day content with image and song title
    - Binds to MusicService for audio control
    - Media playback controls (play/pause/next buttons)
    - Real-time UI updates for playback state
    - Broadcast receiver for day changes
    - Properly manages service lifecycle

3. **MusicService** (NEW in 2025)
    - Foreground service for background music playback
    - MediaPlayer-based audio playback
    - Notification with media controls
    - Service actions: PLAY, PAUSE, NEXT, STOP
    - Broadcasts day change events
    - Wake lock management for continuous playback
    - Auto-play next day functionality

### Data Structure

- **Audio Files**: 25 MP3 files (day_1.mp3 to day_25.mp3) embedded in APK
- **Song Titles**: 25 Spanish song titles mapped to each day
- **Images**: 25 custom drawable images (day_1 to day_25)
- **Database**: SQLite with `opened_days` table (single column: day INTEGER PRIMARY KEY)

## Song List

1. Va a Nevar
2. Frente a La Chimenea
3. Sonríe
4. Amor A Mares
5. Te propongo esta noche
6. Santa Claus Llegó A La Ciudad
7. Dame
8. Más
9. Estaré En Mi Casa Esta Navidad
10. Suave
11. Un Hombre Busca a Una Mujer
12. Cómo Es Posible Que a Mi Lado
13. Motivos
14. Mi Humilde Oración
15. Serenata Huasteca
16. Amarte es un placer
17. Amor, Amor, Amor
18. La Fiesta Del Mariachi
19. Contigo En La Distancia
20. Sueña
21. Llegó La Navidad
22. Te Deseo Muy Felices Fiestas
23. Blanca Navidad
24. Noche De Paz
25. Navidad, Navidad

## Development Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Ensure all MP3 files are in `app/src/main/res/raw/`
5. Run on an Android device or emulator (API 24+)

## Requirements

- **Android SDK**: Min API 24 (Android 7.0), Target API 34 (Android 14)
- **Storage**: ~100 MB (includes 25 MP3 audio files)
- **Permissions**:
  - `INTERNET`: For potential future features
  - `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: Background music
  - `POST_NOTIFICATIONS`: Media playback notifications
  - `WAKE_LOCK`: Keep music playing when screen is off

## Building the App

### Production Build (Date-Locked)
```bash
git checkout 2025_prod
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Test Build (All Days Unlocked)
```bash
git checkout 2025_test_enabled
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Key Dependencies

- **Kotlin**: Primary development language
- **AndroidX Core KTX**: Android extensions
- **AndroidX Media**: Media framework support
- **Material3**: Modern UI components
- **Jetpack Compose**: UI toolkit (configured but not actively used)
- **ConstraintLayout & GridLayout**: Layout management

## Project Structure

```
app/src/main/
├── java/com/example/luismixmascalendar/
│   ├── MainActivity.kt          # Calendar grid interface
│   ├── DayViewActivity.kt       # Individual day display
│   └── MusicService.kt          # Background audio service
├── res/
│   ├── drawable/                # Images for each day + borders
│   ├── layout/
│   │   ├── main_activity.xml    # Calendar grid layout
│   │   └── day_view.xml         # Day detail layout
│   ├── raw/                     # MP3 audio files (day_1 to day_25)
│   └── values/                  # Strings, colors, themes
└── AndroidManifest.xml          # App configuration & permissions
```

## Version History

### Version 1.0 (2025)
- Complete rewrite with background music service
- MP3 audio files replace YouTube integration
- Media notification with playback controls
- Foreground service implementation
- Auto-play next day feature
- Wake lock support
- Two build variants (production/test)

### Earlier Versions
- Initial implementation with YouTube player integration
- Basic calendar grid interface
- SQLite progress tracking

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
- LinkedIn: https://www.linkedin.com/in/nikita-molina-arévalo/
- Portfolio: https://nnktv28.github.io/
