# FoldHome

Use a different launcher on each screen of a foldable phone, for example
[Niagara Launcher](https://niagaralauncher.app/) on the cover screen and One UI
Home on the inner screen. No root, no Shizuku, no Tasker.

The APK is about 20 KB. It is written in plain Java with no dependencies.

## How it works

Android only allows one default home app, so FoldHome takes that role itself and
acts as a proxy:

- **Home → right launcher.** When the system goes home, FoldHome opens the
  launcher you picked for the current fold state. It never shows its own UI.
  It starts the launcher with a `MAIN`/`HOME` intent, and the system treats that
  launcher as a real home activity. Gestures and recents keep working.
- **Instant switch on fold.** A small foreground service keeps the process
  alive, and it listens for configuration changes. The device counts as unfolded
  when `smallestScreenWidthDp >= 600`. If a launcher is on screen when you fold
  or unfold, FoldHome opens the other one immediately. If you're inside an app,
  nothing happens, and the new launcher appears on your next Home gesture.
- **Editing your layout.** Some launchers (One UI Home, for instance) disable
  removing icons and widgets when they are not the default home app. The
  **Edit layout** Quick Settings tile hands the home role to another launcher
  while you edit, and takes it back when you tap the tile again.

## Permissions

| Permission | Why |
|---|---|
| Usage access | Tells whether a launcher is currently on screen when you fold or unfold |
| Appear on top | Android blocks activity starts from the background. While an overlay is visible the app is exempt, so FoldHome shows a 1 px invisible overlay for a moment while it switches |
| Foreground service (special use) | Keeps the fold watcher alive. Notification permission is never requested, so no notification is shown |
| Run at startup | Restarts the watcher after a reboot or an update |

FoldHome has no internet permission and collects no data.

## Setup

1. Install the APK and open **FoldHome**.
2. Pick a launcher for **Folded** and one for **Unfolded**.
3. Tap **Set FoldHome as default home**.
4. Grant **Usage access** and **Appear on top** using the buttons.
5. On Samsung phones, set FoldHome's battery usage to **Unrestricted** so the
   watcher isn't killed.
6. Optional: add the **Edit layout** tile to Quick Settings.

If you install over adb, you can grant the two special permissions directly:

```sh
adb shell appops set app.foldhome GET_USAGE_STATS allow
adb shell appops set app.foldhome SYSTEM_ALERT_WINDOW allow
adb shell cmd role add-role-holder android.app.role.HOME app.foldhome 0
```

## Building

You need JDK 17+ and the Android SDK (platform 36). Point `local.properties`
at your SDK (`sdk.dir=/path/to/Android/Sdk`), or set `ANDROID_HOME`, then run:

```sh
./gradlew assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

The release build is minified, and for simplicity it is signed with the debug
key.

## Tested on

Samsung Galaxy Z Fold7 (One UI, Android 17) with Niagara Launcher and One UI Home.

## License

[MIT](LICENSE)
