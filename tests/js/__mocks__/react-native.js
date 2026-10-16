// Minimal react-native stand-in for the JS-layer tests: the native module is
// a jest mock, and the event emitter is a plain in-process emitter.
const listeners = new Map();

class NativeEventEmitter {
  addListener(name, handler) {
    const set = listeners.get(name) ?? new Set();
    set.add(handler);
    listeners.set(name, set);
    return { remove: () => set.delete(handler) };
  }

  emit(name, payload) {
    for (const handler of listeners.get(name) ?? []) {
      handler(payload);
    }
  }
}

const RNCallKeep = new Proxy(
  {},
  {
    get(target, prop) {
      if (!(prop in target)) {
        target[prop] = jest.fn();
      }
      return target[prop];
    },
  }
);

const Platform = { OS: process.env.CALLKEEP_TEST_OS || 'android', Version: 35 };

module.exports = {
  NativeModules: { RNCallKeep },
  NativeEventEmitter,
  Platform,
  Alert: { alert: jest.fn() },
  __resetListeners: () => listeners.clear(),
};
