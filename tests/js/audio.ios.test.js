const { NativeModules, Platform } = require('react-native');

// The module registry is per test file, so this does not leak into the Android tests.
Platform.OS = 'ios';
const RNCallKeep = require('../../index').default;

describe('setAudioRouteBluetoothDevice on iOS', () => {
  it('rejects without calling native code (iOS picks a device by port name in setAudioRoute)', async () => {
    await expect(RNCallKeep.setAudioRouteBluetoothDevice('u1', 'AA:BB')).rejects.toThrow(/Android only/);
    expect(NativeModules.RNCallKeep.setAudioRouteBluetoothDevice).not.toHaveBeenCalled();
  });
});
