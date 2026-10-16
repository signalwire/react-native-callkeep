const { NativeModules } = require('react-native');
const RNCallKeep = require('../../index').default;

const native = NativeModules.RNCallKeep;

describe('audio routes (Android)', () => {
  it('forwards setAudioRoute and returns the native promise', async () => {
    native.setAudioRoute.mockReturnValueOnce(Promise.resolve(true));
    await expect(RNCallKeep.setAudioRoute('u1', 'Speaker')).resolves.toBe(true);
    expect(native.setAudioRoute).toHaveBeenCalledWith('u1', 'Speaker');
  });

  it('forwards getAudioRoutes', async () => {
    const routes = [{ name: 'Speaker', type: 'Speaker', selected: true }];
    native.getAudioRoutes.mockReturnValueOnce(Promise.resolve(routes));
    await expect(RNCallKeep.getAudioRoutes()).resolves.toEqual(routes);
  });

  it('forwards toggleAudioRouteSpeaker on Android', () => {
    RNCallKeep.toggleAudioRouteSpeaker('u1', false);
    expect(native.toggleAudioRouteSpeaker).toHaveBeenCalledWith('u1', false);
  });
});

describe('setAudioRouteBluetoothDevice', () => {
  it('forwards the uuid and the device address on Android', async () => {
    native.setAudioRouteBluetoothDevice.mockReturnValueOnce(Promise.resolve(true));
    await expect(RNCallKeep.setAudioRouteBluetoothDevice('u1', 'AA:BB:CC:DD:EE:FF')).resolves.toBe(true);
    expect(native.setAudioRouteBluetoothDevice).toHaveBeenCalledWith('u1', 'AA:BB:CC:DD:EE:FF');
  });
});
