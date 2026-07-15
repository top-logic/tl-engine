/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.routing;

import java.util.List;

/**
 * Controls that contribute URL segments implement this interface.
 *
 * <p>Registration/deregistration with the {@link RouteManager} happens
 * automatically via the {@link com.top_logic.layout.react.control.ReactControl}
 * attach/detach lifecycle.</p>
 */
public interface RoutingParticipant {

    /**
     * Route patterns for this participant's direct children.
     * Only returns patterns for children that have a {@code route} attribute declared.
     */
    List<RoutePattern> declaredRoutes();

    /**
     * Activate the matching child route. May trigger lazy materialization.
     * Called by {@link RouteManager} during deep-link resolution or back-navigation.
     */
    void activateRoute(RouteMatch match);

    /**
     * Currently active route segment, or {@code null} if no route-forming
     * item is active.
     */
    RouteSegment activeRouteSegment();

    /**
     * Registers a {@link RouteChangeListener} to be notified when this participant's active route
     * changes.
     */
    void addRouteChangeListener(RouteChangeListener listener);

    /**
     * Unregisters a {@link RouteChangeListener} previously registered via
     * {@link #addRouteChangeListener(RouteChangeListener)}.
     */
    void removeRouteChangeListener(RouteChangeListener listener);
}
