# WaveLock — Audio-Reactive Live Wallpaper (Skeleton)

A working Android Studio project skeleton: a Live Wallpaper that reacts to
whatever music/audio is playing on the device (Spotify, YouTube Music,
etc.), shown behind the lock screen clock/notifications.

## How it works

1. **MainActivity** — requests `RECORD_AUDIO` permission, then triggers the
   system's MediaProjection consent dialog (Android reuses the screen-capture
   dialog for playback-audio capture too — the wording will mention
   "recording/casting" even though only audio is read).
2. **AudioCaptureService** — a foreground service that uses
   `AudioPlaybackCaptureConfiguration` (Android 10+) to tap system playback
   audio, runs a small FFT (`SimpleFFT.kt`) on it, and publishes 32 magnitude
   bands to `AudioDataBus`.
3. **VisualizerWallpaperService** — the actual Live Wallpaper engine. It
   reads the latest bands and draws a bar equalizer at ~30fps. **This is the
   file to edit** to port the look of your Replit visualizer.

## Setup

1. Open the `WaveLockWallpaper/` folder in Android Studio (Koala+).
2. Let Gradle sync — it will pull the AndroidX/Kotlin dependencies.
3. Run on a **physical device or emulator on API 29+** (playback capture
   doesn't work below Android 10).
4. In the app: tap "Grant Audio Capture Access", approve both dialogs, then
   tap "Set as Live Wallpaper".
5. In system Settings → Wallpaper & style, choose "different wallpaper for
   lock screen" and pick WaveLock there too.

## Known limitations (real, not skippable)

- **Not every app's audio can be captured.** Apps can opt out of playback
  capture via `AudioAttributes.setAllowedCapturePolicy()`. Most streaming
  apps allow it; some (e.g. certain DRM-protected content) block it.
- **OEM lock screens vary.** Stock Android/Pixel supports third-party live
  wallpapers on the lock screen since Android 13. Samsung One UI and some
  other skins restrict lock-screen wallpaper choice more heavily — test on
  your actual device.
- **The consent dialog reappears** if the service is killed (e.g. by
  aggressive battery optimization) — MediaProjection tokens don't survive
  process death. You may want a persistent notification "tap to resume"
  action for production use.
- **Battery**: continuous audio capture + 30fps drawing on an always-visible
  lock screen wallpaper will cost noticeably more battery than a static
  wallpaper. Worth throttling the frame rate or pausing rendering when the
  screen is fully off (`onVisibilityChanged` already skips work when not
  visible, but AOD/ambient display is a separate mode to test).

## Next steps to make it yours

- Replace the bar-drawing block in `VisualizerWallpaperService.render()`
  with your wave/particle style from window-music-wave.
- Add a settings screen (color themes, sensitivity, bar vs. wave style).
- Consider `SurfaceView` + `GLSurfaceView`/OpenGL instead of Canvas if you
  want smoother gradients or particle effects — Canvas is fine for bars.
