BJJ Timer
A feature-rich Android timer app built for Brazilian Jiu-Jitsu training and competition. Supports multi-round sessions with automatic phase transitions, voice announcements, customizable sound effects, haptic feedback, and a beautiful OLED-optimized dark theme.

Features
Core Timer
Configurable round duration, rest duration, and number of rounds
Smooth 50ms countdown tick with automatic phase transitions (ROUND → REST → ROUND)
Pause, resume, and skip to next phase at any time
Keeps running in the background via a foreground service with notification controls
Audio Alerts
Event	Options
Round start	Boxing Bell, Buzzer, Air Horn, Whistle
Round end	Double Bell, Gong, Buzzer, Whistle
Minute warnings (1–5 min)	Voice announcer callouts or Soft Beep / Click / Chime
30-second warning	Double Beep, Alert Tone, Buzzer
Final 10-second countdown	Tick, Soft Beep, Click
Half-time alert	Toggle on/off
Session complete	Celebratory tone + vibration
Each alert can be toggled independently and previewed in the settings screen.

Haptic Feedback
Distinct vibration patterns for every event type — round start, round end, 30-second warning, and session complete. Toggle on/off globally in settings.

Built-in Presets
Preset	Duration	Rounds	Rest
IBJJF White Belt	5 min	5	1 min
IBJJF Blue Belt	6 min	5	1 min
IBJJF Purple Belt	7 min	5	1 min
IBJJF Brown/Black Belt	10 min	5	1 min
ADCC Qualifiers	5 min	1	—
Training Rounds	7 min	8	1 min
Drilling	3 min	10	30 sec
Submission Only	10 min	3	2 min
Open Mat	6 min	12	1 min
Save your own custom presets with any name and load them with a single tap.

Visual Design
Animated timer ring with gradient fill, tick marks, and a leading dot
Dynamic color coding: green → yellow → orange → red as time runs out, blue during rest
Pulsing scale animation in the final 10 seconds
Rotating glow halo and floating particle background effects
Glass-morphism cards on a pure OLED-optimized dark background
Full portrait and landscape layout support
Session History
Completed sessions are automatically saved with round count, total elapsed time, and preset name — ready for a future Room DB migration.

Tech Stack
Layer	Technology
Language	Kotlin 1.9+
UI	Jetpack Compose + Material 3
Navigation	Navigation 3
State management	StateFlow / SharedFlow + ViewModel
Background execution	Foreground Service + Wake Lock
Audio	SoundPool (low-latency, up to 4 concurrent)
Haptics	Vibrator / VibratorManager waveform patterns
Architecture	MVVM + Repository
Build	Gradle 8.x with version catalog
Architecture
MainActivity
└── Jetpack Compose Nav host
    ├── HomeScreen        — configure timer, load presets, toggle alerts
    ├── TimerScreen       — countdown display, controls, session info bar
    ├── SettingsScreen    — per-alert sound selection with live preview
    └── PresetsScreen     — browse and save presets
AppViewModel             — orchestrates UI state and timer lifecycle
TimerEngine              — pure coroutine countdown with event emission
TimerService             — foreground service for background execution
SoundManager             — SoundPool playback + vibration
DataRepository           — in-memory preset and session storage
Requirements
Android 8.0+ (API 26)
Target / Compile SDK 36 (Android 15)
JVM 17
Building
# Clone the repo
git clone https://github.com/mdawg747/Timer-App.git
cd Timer-App

# Debug build
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
The debug APK is output to app/build/outputs/apk/debug/.

Permissions
Permission	Reason
FOREGROUND_SERVICE	Keep timer running when app is backgrounded
FOREGROUND_SERVICE_SPECIAL_USE	Android 12+ timer classification
POST_NOTIFICATIONS	Persistent timer notification with controls
VIBRATE	Haptic feedback
WAKE_LOCK	Prevent CPU sleep during long sessions
