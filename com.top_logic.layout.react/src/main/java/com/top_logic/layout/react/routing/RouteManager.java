/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.routing;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * A segment contributes a path, query parameters, or both. The path is positional - it is consumed
 * by the participants in the order the display contains them - while a query parameter names its
 * meaning itself and is therefore offered to every participant, which is what
 * {@link RoutingParticipant#activateQuery(Map)} does. A change that leaves the path as it is and
 * alters only the query is reported as a replacement: refining what a view shows is not a page the
 * user navigated to.
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

	/** Separates the query string from the path of a URL. */
	private static final char QUERY_START = '?';

	/** Separates the parameters of a query string from each other. */
	private static final char PARAM_SEPARATOR = '&';

	/** Separates the name of a query parameter from its value. */
	private static final char VALUE_SEPARATOR = '=';

	private final List<RoutingParticipant> _participants = new ArrayList<>();

	private final RouteChangeListener _internalListener = this::onParticipantRouteChange;

	private String _pendingUrl;

	private Map<String, String> _pendingQuery = Map.of();

	private RouteUrlChangeHandler _urlChangeHandler;

	private boolean _suppressNotifications;

	private String _lastNotifiedUrl;

	private Supplier<List<RoutingParticipant>> _displayedParticipants;

	private boolean _adopting;

	private long _adoptionId;

	private int _activationDepth;

	private final Set<RoutingParticipant> _activatedWhileAdopting =
		Collections.newSetFromMap(new IdentityHashMap<>());

	private final Set<RoutingParticipant> _registeredWhileAdopting =
		Collections.newSetFromMap(new IdentityHashMap<>());

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

		if (_activationDepth > 0) {
			// Brought into the display by an activation of the URL being adopted: it is part of the
			// display the URL asked for, even where the URL says nothing about it, and must therefore
			// not be reset in finishAdoption(). A participant that merely registers while a URL is
			// adopted is not: a page loaded into the control tree its window still holds attaches that
			// tree, and everything it re-registers displays what the previous page left, not what this
			// URL asked for.
			_registeredWhileAdopting.add(participant);
		}

		// The query of the URL being adopted reaches a participant appearing while it is adopted, too:
		// what the display materializes into is the state the URL describes as a whole, and a query
		// parameter belongs to whichever participant declares it, wherever in the display that is.
		offerPendingQuery(participant);

		if (_pendingUrl != null && !_pendingUrl.isEmpty()) {
			// Nothing is reported here: the display is still materializing into the URL, and what it
			// composes in the middle of that describes neither the state the client asked for nor one
			// it should be told about. The URL the display arrives at is reported once, by
			// finishAdoption().
			tryResolvePending(participant);
		} else if (!_adopting) {
			// A participant with a route of its own where no URL is being adopted - the item a sidebar
			// selects by default. The address bar is completed with it as a replacement rather than a
			// history entry, because nobody navigated there.
			//
			// While a URL is adopted, nothing is reported: the display is still being built, and what
			// it composes halfway through describes neither the state the client asked for nor one it
			// should be told about. The URL the display arrives at is reported by finishAdoption().
			if (contributes(participant.activeRouteSegment())) {
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

		if (_adopting) {
			// A display being built up for the URL the client shows exchanges what it displays -
			// a page loaded into an existing control tree re-attaches it, an activated route
			// replaces the content beside it - and the URL such a display composes halfway through
			// is nobody's address: while the tree is not walkable yet, it is not even the one the
			// client shows. The URL the display arrives at is reported by finishAdoption().
			return;
		}

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
	 * Begins adopting the URL the client displays.
	 *
	 * <p>
	 * Records the URL as the one the client shows, so that the display materializing into it is not
	 * reported back as a navigation, and queues its segments for resolution: they are consumed
	 * incrementally as participants register, and by {@link #resolvePending()} for the participants
	 * that are registered already. Pass the empty URL for a client that displays no route at all -
	 * the address bar is then completed from the display, in {@link #finishAdoption()}.
	 * </p>
	 *
	 * <p>
	 * Every adoption ends, either with {@link #finishAdoption()} once the display is complete, or
	 * with {@link #cancelAdoption()} where the URL was refused. Until it ends, the display change it
	 * causes is reported as a replacement rather than a history entry, and the URL the display
	 * composes is reported once, at the end - an adoption left open would keep the next navigation
	 * from being one.
	 * </p>
	 *
	 * <p>
	 * A query string is split off the URL and belongs to the adoption as a whole rather than to one
	 * participant: it is offered to every participant that is registered when the pending URL is
	 * resolved and to every one that registers while the adoption runs.
	 * </p>
	 *
	 * @param url
	 *        The URL the client displays (without leading slash, with its query string), empty for
	 *        none.
	 */
	public void adoptUrl(String url) {
		int queryStart = url == null ? -1 : url.indexOf(QUERY_START);
		_pendingUrl = queryStart < 0 ? url : url.substring(0, queryStart);
		_pendingQuery = queryStart < 0 ? Map.of() : parseQuery(url.substring(queryStart + 1));
		_lastNotifiedUrl = url;
		_adopting = true;
		_adoptionId++;
		_activatedWhileAdopting.clear();
		_registeredWhileAdopting.clear();
	}

	/**
	 * Identifies the adoption of a URL that is in progress.
	 *
	 * <p>
	 * A participant that restores a display of several levels from one URL - the frames of a
	 * drill-down path, for instance, one per level - tells the levels of one adoption from those of
	 * the next by this value: the levels of a URL adopted now replace the ones an earlier URL
	 * established rather than extending them. The value changes with every URL taken up and is
	 * therefore stable exactly for the duration of one adoption.
	 * </p>
	 */
	public long adoptionId() {
		return _adoptionId;
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
	 *
	 * <p>
	 * A registered participant the display does not contain at the moment it is reached - one below a
	 * frame a tile stack keeps covered - is passed over: the URL describes what the user sees, so a
	 * route it names belongs to a participant that is shown. Checked when the participant is reached
	 * rather than once for the pass, because an activation changes the display, and a participant
	 * covered before it may be the one shown afterwards.
	 * </p>
	 */
	public void resolvePending() {
		for (RoutingParticipant participant : new ArrayList<>(_participants)) {
			offerPendingQuery(participant);
		}

		if (_pendingUrl == null || _pendingUrl.isEmpty()) {
			return;
		}

		boolean before = _suppressNotifications;
		_suppressNotifications = true;
		try {
			for (RoutingParticipant participant : new ArrayList<>(_participants)) {
				if (_pendingUrl == null || _pendingUrl.isEmpty()) {
					break;
				}
				if (!_participants.contains(participant) || !isDisplayed(participant)) {
					// Gone from the display over an earlier activation, or not shown by it.
					continue;
				}
				tryResolvePending(participant);
			}
		} finally {
			_suppressNotifications = before;
		}
	}

	/**
	 * Hands the query parameters of the URL being adopted to the given participant.
	 *
	 * <p>
	 * Nothing is reported over it: the value a parameter delivers is part of the state the client
	 * already shows, and the URL the display arrives at is reported once, at the end of the
	 * adoption.
	 * </p>
	 */
	private void offerPendingQuery(RoutingParticipant participant) {
		if (_pendingQuery.isEmpty()) {
			return;
		}
		boolean before = _suppressNotifications;
		_suppressNotifications = true;
		try {
			participant.activateQuery(_pendingQuery);
		} finally {
			_suppressNotifications = before;
		}
	}

	/**
	 * Navigates to the given URL by setting it as pending and attempting resolution on all currently
	 * registered participants.
	 *
	 * <p>
	 * Begins an adoption, which the caller ends with {@link #finishAdoption()} or, where the display
	 * refused the URL, with {@link #cancelAdoption()}.
	 * </p>
	 *
	 * @param url
	 *        The target URL (without leading slash).
	 */
	public void navigateToRoute(String url) {
		// The browser already displays this URL (it changed by popstate); adopting it is not a
		// navigation of its own. Recording it as the URL the client shows keeps every change the
		// adoption causes - a participant selecting an item, a lazily rendered control registering
		// afterwards - from pushing a history entry that would cancel the back navigation.
		adoptUrl(url);
		resolvePending();
	}

	/**
	 * Applies a change of the display that navigates.
	 *
	 * <p>
	 * The URL the display composes afterwards becomes a history entry the user can come back from.
	 * Participants appearing and disappearing while the change is applied report nothing of their
	 * own: exchanging one display for another is how a navigation is carried out, not a series of
	 * corrections of the address bar - and a correction arriving first would leave the navigation
	 * with an address bar that already shows its target and thus nothing left to report.
	 * </p>
	 *
	 * <p>
	 * A change applied while a URL is being adopted is no navigation of its own: it is the display
	 * settling into the URL the client already shows - a drill-down path materializing into the frames
	 * a deep link names - and reports nothing, because the display is not complete before the adoption
	 * ends, and {@link #finishAdoption()} reports what it composes then.
	 * </p>
	 *
	 * @param displayChange
	 *        The change to apply.
	 */
	public void navigate(Runnable displayChange) {
		boolean before = _suppressNotifications;
		_suppressNotifications = true;
		try {
			displayChange.run();
		} finally {
			_suppressNotifications = before;
		}
		if (_adopting) {
			return;
		}
		notifyUrlChange(false);
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
	 *
	 * <p>
	 * Conversely, a participant of the display that shows a route the URL neither activated nor
	 * brought into the display by one of its activations is asked to
	 * {@link RoutingParticipant#resetRoute() return to the state without a route}: a URL naming fewer
	 * segments than the display shows - the way back from a drilled-down path to the view it started
	 * in - activates nothing, and what it leaves out is what the user navigated away from.
	 * </p>
	 */
	public void finishAdoption() {
		_pendingUrl = null;
		_pendingQuery = Map.of();
		resetUnnamedRoutes();
		notifyUrlChange(true);
		_adopting = false;
		_activatedWhileAdopting.clear();
		_registeredWhileAdopting.clear();
	}

	/**
	 * Ends the adoption of a URL that was refused, leaving the display as it is.
	 *
	 * <p>
	 * A URL the display declines to take up - a form with unsaved input vetoes leaving it - describes
	 * a state that is not reached, so nothing of it is applied: the segments still pending are
	 * dropped and no participant is asked to
	 * {@link RoutingParticipant#resetRoute() return to the state without a route}, because what the
	 * display shows is what the refusal keeps. Nothing is reported either - the caller tells the
	 * client which URL it is left with, together with the refusal - but that URL is recorded as the
	 * one the client shows, so that reaching it again later is a navigation the client is told about.
	 * </p>
	 *
	 * <p>
	 * Ending the adoption is what makes the user's next navigation a history entry again rather than
	 * a replacement of the address the refusal restored.
	 * </p>
	 */
	public void cancelAdoption() {
		_pendingUrl = null;
		_pendingQuery = Map.of();
		_adopting = false;
		_activatedWhileAdopting.clear();
		_registeredWhileAdopting.clear();
		_lastNotifiedUrl = currentUrl();
	}

	/**
	 * Resets the participants of the display whose route the adopted URL did not name.
	 *
	 * <p>
	 * Reported as one change, once the resets are through: the intermediate states a reset passes
	 * through - a path shortened frame by frame - are steps of the same adoption, not addresses of
	 * their own.
	 * </p>
	 */
	private void resetUnnamedRoutes() {
		List<RoutingParticipant> displayed = new ArrayList<>(composingParticipants());
		boolean before = _suppressNotifications;
		_suppressNotifications = true;
		try {
			for (RoutingParticipant participant : displayed) {
				if (_activatedWhileAdopting.contains(participant)
					|| _registeredWhileAdopting.contains(participant)) {
					continue;
				}
				if (!_participants.contains(participant)) {
					// Gone from the display while an earlier reset was applied.
					continue;
				}
				RouteSegment segment = participant.activeRouteSegment();
				if (segment == null || segment.path().isEmpty()) {
					continue;
				}
				participant.resetRoute();
			}
		} finally {
			_suppressNotifications = before;
		}
	}

	/**
	 * Composes the current URL from the active segments of the participants the display contains.
	 *
	 * <p>
	 * The paths of the segments form the path of the URL, in display order, and the query parameters
	 * of the segments form its query string, in the same order. A parameter several participants
	 * contribute is carried by the last of them, because one URL has one value for a name.
	 * </p>
	 *
	 * @return The composed URL (without leading slash, with its query string), or empty string if no
	 *         segments are active.
	 *
	 * @see #setDisplayedParticipants(Supplier)
	 */
	public String currentUrl() {
		StringBuilder sb = new StringBuilder();
		Map<String, String> query = new LinkedHashMap<>();
		for (RoutingParticipant participant : composingParticipants()) {
			RouteSegment segment = participant.activeRouteSegment();
			if (segment == null) {
				continue;
			}
			if (!segment.path().isEmpty()) {
				if (sb.length() > 0) {
					sb.append('/');
				}
				sb.append(segment.path());
			}
			query.putAll(segment.queryParams());
		}
		appendQuery(sb, query);
		return sb.toString();
	}

	/**
	 * Whether the given segment describes anything the URL carries.
	 */
	private static boolean contributes(RouteSegment segment) {
		return segment != null && (!segment.path().isEmpty() || !segment.queryParams().isEmpty());
	}

	/**
	 * Appends the given query parameters to the given URL, percent-encoded.
	 */
	private static void appendQuery(StringBuilder url, Map<String, String> query) {
		char separator = QUERY_START;
		for (Map.Entry<String, String> parameter : query.entrySet()) {
			url.append(separator);
			url.append(encodeQuery(parameter.getKey()));
			url.append(VALUE_SEPARATOR);
			url.append(encodeQuery(parameter.getValue()));
			separator = PARAM_SEPARATOR;
		}
	}

	/**
	 * The parameters of the given query string, decoded.
	 *
	 * <p>
	 * A parameter whose escapes name no character - a query string typed by hand - keeps the form it
	 * was written in, so that a URL is taken up as far as it can be understood instead of failing as
	 * a whole.
	 * </p>
	 */
	private static Map<String, String> parseQuery(String query) {
		Map<String, String> result = new LinkedHashMap<>();
		int length = query.length();
		int start = 0;
		while (start < length) {
			int end = query.indexOf(PARAM_SEPARATOR, start);
			if (end < 0) {
				end = length;
			}
			String parameter = query.substring(start, end);
			start = end + 1;
			if (parameter.isEmpty()) {
				continue;
			}
			int assignment = parameter.indexOf(VALUE_SEPARATOR);
			String name = assignment < 0 ? parameter : parameter.substring(0, assignment);
			String value = assignment < 0 ? "" : parameter.substring(assignment + 1);
			result.put(decodeQuery(name), decodeQuery(value));
		}
		return result;
	}

	private static String encodeQuery(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private static String decodeQuery(String value) {
		try {
			return URLDecoder.decode(value, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException ex) {
			return value;
		}
	}

	/**
	 * The path portion of the given URL, without its query string.
	 */
	private static String pathOf(String url) {
		int queryStart = url.indexOf(QUERY_START);
		return queryStart < 0 ? url : url.substring(0, queryStart);
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

	/**
	 * Hands the pending URL to the given participant, as far as the participant takes it up.
	 *
	 * <p>
	 * One route for most participants, and route after route for a participant that
	 * {@link RoutingParticipant#acceptsRouteSequence() displays a chain of them}: what such a
	 * participant took up leaves the pending URL, and the rest is offered to it again. The offering
	 * stops where nothing matches any more, or where the participant is gone from the display over
	 * the route it took up - what the URL names beyond that belongs to whatever appeared instead.
	 * </p>
	 */
	private void tryResolvePending(RoutingParticipant participant) {
		while (true) {
			String segmentToResolve = computeUnresolvedSegment();
			if (segmentToResolve.isEmpty()) {
				return;
			}

			RouteMatch match = firstMatch(participant, segmentToResolve);
			if (match == null) {
				return;
			}

			_pendingUrl = match.remainingPath();
			if (_pendingUrl.isEmpty()) {
				_pendingUrl = null;
			}
			_activatedWhileAdopting.add(participant);
			// Counted, so that a participant registering from inside the activation is recognized as
			// brought into the display by the URL - as opposed to one that registers because the
			// display it belongs to is attached again.
			_activationDepth++;
			try {
				participant.activateRoute(match);
			} finally {
				_activationDepth--;
			}

			if (!participant.acceptsRouteSequence() || !_participants.contains(participant)) {
				return;
			}
		}
	}

	/**
	 * The first route of the given participant that matches the given path, or {@code null} if none
	 * does.
	 */
	private static RouteMatch firstMatch(RoutingParticipant participant, String path) {
		for (RoutePattern pattern : participant.declaredRoutes()) {
			RouteMatch match = pattern.match(path);
			if (match != null) {
				return match;
			}
		}
		return null;
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
		// While a requested URL is being adopted, a segment appearing is the display settling into
		// that URL, not a navigation away from it: a view that a URL leaves unspecified keeps the
		// value it has - the default a table selects, for instance - and the address bar gains that
		// value without an entry the user would have to press back twice to leave.
		notifyUrlChange(_adopting);
	}

	private List<RoutingParticipant> composingParticipants() {
		Supplier<List<RoutingParticipant>> source = _displayedParticipants;
		return source == null ? _participants : source.get();
	}

	/**
	 * Whether the display contains the given participant, as far as the display is known - see
	 * {@link #setDisplayedParticipants(Supplier)}.
	 */
	private boolean isDisplayed(RoutingParticipant participant) {
		Supplier<List<RoutingParticipant>> source = _displayedParticipants;
		return source == null || source.get().contains(participant);
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
		// A change of the query alone refines what the page shows - a filter the user narrows, a
		// sorting they pick - and stays on that page: the address bar has to name what is shown, but
		// the back button belongs to the page the user came from, not to the term they typed before
		// the current one.
		boolean queryOnly = _lastNotifiedUrl != null && pathOf(url).equals(pathOf(_lastNotifiedUrl));
		_lastNotifiedUrl = url;
		if (_urlChangeHandler != null) {
			_urlChangeHandler.onUrlChange(url, replace || queryOnly);
		}
	}
}
