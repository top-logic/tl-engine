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
	 * Whether the URL being adopted can name one route of this participant after another.
	 *
	 * <p>
	 * A participant that displays one thing at a time takes up one route: the item a sidebar
	 * selects, the value a channel holds, and whatever the URL names after it is named for someone
	 * else. A participant that displays a chain of them - the frames of a drill-down path, one route
	 * per frame - takes up as many routes as the URL names in sequence, and is therefore offered the
	 * rest of the URL again after each one it took up.
	 * </p>
	 */
	default boolean acceptsRouteSequence() {
		return false;
	}

	/**
	 * Returns to the state that has no route, because the URL being adopted names none for this
	 * participant.
	 *
	 * <p>
	 * Called by {@link RouteManager#finishAdoption()} on a participant which the display contains,
	 * whose {@link #activeRouteSegment()} is not empty, and which the adopted URL neither activated
	 * nor brought into the display by one of its activations: it shows a state the URL does not
	 * describe, and a back navigation to a shorter URL would otherwise leave that state standing. A
	 * participant an activation of the URL brought into the display is never reset - its segment is
	 * what the URL left unspecified, a default selection for instance. Merely registering while a URL
	 * is adopted is not that: a page loaded into the control tree its window still holds attaches
	 * that tree, and what it re-registers displays what the previous page left.
	 * </p>
	 *
	 * <p>
	 * The default keeps the state, which is what a participant does that always displays something: a
	 * sidebar shows one of its items whatever the URL says, and a value the URL does not name is the
	 * one the view establishes itself. Implement this where the participant has a state without a
	 * route to return to - an empty drill-down path, for instance.
	 * </p>
	 */
	default void resetRoute() {
		// The state stands: what the URL does not name is what the view establishes itself.
	}

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
