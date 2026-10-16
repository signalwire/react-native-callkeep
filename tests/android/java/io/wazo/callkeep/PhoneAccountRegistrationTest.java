package io.wazo.callkeep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Upstream #722: Telecom refuses to turn a registered self-managed phone
 * account into a managed one (IllegalArgumentException "cannot change a
 * self-managed phone account ... to other kinds of phone account"), so an app
 * that switched selfManaged off could never register again.
 */
public class PhoneAccountRegistrationTest {
    private static final String SELF_MANAGED_CHANGE =
            "Error, cannot change a self-managed phone account ComponentInfo{app/io.wazo.callkeep.VoiceConnectionService}, app, UserHandle{0} to other kinds of phone account";

    /** Throws the queued exceptions in turn, then registers. */
    private static class FakeTelecom implements PhoneAccountRegistration.Registrar<String> {
        final List<String> calls = new ArrayList<>();
        final List<RuntimeException> failures;

        FakeTelecom(RuntimeException... failures) {
            this.failures = new ArrayList<>(Arrays.asList(failures));
        }

        @Override
        public void register(String account) {
            calls.add("register " + account);
            if (!failures.isEmpty()) {
                throw failures.remove(0);
            }
        }

        @Override
        public void unregister() {
            calls.add("unregister");
        }
    }

    private static final List<String> warnings = new ArrayList<>();
    private static final java.util.function.Consumer<String> WARN = warnings::add;

    private static void eq(Object expected, Object actual, String what) {
        if (!expected.equals(actual)) {
            throw new AssertionError(what + ": expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) {
        FakeTelecom ok = new FakeTelecom();
        eq(true, PhoneAccountRegistration.register(ok, "managed", WARN), "plain registration");
        eq(Arrays.asList("register managed"), ok.calls, "plain registration calls");

        // #722: the account is unregistered and registered again in the new mode.
        FakeTelecom switched = new FakeTelecom(new IllegalArgumentException(SELF_MANAGED_CHANGE));
        eq(true, PhoneAccountRegistration.register(switched, "managed", WARN), "self-managed to managed");
        eq(Arrays.asList("register managed", "unregister", "register managed"), switched.calls, "re-registration");

        // Any other refusal leaves the existing account alone and reports it.
        FakeTelecom other = new FakeTelecom(new IllegalArgumentException("some other reason"));
        eq(false, PhoneAccountRegistration.register(other, "managed", WARN), "other IllegalArgumentException");
        eq(Arrays.asList("register managed"), other.calls, "no unregister for other reasons");

        FakeTelecom stillRefused = new FakeTelecom(new IllegalArgumentException(SELF_MANAGED_CHANGE),
                new IllegalArgumentException(SELF_MANAGED_CHANGE));
        eq(false, PhoneAccountRegistration.register(stillRefused, "managed", WARN), "refused twice");

        // #815, #761: devices without Telecom.
        eq(false, PhoneAccountRegistration.register(new FakeTelecom(
                new UnsupportedOperationException("System does not support feature android.software.telecom")), "x", WARN),
                "no telecom feature");
        eq(false, PhoneAccountRegistration.register(new FakeTelecom(new SecurityException("not voice capable")), "x", WARN),
                "security exception");
        eq(6, warnings.size(), "every failure is reported");

        // #761: in self-managed mode hasPhoneAccount() was always true, even where
        // Telecom refused the account; it is false once a registration was refused.
        eq(true, PhoneAccountRegistration.selfManagedAccountAvailable(null), "not registered yet");
        eq(true, PhoneAccountRegistration.selfManagedAccountAvailable(true), "registered");
        eq(false, PhoneAccountRegistration.selfManagedAccountAvailable(false), "refused");
        System.out.println("PhoneAccountRegistrationTest: ok");
    }
}
