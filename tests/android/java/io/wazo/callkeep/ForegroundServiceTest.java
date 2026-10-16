package io.wazo.callkeep;

import android.content.pm.ServiceInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Upstream #827: no outgoing audio with the screen locked on Android 14+.
 *
 * VoiceConnectionService called startForeground(FOREGROUND_SERVICE_TYPE_MICROPHONE, n):
 * the type constant went in as the notification id, and the two-argument form
 * starts the service with every type the app's manifest declares. The
 * documented declaration is camera|microphone; on Android 14+ a camera type
 * without FOREGROUND_SERVICE_CAMERA and a CAMERA grant makes the whole start
 * throw (SecurityException, caught and logged), so the call never ran as a
 * microphone foreground service and the microphone went silent once the app
 * left the foreground.
 */
public class ForegroundServiceTest {
    private static final int SDK_Q = 29, SDK_T = 33, SDK_U = 34;
    private static final int PHONE = ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL;
    private static final int MIC = ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
    private static final int CAMERA = ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA;
    private static final int MANIFEST = ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST;
    private static final int DATA_SYNC = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;

    private static ForegroundServiceTypes.Permissions granted(String... names) {
        Set<String> set = new HashSet<>(Arrays.asList(names));
        return set::contains;
    }

    private static void eq(int[] expected, int[] actual, String what) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(what + ": expected " + Arrays.toString(expected) + ", got " + Arrays.toString(actual));
        }
    }

    public static void main(String[] args) {
        // The id is the service's own constant, not a foreground-service type.
        if (ForegroundServiceTypes.NOTIFICATION_ID == MIC || ForegroundServiceTypes.NOTIFICATION_ID == 0) {
            throw new AssertionError("notification id " + ForegroundServiceTypes.NOTIFICATION_ID);
        }

        ForegroundServiceTypes.Permissions all = granted(
                "android.permission.FOREGROUND_SERVICE_PHONE_CALL", "android.permission.MANAGE_OWN_CALLS",
                "android.permission.FOREGROUND_SERVICE_MICROPHONE", "android.permission.RECORD_AUDIO",
                "android.permission.FOREGROUND_SERVICE_CAMERA", "android.permission.CAMERA");

        // Before Android 14 nothing changes: the manifest's types, as startForeground(id, n) used.
        eq(new int[]{MANIFEST}, ForegroundServiceTypes.candidates(SDK_Q, CAMERA | MIC, granted()), "API 29");
        eq(new int[]{MANIFEST}, ForegroundServiceTypes.candidates(SDK_T, CAMERA | MIC, all), "API 33");

        // #827: camera|microphone (the README's declaration), audio-only app (no CAMERA grant,
        // no FOREGROUND_SERVICE_CAMERA): start with the microphone type alone.
        eq(new int[]{MIC}, ForegroundServiceTypes.candidates(SDK_U, CAMERA | MIC,
                granted("android.permission.FOREGROUND_SERVICE_MICROPHONE", "android.permission.RECORD_AUDIO")),
                "camera|microphone without camera");

        // Everything allowed: every call type the manifest declares, then without the
        // while-in-use types in case the start comes from the background.
        eq(new int[]{PHONE | MIC | CAMERA, PHONE}, ForegroundServiceTypes.candidates(SDK_U, PHONE | MIC | CAMERA, all),
                "phoneCall|microphone|camera");
        eq(new int[]{PHONE | MIC, PHONE}, ForegroundServiceTypes.candidates(SDK_U, PHONE | MIC, all), "phoneCall|microphone");

        // Each type needs its FOREGROUND_SERVICE_* permission and its runtime prerequisite.
        eq(new int[]{PHONE}, ForegroundServiceTypes.candidates(SDK_U, PHONE | MIC,
                granted("android.permission.FOREGROUND_SERVICE_PHONE_CALL", "android.permission.MANAGE_OWN_CALLS",
                        "android.permission.FOREGROUND_SERVICE_MICROPHONE")),
                "microphone without RECORD_AUDIO");
        eq(new int[]{MIC}, ForegroundServiceTypes.candidates(SDK_U, PHONE | MIC,
                granted("android.permission.MANAGE_OWN_CALLS",
                        "android.permission.FOREGROUND_SERVICE_MICROPHONE", "android.permission.RECORD_AUDIO")),
                "phoneCall without FOREGROUND_SERVICE_PHONE_CALL");

        // Types callkeep does not use are left out.
        eq(new int[]{MIC}, ForegroundServiceTypes.candidates(SDK_U, MIC | DATA_SYNC, all), "dataSync dropped");

        // Nothing usable, or nothing declared: the old call, which logs Android's reason.
        eq(new int[]{MANIFEST}, ForegroundServiceTypes.candidates(SDK_U, CAMERA | MIC, granted()), "nothing allowed");
        eq(new int[]{MANIFEST}, ForegroundServiceTypes.candidates(SDK_U, 0, all), "no type declared");

        System.out.println("ForegroundServiceTest: ok");
    }
}
