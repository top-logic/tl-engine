/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.routing;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;

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
	 * Displays what the matched route names, materializing it where it is created lazily.
	 *
	 * <p>
	 * Called by the {@link RouteManager} while it resolves a URL - a link followed into the
	 * application, an address the browser moved to over the back button, or one entered by hand.
	 * </p>
	 *
	 * <p>
	 * The activation may be refused with a
	 * {@link com.top_logic.layout.react.dirty.ChannelVetoException} while what the participant
	 * displays holds unsaved changes: a URL takes the user off a page no more silently than a click
	 * does. The state the URL describes is then not reached, and the caller ends the adoption with
	 * {@link RouteManager#cancelAdoption()}, which leaves the display as the refusal keeps it. What
	 * is to become of the changes is put to the user, and the URL reaches this participant again
	 * once they answered.
	 * </p>
	 *
	 * @param match
	 *        The route to display, with the parameters the URL carried for it.
	 */
	void activateRoute(RouteMatch match);

    /**
     * Currently active route segment, or {@code null} if no route-forming
     * item is active.
     */
    RouteSegment activeRouteSegment();

	/**
	 * The label of what the {@link #activeRouteSegment() active route segment} names, or
	 * {@code null} if this participant names nothing a user would recognize the page by.
	 *
	 * <p>
	 * The label names the page in the browser tab and in the history: the {@link RouteManager}
	 * titles the page after the label of the innermost participant of the display that reports one
	 * - the item a sidebar selects, the tab a tab bar shows, refined by whatever the display names
	 * below them. A participant without a label leaves the title to the participants around it.
	 * </p>
	 *
	 * <p>
	 * A change of the label is reported like a change of the route, to the
	 * {@link #addRouteChangeListener(RouteChangeListener) route change listeners}, even where the
	 * segment stays the same: the page keeps its address, but not its title.
	 * </p>
	 *
	 * <p>
	 * The default reports no label, which is what a participant does whose route refines the page
	 * rather than naming one - a query parameter holding a filter, for instance.
	 * </p>
	 *
	 * @see RouteManager#currentTitle()
	 */
	default ResKey activeRouteLabel() {
		return null;
	}

	/**
	 * Takes up the query parameters of the URL being adopted.
	 *
	 * <p>
	 * Offered to every participant of the display, with the whole query of the URL: a participant
	 * reads the parameters it declares and ignores the rest, because a query parameter names its
	 * meaning itself instead of being placed by its position, and one URL carries the parameters of
	 * everything the display shows. A parameter the URL does not carry says nothing about the value
	 * behind it, which therefore stays what the view establishes.
	 * </p>
	 *
	 * <p>
	 * The default takes up nothing, which is what a participant does whose state is a path segment.
	 * </p>
	 *
	 * @param query
	 *        The query parameters of the URL, decoded, never empty.
	 */
	default void activateQuery(Map<String, String> query) {
		// Nothing to take up: the state of this participant is named by the path.
	}

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
	 * Whether what this participant displays below itself is addressed by the URL.
	 *
	 * <p>
	 * A participant showing an item that is explicitly not routed - a navigation item or tab with
	 * {@code route="none"} - displays a part of the application that has no URL. The participants
	 * inside it keep working, but contribute nothing to the URL and are not offered its segments:
	 * a segment they contributed would stand where the segment of the unrouted item belongs, and
	 * the URL could not be resolved back to the display it was composed from. The URL then names
	 * only what lies above, and a reload of the window keeps the display, because the URL names no
	 * route that would have to be reset.
	 * </p>
	 *
	 * <p>
	 * The default is {@code true}: the content of a participant is part of the URL.
	 * </p>
	 */
	default boolean routesContent() {
		return true;
	}

	/**
	 * Whether the participants inside what this participant displays refine the title of the page.
	 *
	 * <p>
	 * The page is titled after the innermost participant of the display that reports a
	 * {@link #activeRouteLabel() label}. Where this participant names the page completely - a frame
	 * showing an object, whose tabs only divide what is shown of it - the label of a tab inside it
	 * would rename the page after a part of it: the browser tab would read "Details" instead of the
	 * name of the object. A participant answering {@code false} keeps the labels of the participants
	 * inside its content out of the title, which then is its own label or, where it reports none,
	 * whatever the participants around it name.
	 * </p>
	 *
	 * <p>
	 * The URL is not affected: the participants inside still contribute their segments and are
	 * offered the segments of a URL that is opened.
	 * </p>
	 *
	 * <p>
	 * The default is {@code true}: what is displayed inside a participant names the page more
	 * precisely than the participant itself.
	 * </p>
	 *
	 * @see RouteManager#setTitlingParticipants(java.util.function.Supplier)
	 */
	default boolean titlesContent() {
		return true;
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
