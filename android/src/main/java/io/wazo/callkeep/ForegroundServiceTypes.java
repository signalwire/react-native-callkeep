/*
 * Copyright (c) 2016-2019 The CallKeep Authors (see the AUTHORS file)
 * SPDX-License-Identifier: ISC, MIT
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package io.wazo.callkeep;

import android.content.pm.ServiceInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * The foreground-service types VoiceConnectionService starts with.
 *
 * startForeground(id, notification) starts with every type the app's manifest
 * declares. From Android 14 each type must be allowed (its FOREGROUND_SERVICE_*
 * permission and its runtime prerequisite) or the whole start is refused, so
 * one unusable type (the documented camera|microphone, in an audio-only app)
 * left the call without a foreground service and the microphone went silent
 * once the app left the foreground (upstream #827). Kept free of Android calls
 * so it can be tested off-device.
 */
final class ForegroundServiceTypes {
    private ForegroundServiceTypes() {}

    /** The ongoing-call notification's id. It used to be FOREGROUND_SERVICE_TYPE_MICROPHONE (128). */
    static final int NOTIFICATION_ID = 0x43414c4c;

    interface Permissions {
        boolean granted(String permission);
    }

    /** The call types, each with its FOREGROUND_SERVICE_* permission and its runtime prerequisite. */
    private static final Object[][] CALL_TYPES = {
        { ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL,
          "android.permission.FOREGROUND_SERVICE_PHONE_CALL", "android.permission.MANAGE_OWN_CALLS" },
        { ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
          "android.permission.FOREGROUND_SERVICE_MICROPHONE", "android.permission.RECORD_AUDIO" },
        { ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA,
          "android.permission.FOREGROUND_SERVICE_CAMERA", "android.permission.CAMERA" },
    };

    /** Types that need a while-in-use permission, which Android refuses to a start from the background. */
    private static final int WHILE_IN_USE =
        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE | ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA;

    /**
     * The types to try, in order, for startForeground(id, notification, type).
     * Before Android 14, and when nothing usable is declared, the manifest's
     * types (what startForeground(id, notification) does). Otherwise the
     * declared call types that are allowed, then, if that start is refused,
     * the same without microphone and camera.
     */
    static int[] candidates(int sdkInt, int declaredTypes, Permissions permissions) {
        if (sdkInt < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new int[]{ ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST };
        }
        int allowed = 0;
        for (Object[] type : CALL_TYPES) {
            int bit = (Integer) type[0];
            if ((declaredTypes & bit) != 0
                    && permissions.granted((String) type[1]) && permissions.granted((String) type[2])) {
                allowed |= bit;
            }
        }
        if (allowed == 0) {
            return new int[]{ ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST };
        }
        List<Integer> out = new ArrayList<>();
        out.add(allowed);
        int background = allowed & ~WHILE_IN_USE;
        if (background != 0 && background != allowed) {
            out.add(background);
        }
        int[] result = new int[out.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = out.get(i);
        }
        return result;
    }
}
