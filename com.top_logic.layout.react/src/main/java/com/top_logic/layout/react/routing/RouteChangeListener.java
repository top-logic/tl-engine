/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.routing;

/**
 * Listener for route-forming navigation events within a {@link RoutingParticipant}.
 */
@FunctionalInterface
public interface RouteChangeListener {

    /**
     * Called when a route-forming navigation happened.
     */
    void onRouteChange(RoutingParticipant participant, RouteSegment newSegment);
}
