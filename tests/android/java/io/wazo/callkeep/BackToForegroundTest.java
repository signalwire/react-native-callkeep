package io.wazo.callkeep;

import android.content.Intent;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * backToForeground over the lock screen (upstream #779, #667; the fix in PR #684).
 *
 * It added WindowManager.LayoutParams flags to the Intent's flags. They are
 * window flags, not intent flags, and their bits mean other things on an
 * Intent: FLAG_SHOW_WHEN_LOCKED is FLAG_ACTIVITY_NEW_DOCUMENT,
 * FLAG_DISMISS_KEYGUARD is FLAG_ACTIVITY_BROUGHT_TO_FRONT and
 * FLAG_TURN_SCREEN_ON is FLAG_ACTIVITY_RESET_TASK_IF_NEEDED. So the app was
 * started as a new document task and never shown over the lock screen.
 */
public class BackToForegroundTest {
    public static void main(String[] args) throws Exception {
        // The aliasing the old code fell into.
        check(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED == Intent.FLAG_ACTIVITY_NEW_DOCUMENT, "SHOW_WHEN_LOCKED alias");
        check(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD == Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT, "DISMISS_KEYGUARD alias");
        check(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON == Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED, "TURN_SCREEN_ON alias");

        check(LockScreen.launchFlags(true) == Intent.FLAG_ACTIVITY_REORDER_TO_FRONT, "running activity: reorder to front");
        check(LockScreen.launchFlags(false) == Intent.FLAG_ACTIVITY_NEW_TASK, "no activity: a new task, nothing else");

        // The old expression, folded by javac into one int constant, is gone from the module.
        int old = Intent.FLAG_ACTIVITY_NEW_TASK + WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD + WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        byte[] constant = { 3, (byte) (old >>> 24), (byte) (old >>> 16), (byte) (old >>> 8), (byte) old };
        for (String name : new String[] { "RNCallKeepModule", "LockScreen" }) {
            check(indexOf(classBytes(name), constant) < 0, name + " still adds window flags to an Intent");
        }
        System.out.println("BackToForegroundTest: ok");
    }

    private static void check(boolean ok, String what) {
        if (!ok) {
            throw new AssertionError(what);
        }
    }

    private static byte[] classBytes(String name) throws Exception {
        try (InputStream in = BackToForegroundTest.class.getResourceAsStream("/io/wazo/callkeep/" + name + ".class")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            for (int n; (n = in.read(buffer)) > 0; ) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        }
    }

    private static int indexOf(byte[] haystack, byte[] needle) {
        outer:
        for (int i = 0; i + needle.length <= haystack.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
