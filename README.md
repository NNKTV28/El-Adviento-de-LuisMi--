# El Adviento de LuisMi 🎄

An interactive Advent Calendar Android application featuring Luis Miguel's songs and special Christmas moments.

## Description

LuismiXmasCalendar is a festive Android application that combines the magic of Christmas with Luis Miguel's music. It presents users with a daily advent calendar experience from December 1st to December 25th, where each day reveals a special song and image.

## Features

- **Interactive Calendar Interface**: 24 clickable days plus a special Christmas Day celebration
- **Daily Content Unlock**: Days are unlocked progressively as December advances
- **YouTube Integration**: Each day features a specific Luis Miguel song video
- **Persistent Progress**: SQLite database tracks opened days
- **Custom Imagery**: Unique images for each calendar day
- **Responsive Design**: Adapts to different screen sizes
- **Special Christmas Day**: Exclusive celebration view for December 25th

## Technical Details

### Main Components

1. **MainActivity**
    - Manages the calendar grid interface
    - Handles day unlocking logic
    - Implements SQLite database for progress tracking
    - Features testing mode for development

2. **DayViewActivity**
    - Displays individual day content
    - Manages YouTube video embedding
    - Shows day-specific images and song titles
    - Includes navigation controls

### Data Structure

- **YouTube Links**: 25 pre-configured video URLs
- **Song Titles**: Corresponding Spanish titles for each day
- **Database**: Simple SQLite structure for tracking opened days

## Development Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on an Android device or emulator

## Requirements

- Android SDK
- Minimum Android version: [Specify version]
- Internet connection for YouTube video playback

## Testing Mode

The application includes a testing mode for development:
- Set `testing = true` in MainActivity
- Adjust `testing_day` value to simulate different December dates

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
