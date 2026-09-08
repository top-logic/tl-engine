/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
 * Registration follows the display: a participant registers when its control is attached and
 * unregisters when it is detached. The composed URL is built from the participants the display
 * currently contains, in the order in which the display contains them - see
 * {@link #setDisplayedParticipants(Supplier)}. A deep link, in contrast, is resolved in registration
 * order, because its segments are consumed by the participants as they appear.
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

	private boolean _suppressNotifications;

	private String _lastNotifiedUrl;

	private Supplier<List<RoutingParticipant>> _displayedParticipants;

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

		if (_pendingUrl != null && !_pendingUrl.isEmpty()) {
			tryResolvePending(participant);
			notifyAdoptionComplete();
		} else {
			// No pending deep-link: check if the new participant already has an
			// active route segment (e.g., default sidebar item). If so, send the
			// initial URL as a replaceState (not pushState) so the address bar
			// reflects the current state without creating a history entry.
			RouteSegment segment = participant.activeRouteSegment();
			if (segment != null && !segment.path().isEmpty()) {
				notifyUrlChange(true);
			}
		}
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

		// The participant may have contributed a segment that is gone with it: a tab bar shown for
		// one case of a <switch>, for instance, leaves the display when another case is selected.
		// Its segment must not stay in the address bar, where it would point at something not
		// displayed - and would be silently dropped on the next reload.
		//
		// Replacing rather than pushing: what disappeared from the display is not a navigation the
		// user should have to undo with the back button.
		notifyUrlChange(true);
	}

	/**
	 * Installs the source of the participants the display currently contains, in display order.
	 *
	 * <p>
	 * The URL is composed from these, so a participant that has left the display contributes
	 * nothing, and the segment order follows the display hierarchy rather than the sequence in which
	 * the participants happened to register - which, for a lazily rendered tab or a re-entered view,
	 * is neither the display order nor free of what is no longer shown.
	 * </p>
	 *
	 * @param source
	 *        Supplier of the displayed participants, or {@code null} to compose from the registered
	 *        participants instead.
	 */
	public void setDisplayedParticipants(Supplier<List<RoutingParticipant>> source) {
		_displayedParticipants = source;
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

		// The client displays this URL: it is the one it requested. Recording it keeps the display
		// it materializes from being reported back as a navigation.
		_lastNotifiedUrl = url;
	}

	/**
	 * Resolves the pending URL against the participants that are registered now.
	 *
	 * <p>
	 * Registration resolves the pending URL as participants appear, which is what a display being
	 * built up for the first time does. A display that already exists - the control tree a reloaded
	 * page is rendered into - has its participants registered before the URL to adopt is known, so
	 * nothing appears to consume it. This pass hands the pending segments to those participants,
	 * whose activation in turn materializes the display below them.
	 * </p>
	 */
	public void resolvePending() {
		if (_pendingUrl == null || _pendingUrl.isEmpty()) {
			return;
		}

		_suppressNotifications = true;
		try {
			for (RoutingParticipant participant : new ArrayList<>(_participants)) {
				if (_pendingUrl == null || _pendingUrl.isEmpty()) {
					break;
				}
				tryResolvePending(participant);
			}
		} finally {
			_suppressNotifications = false;
		}

		notifyAdoptionComplete();
	}

	/**
	 * Navigates to the given URL by setting it as pending and attempting resolution on all currently
	 * registered participants.
	 *
	 * @param url
	 *        The target URL (without leading slash).
	 */
	public void navigateToRoute(String url) {
		// The browser already displays this URL (it changed by popstate); adopting it is not a
		// navigation of its own. Recording it as the URL the client shows keeps every change the
		// adoption causes - a participant selecting an item, a lazily rendered control registering
		// afterwards - from pushing a history entry that would cancel the back navigation.
		setPendingUrl(url);
		resolvePending();
	}

	/**
	 * Concludes the adoption of the URL the client requested.
	 *
	 * <p>
	 * Called once the display is complete, so that a segment still unresolved is one the display
	 * cannot reproduce - a route that no participant declares, or one naming something that is not
	 * shown. Such a segment is dropped and the address bar is corrected to what the display
	 * composes, because a URL kept beyond the state it describes would be silently lost on the next
	 * reload, or appended to by the next navigation.
	 * </p>
	 */
	public void finishAdoption() {
		_pendingUrl = null;
		notifyUrlChange(true);
	}

	/**
	 * Composes the current URL from the active segments of the participants the display contains.
	 *
	 * @return The composed URL (without leading slash), or empty string if no segments are active.
	 *
	 * @see #setDisplayedParticipants(Supplier)
	 */
	public String currentUrl() {
		StringBuilder sb = new StringBuilder();
		for (RoutingParticipant participant : composingParticipants()) {
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

	/**
	 * Reports the composed URL once every segment of the URL being adopted has been consumed.
	 *
	 * <p>
	 * Reporting while segments are still unresolved would send a URL that describes a display only
	 * half materialized, and the client would take it for the state it asked for. A URL the display
	 * cannot reproduce at all - an unresolvable segment, or one naming something that is not shown -
	 * is corrected here, as a replacement rather than a history entry: adopting a URL is not a
	 * navigation the user should have to undo.
	 * </p>
	 */
	private void notifyAdoptionComplete() {
		if (_pendingUrl == null || _pendingUrl.isEmpty()) {
			notifyUrlChange(true);
		}
	}

	private List<RoutingParticipant> composingParticipants() {
		Supplier<List<RoutingParticipant>> source = _displayedParticipants;
		return source == null ? _participants : source.get();
	}

	private void notifyUrlChange(boolean replace) {
		if (_suppressNotifications) {
			return;
		}
		String url = currentUrl();
		if (url.equals(_lastNotifiedUrl)) {
			// The client already shows this URL - notifying again would either pollute the history
			// or repeat a replacement that changes nothing.
			return;
		}
		_lastNotifiedUrl = url;
		if (_urlChangeHandler != null) {
			_urlChangeHandler.onUrlChange(url, replace);
		}
	}
}
