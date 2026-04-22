/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.routing;

import java.util.ArrayList;
import java.util.List;

/**
 * Central coordination service for URL routing in the React view system.
 *
 * <p>
 * Manages the set of registered {@link RoutingParticipant}s, composes the current URL from their
 * active segments, and resolves pending (deep-link) URLs by matching segments against participant
 * route declarations.
 * </p>
 *
 * <p>
 * Participants are registered in top-down order (e.g., sidebar first, then tabs). The composed URL
 * is built by concatenating the active segments of all registered participants.
 * </p>
 *
 * @see RoutingParticipant
 * @see RoutePattern
 */
public final class RouteManager {

	private final List<RoutingParticipant> _participants = new ArrayList<>();

	private final RouteChangeListener _internalListener = this::onParticipantRouteChange;

	private String _pendingUrl;

	private RouteUrlChangeHandler _urlChangeHandler;

	/**
	 * Creates a new {@link RouteManager}.
	 */
	public RouteManager() {
		// Default constructor.
	}

	/**
	 * Registers a {@link RoutingParticipant} and subscribes to its route changes.
	 *
	 * <p>
	 * If there is a pending URL with unresolved segments, the manager attempts to match the next
	 * unresolved segment against the new participant's declared routes. If matched, the participant
	 * is activated, which may trigger lazy materialization of child views.
	 * </p>
	 *
	 * @param participant
	 *        The participant to register.
	 */
	public void register(RoutingParticipant participant) {
		_participants.add(participant);
		participant.addRouteChangeListener(_internalListener);

		tryResolvePending(participant);
	}

	/**
	 * Unregisters a {@link RoutingParticipant} and unsubscribes from its route changes.
	 *
	 * @param participant
	 *        The participant to unregister.
	 */
	public void unregister(RoutingParticipant participant) {
		participant.removeRouteChangeListener(_internalListener);
		_participants.remove(participant);
	}

	/**
	 * Sets a pending URL for deferred resolution.
	 *
	 * <p>
	 * The pending URL is resolved incrementally as participants register. Each registration attempt
	 * consumes the next unresolved segment from this URL.
	 * </p>
	 *
	 * @param url
	 *        The URL to resolve (without leading slash).
	 */
	public void setPendingUrl(String url) {
		_pendingUrl = url;
	}

	/**
	 * Navigates to the given URL by setting it as pending and attempting resolution on all currently
	 * registered participants.
	 *
	 * @param url
	 *        The target URL (without leading slash).
	 */
	public void navigateToRoute(String url) {
		_pendingUrl = url;

		for (RoutingParticipant participant : new ArrayList<>(_participants)) {
			if (_pendingUrl == null || _pendingUrl.isEmpty()) {
				break;
			}
			tryResolvePending(participant);
		}
	}

	/**
	 * Composes the current URL from the active segments of all registered participants.
	 *
	 * @return The composed URL (without leading slash), or empty string if no segments are active.
	 */
	public String currentUrl() {
		StringBuilder sb = new StringBuilder();
		for (RoutingParticipant participant : _participants) {
			RouteSegment segment = participant.activeRouteSegment();
			if (segment != null && !segment.path().isEmpty()) {
				if (sb.length() > 0) {
					sb.append('/');
				}
				sb.append(segment.path());
			}
		}
		return sb.toString();
	}

	/**
	 * Sets the handler that is notified when the composed URL changes.
	 *
	 * @param handler
	 *        The handler to receive URL change notifications, or {@code null} to remove the
	 *        handler.
	 */
	public void setUrlChangeHandler(RouteUrlChangeHandler handler) {
		_urlChangeHandler = handler;
	}

	private void tryResolvePending(RoutingParticipant participant) {
		if (_pendingUrl == null || _pendingUrl.isEmpty()) {
			return;
		}

		String segmentToResolve = computeUnresolvedSegment();
		if (segmentToResolve.isEmpty()) {
			return;
		}

		for (RoutePattern pattern : participant.declaredRoutes()) {
			RouteMatch match = pattern.match(segmentToResolve);
			if (match != null) {
				_pendingUrl = match.remainingPath();
				if (_pendingUrl.isEmpty()) {
					_pendingUrl = null;
				}
				participant.activateRoute(match);
				return;
			}
		}
	}

	/**
	 * Computes the portion of the pending URL that has not yet been consumed by registered
	 * participants.
	 */
	private String computeUnresolvedSegment() {
		if (_pendingUrl == null) {
			return "";
		}
		return _pendingUrl;
	}

	private void onParticipantRouteChange(RoutingParticipant participant, RouteSegment newSegment) {
		// Clear segments from participants below the changed one.
		int index = _participants.indexOf(participant);
		if (index >= 0) {
			// Participants registered after this one may have stale segments, but we don't
			// forcefully clear them - they will update when new child participants register.
		}

		notifyUrlChange(false);
	}

	private void notifyUrlChange(boolean replace) {
		if (_urlChangeHandler != null) {
			_urlChangeHandler.onUrlChange(currentUrl(), replace);
		}
	}
}
