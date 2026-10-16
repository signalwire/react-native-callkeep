# Changelog

## 4.4.0-signalwire.0 (fork, unreleased)

A maintained fork of [react-native-callkeep](https://github.com/react-native-webrtc/react-native-callkeep) (ISC, The CallKeep
Authors; see `LICENSE` and `AUTHORS`), based on upstream `master`
`71f80cb` (2026-09-14). npm `latest` upstream is 4.3.16; everything below
4.3.16 that is on upstream master is included. The package name is still
`react-native-callkeep`; it is not published to npm.

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
