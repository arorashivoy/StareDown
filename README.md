# StareDown

An Android staring-contest game: hold the front camera on your face and try not to
blink. ML Kit's face detector watches your eyes, the timer stops the moment they
close, and your time goes on a shared leaderboard.

Android coursework, May 2025. Kotlin and XML views.

> A screen recording of a round belongs here.

## How it works

- **Blink detection** — Camera2 feeds frames to **ML Kit face detection**, which
  reports per-eye open probability. The round ends when that probability drops
  past the threshold, which is what makes the timing feel fair rather than
  frame-counted.
- **Leaderboard** — scores go to **Firebase Realtime Database**, read back into a
  `RecyclerView` through `LeaderboardAdapter`.
- **Accounts** — Firebase Auth, with a username set on first run.
- **Notifications** — Firebase Cloud Messaging, handled in
  `FirebaseMessageReceiver`.
- `RequestCamera` handles the runtime camera permission, which has to be granted
  before a round can start.

## Building it

You need your own Firebase project — `app/google-services.json` in this repository
points at mine. Replace it with yours from the Firebase console, then:

```sh
./gradlew installDebug
```

`local.properties` is machine-specific and is not committed; Android Studio
regenerates it.
