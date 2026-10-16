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

import android.telecom.CallAudioState;

import java.util.ArrayList;
import java.util.List;

/** Telecom audio route helpers. Uses only CallAudioState's constants, so it runs off-device. */
final class AudioRoutes {
    private AudioRoutes() {}

    /** Telecom's routes in its own bit order. ROUTE_STREAMING (16) is API 34+. */
    private static final int[] ROUTES = {
        CallAudioState.ROUTE_EARPIECE,
        CallAudioState.ROUTE_BLUETOOTH,
        CallAudioState.ROUTE_WIRED_HEADSET,
        CallAudioState.ROUTE_SPEAKER,
        CallAudioState.ROUTE_STREAMING,
    };

    /** Each route in a route mask, in Telecom's order. */
    static List<Integer> routes(int mask) {
        List<Integer> routes = new ArrayList<>();
        for (int route : ROUTES) {
            if ((mask & route) == route) {
                routes.add(route);
            }
        }
        return routes;
    }

    /** A single route's name, as CallAudioState.audioRouteToString (and didChangeAudioRoute's output) writes it. */
    static String routeName(int route) {
        switch (route) {
            case CallAudioState.ROUTE_EARPIECE: return "EARPIECE";
            case CallAudioState.ROUTE_BLUETOOTH: return "BLUETOOTH";
            case CallAudioState.ROUTE_WIRED_HEADSET: return "WIRED_HEADSET";
            case CallAudioState.ROUTE_SPEAKER: return "SPEAKER";
            case CallAudioState.ROUTE_STREAMING: return "STREAMING";
            default: return "UNKNOWN";
        }
    }

    /** getAudioRoutes()'s type (and name) for a route; null for a route it does not list (streaming). */
    static String callKeepType(int route) {
        switch (route) {
            case CallAudioState.ROUTE_EARPIECE: return "Phone";
            case CallAudioState.ROUTE_BLUETOOTH: return "Bluetooth";
            case CallAudioState.ROUTE_WIRED_HEADSET: return "Headset";
            case CallAudioState.ROUTE_SPEAKER: return "Speaker";
            default: return null;
        }
    }

    /** The names of the routes in a mask (a CallAudioState's supported route mask). */
    static List<String> routeNames(int mask) {
        List<String> names = new ArrayList<>();
        for (int route : routes(mask)) {
            names.add(routeName(route));
        }
        return names;
    }

    /** The route for toggleAudioRouteSpeaker: off means a wired headset if one is in, else the earpiece. */
    static int speakerRoute(boolean on) {
        return on ? CallAudioState.ROUTE_SPEAKER : CallAudioState.ROUTE_WIRED_OR_EARPIECE;
    }
}
