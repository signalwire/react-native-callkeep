# Tests

| Command | What it checks |
|---|---|
| `npm test` | Jest, JS layer (`index.js`, `actions.js`) against a stand-in `react-native` (`tests/js/__mocks__`). |
| `npm run test:types` | `tsc` of `tests/types/consumer.ts`, which imports `@signalwire/react-native-callkeep` as an app does, against `index.d.ts`. |
| `npm run test:android` | `javac` of `android/src/main/java` against `android.jar` (API 35) and react-android 0.81.5 / AndroidX from the Gradle cache, then the plain-Java tests in `tests/android/java`. Needs `ANDROID_HOME` and a Gradle cache that has those artifacts (any RN 0.81 app build fills it). |
| `npm run test:ios` | `clang -fsyntax-only` of `ios/RNCallKeep/RNCallKeep.m` against the iOS Simulator SDK and React Native's headers, and `plutil -lint` of the privacy manifest. Set `REACT_NATIVE_DIR` to a react-native package. |

`tests/android/*.kt` are instrumentation fixtures that run inside a host app
(see `tests/android/README.md`); they are not part of these commands.
