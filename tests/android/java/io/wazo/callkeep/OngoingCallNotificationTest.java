package io.wazo.callkeep;

/** The ongoing-call notification opens the app with or without a live activity (see OngoingCallNotification). */
public class OngoingCallNotificationTest {
    public static void main(String[] args) {
        check("com.app.MainActivity".equals(OngoingCallNotification.contentActivity("com.app.MainActivity", "com.app.Launcher")),
                "the current activity wins");
        check("com.app.Launcher".equals(OngoingCallNotification.contentActivity(null, "com.app.Launcher")),
                "no activity (closed with Back during a call): the launcher activity");
        check("com.app.Launcher".equals(OngoingCallNotification.contentActivity("", "com.app.Launcher")), "empty name");
        check(OngoingCallNotification.contentActivity(null, null) == null, "nothing to open");
        System.out.println("OngoingCallNotificationTest: ok");
    }

    private static void check(boolean ok, String what) {
        if (!ok) {
            throw new AssertionError(what);
        }
    }
}
