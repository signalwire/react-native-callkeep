# Changelog

## 4.4.1

### Fixes

- Android: the ongoing-call notification opens the app's launcher activity when no React activity exists. With `endCallsOnHostDestroy: false` a call outlives its activity (Back on the root screen); the notification rebuilt for a call that started then had no content intent, so tapping it did nothing.

## 4.4.0

The first npm release of the fork, as **`@signalwire/react-native-callkeep`**.
It contains everything in 4.4.0-signalwire.0 and 4.4.0-signalwire.1 below
(those were git-only pre-releases, never on npm); there are no code changes
beyond them.

### Packaging

- Package name `@signalwire/react-native-callkeep` (was `react-native-callkeep`). Imports change to `'@signalwire/react-native-callkeep'`; the JS API, the `RNCallKeep` native module and pod, and the `io.wazo.callkeep` Android package are unchanged. See "Migrating from `react-native-callkeep`" in the README.
- `repository`, `bugs` and `homepage` (and so the podspec's `source`/`homepage`) point at [signalwire/react-native-callkeep](https://github.com/signalwire/react-native-callkeep).
- A `files` allowlist: the tarball holds only the JS, types, Android and iOS sources, the podspec, the privacy manifest, LICENSE, AUTHORS, README and CHANGELOG.
- `prepublishOnly` runs the Jest suite and `test:types`.

### Fixes

- TypeScript: `index.d.ts` is a module. It was an ambient `declare module 'react-native-callkeep'`, which types only that name; importing `'@signalwire/react-native-callkeep'` failed with TS2306 ("is not a module"). `npm run test:types` type-checks an import by the package name.

## 4.4.0-signalwire.1 (git pre-release, part of 4.4.0)

Fixes for open upstream issues and pull requests, on top of 4.4.0-signalwire.0.

### Fixes

- Android 14+: the call's foreground service starts with only the declared types the app may use (`phoneCall`, `microphone`, `camera`, each with its `FOREGROUND_SERVICE_*` permission and runtime permission), then without `microphone`/`camera` if that start is refused. It used `startForeground(FOREGROUND_SERVICE_TYPE_MICROPHONE, notification)`: the type constant was the notification id, and every declared type applied, so the documented `camera|microphone` in an app without the CAMERA grant refused the whole start and the microphone went silent with the screen locked ([#827](https://github.com/react-native-webrtc/react-native-callkeep/issues/827)).
- Android: `backToForeground()` shows the app over the lock screen and turns the screen on (`setShowWhenLocked`/`setTurnScreenOn`, window flags before API 27) until the last call ends. It added `WindowManager.LayoutParams` flags to the launch Intent's flags, where those bits are `FLAG_ACTIVITY_NEW_DOCUMENT`, `BROUGHT_TO_FRONT` and `RESET_TASK_IF_NEEDED` ([#779](https://github.com/react-native-webrtc/react-native-callkeep/issues/779), [#667](https://github.com/react-native-webrtc/react-native-callkeep/issues/667); from [#684](https://github.com/react-native-webrtc/react-native-callkeep/pull/684), Rami Jarrar).
- Android: switching `selfManaged` off after a self-managed phone account was registered works: Telecom refuses to change the account's kind (`IllegalArgumentException: cannot change a self-managed phone account ... to other kinds of phone account`, which crashed `setup()` upstream and left the old account in 4.4.0-signalwire.0), so callkeep unregisters it and registers the new one ([#722](https://github.com/react-native-webrtc/react-native-callkeep/issues/722)).
- Android: in self-managed mode `hasPhoneAccount()`/`checkPhoneAccountEnabled()` return false when Telecom refused the phone account (devices without `android.software.telecom`, non-voice-capable tablets); they always returned true ([#761](https://github.com/react-native-webrtc/react-native-callkeep/issues/761)).

### Open upstream pull requests taken

- Android: `@RequiresApi(S)` on the `TelephonyCallback` listener, which only exists from API 31 ([#741](https://github.com/react-native-webrtc/react-native-callkeep/pull/741), Rami Jarrar; cherry-picked).
- iOS: a privacy manifest (`RNCallKeepPrivacyInfo` resource bundle) declaring the `NSUserDefaults` use (reason `CA92.1`); no collected data, no tracking ([#770](https://github.com/react-native-webrtc/react-native-callkeep/pull/770), ujeon; rewritten, its collected-data entry dropped).

### Upstream issues already fixed by 4.4.0-signalwire.0

- [#833](https://github.com/react-native-webrtc/react-native-callkeep/issues/833): clearing the app from Recents killed the process (and any other foreground service) through `onHostDestroy`'s `killProcess`; the fork no longer kills, and `endCallsOnHostDestroy: false` keeps the call. [#534](https://github.com/react-native-webrtc/react-native-callkeep/issues/534) (the call should end when the app is killed) is the default, `endCallsOnHostDestroy: true`.

### Behaviour changes

- Android, self-managed: `hasPhoneAccount()` can now be false (only where Telecom refused the account; before the first registration it is still true).
- Android: when the phone account changes from self-managed to managed, callkeep unregisters the old account first (any call on it ends). Other registration errors still leave the existing account alone.
- Android: after `backToForeground()` the app shows over the lock screen while a call is up (it was meant to; the flags never applied). It stops when the last call ends, or 30 s after a `backToForeground()` with no call. Starting the activity from the background is still subject to Android 10+'s background-activity-start limits.
- Android: the ongoing-call notification's id is now `0x43414c4c` (was 128).

## 4.4.0-signalwire.0 (git pre-release, part of 4.4.0)

A maintained fork of [react-native-callkeep](https://github.com/react-native-webrtc/react-native-callkeep) (ISC, The CallKeep
Authors; see `LICENSE` and `AUTHORS`), based on upstream `master`
`71f80cb` (2026-09-14). npm `latest` upstream is 4.3.16; everything below
4.3.16 that is on upstream master is included. At this pre-release the package
name was still `react-native-callkeep` and it was not on npm.

### From upstream master since 4.3.16 (unreleased upstream)

- Android: drop the duplicate `displayIncomingCall`/`startCall` @ReactMethod overloads, which the New Architecture refuses at launch ([#816](https://github.com/react-native-webrtc/react-native-callkeep/pull/816), Irfan Wani).
- iOS: reject promises with a string, not an NSException ([#845](https://github.com/react-native-webrtc/react-native-callkeep/pull/845), Alex Korotkov).
- Android: set the Gradle `namespace` for AGP 8 ([#781](https://github.com/react-native-webrtc/react-native-callkeep/pull/781), gasolin).
- Android: keep cold-start events until React is ready, and return fresh `getInitialEvents` snapshots ([#875](https://github.com/react-native-webrtc/react-native-callkeep/pull/875), Sylvain Boily).
- Android: no phone-number permission for self-managed incoming calls ([#876](https://github.com/react-native-webrtc/react-native-callkeep/pull/876), Sylvain Boily, following [#685](https://github.com/react-native-webrtc/react-native-callkeep/pull/685) by Donald Wong).
- Android: `stopForeground(STOP_FOREGROUND_REMOVE)` ([#742](https://github.com/react-native-webrtc/react-native-callkeep/pull/742), Rami Jarrar).

### Open upstream pull requests taken (cherry-picked, authors kept)

- iOS: events sent before the module is initialised were dropped (`_delayedEvents` nil) ([#877](https://github.com/react-native-webrtc/react-native-callkeep/pull/877), Min Hu; issues [#844](https://github.com/react-native-webrtc/react-native-callkeep/issues/844), [#855](https://github.com/react-native-webrtc/react-native-callkeep/issues/855)).
- iOS: `getAudioRoutes`/`setAudioRoute` no longer activate the audio session outside CallKit ([#863](https://github.com/react-native-webrtc/react-native-callkeep/pull/863), siebeg-ideal).
- iOS: no forced 44.1 kHz sample rate / 5 ms IO buffer in `configureAudioSession` ([#858](https://github.com/react-native-webrtc/react-native-callkeep/pull/858), fhilleriteau).
- Android: keep the foreground service while another call is up ([#826](https://github.com/react-native-webrtc/react-native-callkeep/pull/826), Fedor Zarubin).
- Android < 12: no `Looper.loop()`/`quit()` on React Native's native-modules thread when listening to the phone state ([#825](https://github.com/react-native-webrtc/react-native-callkeep/pull/825), Jonathan Bolduc; supersedes [#836](https://github.com/react-native-webrtc/react-native-callkeep/pull/836)).
- iOS: `didFailCallAction` event when CallKit refuses a requested action ([#878](https://github.com/react-native-webrtc/react-native-callkeep/pull/878), Michael Jerris).

### Fork changes

- Android: `setAudioRoute` rejects (`E_NO_CONNECTION`) instead of never settling when there is no call for the uuid.
- iOS: `setAudioRoute` rejects (`E_NO_AUDIO_ROUTE`) instead of never settling when no input has that name.
- Android: `toggleAudioRouteSpeaker(uuid, false)` uses `ROUTE_WIRED_OR_EARPIECE` (was `ROUTE_EARPIECE`, which left a wired headset).
- Android: `didChangeAudioRoute` adds `supportedRoutes`, `bluetoothDevice` and `bluetoothDevices` (API 28+).
- Android: `getAudioRoutes()` lists the call's routes from its `CallAudioState` (one entry per Bluetooth device on API 28+, with `address`/`deviceName`); typed `Promise<AudioRoute[]>` ([#748](https://github.com/react-native-webrtc/react-native-callkeep/issues/748), [#810](https://github.com/react-native-webrtc/react-native-callkeep/issues/810)).
- Android: `setAudioRouteBluetoothDevice(uuid, address)`: `requestCallEndpointChange` (API 34+), `requestBluetoothAudio` (API 28-33).
- Android: `onHostDestroy` never kills the process, so the `endCall` it triggers reaches JS; `setup({ android: { endCallsOnHostDestroy: false } })` keeps calls after the activity is destroyed ([#724](https://github.com/react-native-webrtc/react-native-callkeep/pull/724), [#797](https://github.com/react-native-webrtc/react-native-callkeep/issues/797)).
- Android: `didFailCallAction` (`action: 'startCall'`, `errorCode: 'ConnectionRefused'`) when Telecom refuses an outgoing call (`onCreateOutgoingConnectionFailed`, which callkeep did not implement).
- Android: `setup()` no longer crashes on devices without Telecom ([#815](https://github.com/react-native-webrtc/react-native-callkeep/issues/815), [#761](https://github.com/react-native-webrtc/react-native-callkeep/issues/761)).
- Tests: Jest for the JS layer, `javac` of the Android sources against android-35 and react-android 0.81.5 with off-device Java tests, `clang -fsyntax-only` of `RNCallKeep.m` (`tests/README.md`). The `react-native` devDependency is dropped (it stays a peer dependency).

### Behaviour changes to know about

- `getAudioRoutes()` on Android returns what Telecom offers the call, not every audio device. Bluetooth entries keep `name: 'Bluetooth'`.
- `setAudioRoute` can now reject where it used to hang.
- After Back on the root screen the app process stays alive (it was killed).
