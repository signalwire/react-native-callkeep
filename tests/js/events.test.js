const { NativeModules } = require('react-native');
const RNCallKeep = require('../../index').default;
const { emit } = require('../../index');

describe('event listeners', () => {
  it('delivers every native event to its listener', () => {
    const pairs = {
      didReceiveStartCallAction: 'RNCallKeepDidReceiveStartCallAction',
      answerCall: 'RNCallKeepPerformAnswerCallAction',
      endCall: 'RNCallKeepPerformEndCallAction',
      didActivateAudioSession: 'RNCallKeepDidActivateAudioSession',
      didDeactivateAudioSession: 'RNCallKeepDidDeactivateAudioSession',
      didDisplayIncomingCall: 'RNCallKeepDidDisplayIncomingCall',
      didPerformSetMutedCallAction: 'RNCallKeepDidPerformSetMutedCallAction',
      didToggleHoldCallAction: 'RNCallKeepDidToggleHoldAction',
      didPerformDTMFAction: 'RNCallKeepDidPerformDTMFAction',
      didResetProvider: 'RNCallKeepProviderReset',
      checkReachability: 'RNCallKeepCheckReachability',
      didLoadWithEvents: 'RNCallKeepDidLoadWithEvents',
      showIncomingCallUi: 'RNCallKeepShowIncomingCallUi',
      silenceIncomingCall: 'RNCallKeepOnSilenceIncomingCall',
      createIncomingConnectionFailed: 'RNCallKeepOnIncomingConnectionFailed',
      didChangeAudioRoute: 'RNCallKeepDidChangeAudioRoute',
      onHasActiveCall: 'RNCallKeepHasActiveCall',
      didFailCallAction: 'RNCallKeepDidFailCallAction',
    };
    for (const [type, nativeName] of Object.entries(pairs)) {
      const handler = jest.fn();
      const subscription = RNCallKeep.addEventListener(type, handler);
      const payload = { callUUID: `uuid-${type}` };
      emit(nativeName, payload);
      expect(handler).toHaveBeenCalledWith(payload);
      subscription.remove();
      handler.mockClear();
      emit(nativeName, payload);
      expect(handler).not.toHaveBeenCalled();
    }
  });

  it('parses an Android didDisplayIncomingCall payload string', () => {
    const handler = jest.fn();
    const subscription = RNCallKeep.addEventListener('didDisplayIncomingCall', handler);
    emit('RNCallKeepDidDisplayIncomingCall', { callUUID: 'a', payload: '{"callId":"x"}' });
    expect(handler).toHaveBeenCalledWith({ callUUID: 'a', payload: { callId: 'x' } });
    subscription.remove();
  });

  it('removes every listener of a type', () => {
    const handler = jest.fn();
    RNCallKeep.addEventListener('endCall', handler);
    RNCallKeep.removeEventListener('endCall');
    emit('RNCallKeepPerformEndCallAction', { callUUID: 'a' });
    expect(handler).not.toHaveBeenCalled();
    expect(NativeModules.RNCallKeep).toBeDefined();
  });
});
