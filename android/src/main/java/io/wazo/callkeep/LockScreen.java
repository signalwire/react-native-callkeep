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

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * Shows the app over the lock screen for backToForeground(), until the calls end.
 *
 * backToForeground used to add WindowManager.LayoutParams flags to the launch
 * Intent's flags; on an Intent those bits mean NEW_DOCUMENT, BROUGHT_TO_FRONT
 * and RESET_TASK_IF_NEEDED, so the app never came up over the lock screen
 * (upstream #779, #667, PR #684). Window flags belong to the activity:
 * setShowWhenLocked/setTurnScreenOn from API 27, the window flags before.
 * They are cleared when the last call ends, so the app does not stay
 * reachable without unlocking.
 */
final class LockScreen {
    private static final String TAG = "RNCallKeep";
    /** How long a backToForeground() with no call keeps the app over the lock screen. */
    private static final long NO_CALL_TIMEOUT_MS = 30_000;

    @Nullable private static Handler main;
    @Nullable private static WeakReference<Activity> shown;
    @Nullable private static Application pendingApplication;
    @Nullable private static Application.ActivityLifecycleCallbacks pending;

    private LockScreen() {}

    private static synchronized Handler main() {
        if (main == null) {
            main = new Handler(Looper.getMainLooper());
        }
        return main;
    }

    /** The launch Intent's flags: bring a running activity to the front, else start a task. */
    static int launchFlags(boolean activityRunning) {
        return activityRunning ? Intent.FLAG_ACTIVITY_REORDER_TO_FRONT : Intent.FLAG_ACTIVITY_NEW_TASK;
    }

    /** Shows this activity over the lock screen and turns the screen on. */
    static void show(@NonNull Activity activity) {
        activity.runOnUiThread(() -> {
            cancelPending();
            apply(activity, true);
            shown = new WeakReference<>(activity);
        });
        releaseIfNoCall();
    }

    /** Shows the next activity the app creates (the one backToForeground starts) over the lock screen. */
    static void showNextActivity(@NonNull Application application) {
        main().post(() -> {
            cancelPending();
            pending = new SimpleCallbacks() {
                @Override
                public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                    cancelPending();
                    apply(activity, true);
                    shown = new WeakReference<>(activity);
                }
            };
            pendingApplication = application;
            application.registerActivityLifecycleCallbacks(pending);
        });
        releaseIfNoCall();
    }

    /** The calls have ended: the app shows over the lock screen no more. */
    static void release() {
        main().post(() -> {
            cancelPending();
            Activity activity = shown != null ? shown.get() : null;
            shown = null;
            if (activity != null) {
                apply(activity, false);
                Log.d(TAG, "[LockScreen] no longer shown over the lock screen");
            }
        });
    }

    /** backToForeground() without a call (yet): release unless one starts soon. */
    private static void releaseIfNoCall() {
        main().postDelayed(() -> {
            if (VoiceConnectionService.currentConnections.isEmpty()) {
                release();
            }
        }, NO_CALL_TIMEOUT_MS);
    }

    private static void cancelPending() {
        if (pending != null && pendingApplication != null) {
            pendingApplication.unregisterActivityLifecycleCallbacks(pending);
        }
        pending = null;
        pendingApplication = null;
    }

    private static void apply(Activity activity, boolean on) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                activity.setShowWhenLocked(on);
                activity.setTurnScreenOn(on);
            } else {
                int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
                if (on) {
                    activity.getWindow().addFlags(flags);
                } else {
                    activity.getWindow().clearFlags(flags);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "[LockScreen] can't change lock-screen visibility: " + e);
        }
    }

    private abstract static class SimpleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override public void onActivityStarted(@NonNull Activity activity) {}
        @Override public void onActivityResumed(@NonNull Activity activity) {}
        @Override public void onActivityPaused(@NonNull Activity activity) {}
        @Override public void onActivityStopped(@NonNull Activity activity) {}
        @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
        @Override public void onActivityDestroyed(@NonNull Activity activity) {}
    }
}
