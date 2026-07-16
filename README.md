# AD-GPT Android

AD-GPT is a Kotlin Android Studio project built with Jetpack Compose, Material 3, MVVM, Clean Architecture, Hilt, Navigation Compose, Retrofit/OkHttp, Coil, Room, DataStore, and AndroidX Media3.

## Startup Experience

- The startup video is packaged from the root `video/` directory through Gradle asset sources.
- The current intro video is `video/Same_but_in_vertical_not_horiz.mp4`.
- Replace the file in `video/` with any `.mp4`, `.webm`, or `.mkv` file to change the intro without code changes.
- The app auto-detects startup videos in `app/src/main/assets/startup/` first, then the packaged root asset directory.
- If the video is missing, fails to decode, or audio cannot play, AD-GPT skips gracefully into the main interface.
- Click, touch, `Space`, `Enter`, or `Escape` skips the intro and starts the cinematic fade/blur transition.

## Open in Android Studio

1. Open this folder as an Android Studio project.
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or device running Android 8.0/API 26 or newer.

## Applying Updates in Android Studio

1. Click `Git > Pull` or run `git pull` in the terminal if you are using the GitHub copy.
2. Click `File > Sync Project with Gradle Files`.
3. Select the `app` run configuration.
4. Click `Run`.
5. If Android Studio shows stale errors, use `File > Invalidate Caches`, restart, then sync again.

The project targets API 35 and uses Android Gradle Plugin `8.7.3` for broad compatibility with current Android Studio installs.

## Structure

```text
app/src/main/java/com/adgpt/app
├── data
│   ├── local          Room-shaped data models plus local DAO
│   ├── network        Retrofit DTOs/API contract
│   ├── provider       AI provider abstraction
│   └── repository     Repository implementations
├── di                 Hilt modules
├── domain             Models, repositories, use cases
└── presentation
    ├── chat           Main AD-GPT interface
    ├── navigation     Navigation Compose graph
    ├── settings       Settings surface
    ├── startup        Media3 intro and transition flow
    └── theme          Material 3 theme
```

## Notes

- The default AI provider is local demo mode so the app launches without external keys.
- Paste an API key into the sidebar `Add API` card to detect OpenAI, Anthropic, Gemini, Groq, OpenRouter, or Mistral-style keys.
- Add a production provider by implementing `AiProvider` and binding it in `AppModule`.
- The local chat DAO currently uses an in-memory implementation to avoid Windows SQLite native verifier issues during KAPT builds.
- The startup screen is intentionally pure black with only the Media3 video surface visible before reveal.
