package io.wazo.callkeep;

import android.telecom.CallAudioState;

/** Plain-Java checks of AudioRoutes (only CallAudioState's compile-time constants are used). */
public class AudioRoutesTest {
    private static void eq(Object expected, Object actual, String what) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(what + ": expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) {
        eq(CallAudioState.ROUTE_SPEAKER, AudioRoutes.speakerRoute(true), "speaker on");
        eq(CallAudioState.ROUTE_WIRED_OR_EARPIECE, AudioRoutes.speakerRoute(false),
                "speaker off goes to the headset or the earpiece, as setAudioRoute does");
        System.out.println("AudioRoutesTest: ok");
    }
}
