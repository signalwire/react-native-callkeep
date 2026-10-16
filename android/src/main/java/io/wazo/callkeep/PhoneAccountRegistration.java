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

import java.util.function.Consumer;

/**
 * Registers callkeep's phone account with Telecom. Kept free of Telecom
 * classes so it can be tested off-device.
 */
final class PhoneAccountRegistration {
    private PhoneAccountRegistration() {}

    interface Registrar<A> {
        void register(A account);

        void unregister();
    }

    /**
     * Registers the account; true if Telecom took it.
     *
     * Telecom refuses to turn a registered self-managed account into another
     * kind (IllegalArgumentException "cannot change a self-managed phone
     * account ... to other kinds of phone account"), and the account outlives
     * the app process, so an app that turned selfManaged off could never
     * register again (#722): unregister it and register the new one. Devices
     * without Telecom throw UnsupportedOperationException or SecurityException
     * (#815, #761): leave the account unregistered rather than crash setup().
     * Failures go to {@code warn}.
     */
    static <A> boolean register(Registrar<A> registrar, A account, Consumer<String> warn) {
        try {
            registrar.register(account);
            return true;
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message == null || !message.contains("self-managed")) {
                warn.accept("registerPhoneAccount failed: " + e);
                return false;
            }
            warn.accept("the phone account changes kind; registering it again: " + e);
        } catch (UnsupportedOperationException | SecurityException e) {
            warn.accept("registerPhoneAccount failed: " + e);
            return false;
        }
        try {
            registrar.unregister();
            registrar.register(account);
            return true;
        } catch (UnsupportedOperationException | SecurityException | IllegalArgumentException e) {
            warn.accept("registerPhoneAccount failed again: " + e);
            return false;
        }
    }
}
