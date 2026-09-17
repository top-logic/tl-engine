/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.layout.react.routing.RouteChangeListener;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.react.routing.RouteSegment;
import com.top_logic.layout.react.routing.RoutingParticipant;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * The {@link RoutingParticipant} of a mounted frame of a {@link TileStackElement tile stack}: it
 * writes the drill-down path into the URL and restores it from a URL that is opened.
 *
 * <p>
 * One participant exists per stack, created by {@link ReactTileStackControl} and anchored at the
 * control of the stack, so that the path appears in the URL exactly where the stack appears in the
 * display. It speaks for the whole path: the segment it contributes is the {@link FrameRoute route}
 * of every frame on the path, one after the other.
 * </p>
 *
 * <p>
 * A URL is taken up frame by frame: the participant is offered the route of the frame that follows
 * the ones the URL has already established, puts that frame on the path, and is offered the rest of
 * the URL again for the frame above it. A route naming something the display cannot show - a deleted
 * object, a mistyped identifier - ends the path at the frames the URL established, and the address
 * bar is then corrected to that path.
 * </p>
 */
public class TileFrameRouteParticipant implements RoutingParticipant {

	private final TileStackScope _scope;

	private final RouteManager _routeManager;

	private final ViewChannel _pathChannel;

	private final ChannelListener _pathListener;

	private final List<RouteChangeListener> _listeners = new ArrayList<>();

	/**
	 * Creates a {@link TileFrameRouteParticipant}.
	 *
	 * @param scope
	 *        The stack whose path this participant describes. Its path channel is watched, so that a
	 *        drill-down reaches the address bar.
	 * @param routeManager
	 *        The manager the participant is registered with, asked which URL adoption is in
	 *        progress.
	 */
	public TileFrameRouteParticipant(TileStackScope scope, RouteManager routeManager) {
		_scope = scope;
		_pathChannel = scope.pathChannel();
		_routeManager = routeManager;

		_pathListener = (sender, oldValue, newValue) -> notifyRouteChange();
		_pathChannel.addListener(_pathListener);
	}

	/**
	 * Stops watching the path, called when the frame this participant belongs to is disposed.
	 */
	public void dispose() {
		_pathChannel.removeListener(_pathListener);
	}

	@Override
	public List<RoutePattern> declaredRoutes() {
		List<RoutePattern> result = new ArrayList<>();
		for (FrameRoute route : _scope.frameRoutes()) {
			result.add(route.pattern());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The frames of a path are named one route each, and the URL names them from the bottom up.
	 * </p>
	 */
	@Override
	public boolean acceptsRouteSequence() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Puts the frame the route names above the ones the URL has established so far, which for the
	 * first route of an adoption is nothing: the URL describes the path from its first frame, so
	 * what the display holds from an earlier navigation is replaced rather than extended.
	 * </p>
	 *
	 * <p>
	 * A route naming something the display cannot show ends the path there: the frames the URL
	 * established stay, everything the display held above them goes. The URL is the authority for
	 * the path from its first frame on, so a frame it names but cannot fill is not a reason to keep
	 * showing a different one.
	 * </p>
	 */
	@Override
	public void activateRoute(RouteMatch match) {
		FrameRoute route = FrameRoute.byPattern(_scope.frameRoutes(), match.pattern());
		if (route == null) {
			return;
		}

		long adoptionId = _routeManager.adoptionId();
		int base = _scope.restoreBase(adoptionId);

		Map<String, Object> params = route.resolveParams(match);
		if (params == null) {
			Logger.info("Route '" + match.pattern() + "' names nothing the frame '" + route.viewRef()
				+ "' can display, ending the path at " + base + " frames.",
				TileFrameRouteParticipant.class);
			_scope.popTo(base);
			return;
		}

		_scope.restore(base, route.viewRef(), params, adoptionId);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The routes of all frames on the path, in the order the user drilled down, so that the address
	 * describes the whole path and not only the frame that is displayed. The address describes the
	 * deepest prefix of the path it can: a frame the stack declares no route for, or one whose
	 * parameters no conversion turns into a URL text, has no address - and neither has anything
	 * drilled into from it, which only the frame below identifies.
	 * </p>
	 */
	@Override
	public RouteSegment activeRouteSegment() {
		StringBuilder path = new StringBuilder();
		for (TileFrame frame : _scope.getPath()) {
			FrameRoute route = _scope.frameRoute(frame.getViewRef());
			String segment = route == null ? null : route.segment(frame.getParams());
			if (segment == null) {
				Logger.debug(() -> "Frame '" + frame.getViewRef() + "' has no address, "
					+ "the URL describes the path up to it.", TileFrameRouteParticipant.class);
				break;
			}
			if (path.length() > 0) {
				path.append('/');
			}
			path.append(segment);
		}
		return path.length() == 0 ? null : new RouteSegment(path.toString());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Empties the path back to the frames the URL established, which for a URL naming no frame at all
	 * is none: a back navigation out of a drill-down returns to the view it started in.
	 * </p>
	 */
	@Override
	public void resetRoute() {
		_scope.popTo(_scope.restoreBase(_routeManager.adoptionId()));
	}

	@Override
	public void addRouteChangeListener(RouteChangeListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeRouteChangeListener(RouteChangeListener listener) {
		_listeners.remove(listener);
	}

	private void notifyRouteChange() {
		RouteSegment segment = activeRouteSegment();
		RouteSegment reported = segment != null ? segment : new RouteSegment("");
		for (RouteChangeListener listener : new ArrayList<>(_listeners)) {
			listener.onRouteChange(this, reported);
		}
	}
}
