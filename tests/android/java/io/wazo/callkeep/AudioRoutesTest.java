package io.wazo.callkeep;

import android.telecom.CallAudioState;

import java.util.Arrays;
import java.util.List;

/** Plain-Java checks of AudioRoutes (only CallAudioState's compile-time constants are used). */
public class AudioRoutesTest {
    private static void eq(Object expected, Object actual, String what) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(what + ": expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) {
        eq(Arrays.asList("EARPIECE", "SPEAKER"),
                AudioRoutes.routeNames(CallAudioState.ROUTE_EARPIECE | CallAudioState.ROUTE_SPEAKER),
                "earpiece and speaker");
        eq(Arrays.asList("EARPIECE", "BLUETOOTH", "WIRED_HEADSET", "SPEAKER", "STREAMING"),
                AudioRoutes.routeNames(31), "every route, Telecom's bit order");
        eq(Arrays.asList(), AudioRoutes.routeNames(0), "no route");

        List<Integer> routes = AudioRoutes.routes(CallAudioState.ROUTE_SPEAKER | CallAudioState.ROUTE_WIRED_HEADSET);
        eq(Arrays.asList(CallAudioState.ROUTE_WIRED_HEADSET, CallAudioState.ROUTE_SPEAKER), routes, "route bits");

        eq("Phone", AudioRoutes.callKeepType(CallAudioState.ROUTE_EARPIECE), "earpiece type");
        eq("Speaker", AudioRoutes.callKeepType(CallAudioState.ROUTE_SPEAKER), "speaker type");
        eq("Headset", AudioRoutes.callKeepType(CallAudioState.ROUTE_WIRED_HEADSET), "headset type");
        eq("Bluetooth", AudioRoutes.callKeepType(CallAudioState.ROUTE_BLUETOOTH), "bluetooth type");
        eq(null, AudioRoutes.callKeepType(CallAudioState.ROUTE_STREAMING), "streaming has no callkeep type");

        eq(CallAudioState.ROUTE_SPEAKER, AudioRoutes.speakerRoute(true), "speaker on");
        eq(CallAudioState.ROUTE_WIRED_OR_EARPIECE, AudioRoutes.speakerRoute(false),
                "speaker off goes to the headset or the earpiece, as setAudioRoute does");
        System.out.println("AudioRoutesTest: ok");
    }
}
