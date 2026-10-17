// Type-checks the published declarations the way an app imports them: by the
// package's own name. index.d.ts must be a module; an ambient
// `declare module 'react-native-callkeep'` gives TS2306 ("is not a module")
// for '@signalwire/react-native-callkeep'.
import RNCallKeep, {
  AudioSessionCategoryOption,
  CONSTANTS,
  type AudioRoute,
  type EventsPayload
} from '@signalwire/react-native-callkeep';

const reason: number = CONSTANTS.END_CALL_REASONS.FAILED;
const option: AudioSessionCategoryOption = AudioSessionCategoryOption.allowBluetooth;

RNCallKeep.setup({
  ios: { appName: 'Example' },
  android: {
    alertTitle: 'Permissions',
    alertDescription: 'This app needs to make calls',
    cancelButton: 'Cancel',
    okButton: 'OK',
    additionalPermissions: []
  }
});
const listener = RNCallKeep.addEventListener('didFailCallAction', (event: EventsPayload['didFailCallAction']) => {
  void event;
});
listener.remove();
const routes: Promise<AudioRoute[] | void> = RNCallKeep.getAudioRoutes();

export { option, reason, routes };
