package io.wazo.callkeep;

/**
 * Where a tap on the ongoing-call (foreground service) notification goes.
 *
 * The notification is rebuilt each time the service is started for a new
 * call. It used to open the current React activity's class and nothing when
 * there was none: with endCallsOnHostDestroy false the call outlives the
 * activity (Back on the root screen), and a second call starting then left a
 * notification that did nothing when tapped. The app's launcher activity is
 * the fallback.
 */
final class OngoingCallNotification {
    private OngoingCallNotification() {}

    /** The activity to open: the current one, else the launcher's, else none. */
    static String contentActivity(String currentActivityClass, String launcherActivityClass) {
        if (currentActivityClass != null && !currentActivityClass.isEmpty()) {
            return currentActivityClass;
        }
        if (launcherActivityClass != null && !launcherActivityClass.isEmpty()) {
            return launcherActivityClass;
        }
        return null;
    }
}
