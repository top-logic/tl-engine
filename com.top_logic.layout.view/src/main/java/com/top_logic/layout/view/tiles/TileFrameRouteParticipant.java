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
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.routing.RouteChangeListener;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.react.routing.RouteSegment;
import com.top_logic.layout.react.routing.RoutingParticipant;
import com.top_logic.layout.view.channel.ViewChannel;

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
 * A URL is taken up frame by frame along the {@link TileStackScope#root() graph of frame
 * declarations}: the participant is offered the route of the frame that follows the ones the URL has
 * already established, looks for it among the {@link FrameRoute#successors() successors} of the
 * declaration of the frame below - for the first route, among the declarations directly inside the
 * stack - puts that frame on the path, and is offered the rest of the URL again for the frame above
 * it. A frame whose content follows from the frame below it takes its
 * {@link FrameRoute#pathParams() path parameters} from the frames established so far. A route that
 * no successor declares - a frame named out of the order the user can drill down in - or that names
 * something the display cannot show - a deleted object, a mistyped identifier, a path parameter no
 * frame below holds - ends the path at the frames the URL established, the rest of the URL is not
 * taken up, and the address bar is then corrected to that path.
 * </p>
 *
 * <p>
 * The page is named by the frame the path leads to: the label of a frame is fixed when the frame is
 * put on the path, so the title changes with the path and is reported with it. A frame whose route
 * is declared the {@link FrameRoute#isFinalTitle() final title} keeps that title against whatever
 * it displays inside.
 * </p>
 */
public class TileFrameRouteParticipant implements RoutingParticipant {

	private final TileStackScope _scope;

	private final RouteManager _routeManager;

	private final ViewChannel _pathChannel;

	private final Registration _pathRegistration;

	private final List<RouteChangeListener> _listeners = new ArrayList<>();

	/**
	 * The URL adoption in which the path ended at a route that could not be taken up, so that the
	 * rest of that URL is not offered again.
	 */
	private long _endedAdoption = -1;

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

		_pathRegistration = _pathChannel.addListener((sender, oldValue, newValue) -> notifyRouteChange());
	}

	/**
	 * Stops watching the path, called when the frame this participant belongs to is disposed.
	 */
	public void dispose() {
		_pathRegistration.dispose();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The routes of the successors of the frame the URL established last come first, the routes of
	 * all other declarations after them: a URL naming one of those is a frame out of order, which
	 * ends the path. Once the path ended, nothing more of that URL is taken up.
	 * </p>
	 */
	@Override
	public List<RoutePattern> declaredRoutes() {
		long adoptionId = _routeManager.adoptionId();
		if (adoptionId == _endedAdoption) {
			return List.of();
		}
		List<FrameRoute> successors = successors(_scope.restoreBase(adoptionId));
		List<RoutePattern> result = new ArrayList<>();
		for (FrameRoute route : successors) {
			addPattern(result, route);
		}
		for (FrameRoute route : _scope.declarations()) {
			if (!successors.contains(route)) {
				addPattern(result, route);
			}
		}
		return result;
	}

	private static void addPattern(List<RoutePattern> result, FrameRoute route) {
		RoutePattern pattern = route.pattern();
		if (pattern != null && !result.contains(pattern)) {
			result.add(pattern);
		}
	}

	/**
	 * The declarations of the frames that may follow the first given number of frames of the path.
	 */
	private List<FrameRoute> successors(int base) {
		List<TileFrame> path = _scope.getPath();
		FrameRoute top = _scope.top(path.subList(0, Math.min(base, path.size())));
		return top == null ? List.of() : top.successors();
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
	 * A route that is no successor of the frame below, or that names something the display cannot
	 * show, ends the path there: the frames the URL established stay, everything the display held
	 * above them goes. The URL is the authority for the path from its first frame on, so a frame it
	 * names but cannot fill is not a reason to keep showing a different one.
	 * </p>
	 */
	@Override
	public void activateRoute(RouteMatch match) {
		long adoptionId = _routeManager.adoptionId();
		if (adoptionId == _endedAdoption) {
			return;
		}
		int base = _scope.restoreBase(adoptionId);

		FrameRoute route = FrameRoute.byPattern(successors(base), match.pattern());
		if (route == null) {
			FrameRoute misplaced = FrameRoute.byPattern(_scope.declarations(), match.pattern());
			if (misplaced == null) {
				return;
			}
			Logger.info("Route '" + match.pattern() + "' names " + misplaced.describe()
				+ ", which does not follow the frame below it, ending the path at " + base + " frames.",
				TileFrameRouteParticipant.class);
			endPath(adoptionId, base);
			return;
		}

		Map<String, Object> params = route.resolveParams(match, _scope.getPath().subList(0, base));
		if (params == null) {
			Logger.info("Route '" + match.pattern() + "' names nothing " + route.describe()
				+ " can display, ending the path at " + base + " frames.",
				TileFrameRouteParticipant.class);
			endPath(adoptionId, base);
			return;
		}

		_scope.restore(base, route, params, adoptionId);
	}

	/**
	 * Ends the path the given adoption restores at the given number of frames.
	 */
	private void endPath(long adoptionId, int base) {
		_endedAdoption = adoptionId;
		_scope.popTo(base);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The routes of all frames on the path, in the order the user drilled down, so that the address
	 * describes the whole path and not only the frame that is displayed. The address describes the
	 * deepest prefix of the path it can: a frame whose declaration is no successor of the one of the
	 * frame below, one whose declaration has no route, or one whose parameters no conversion turns
	 * into a URL text, has no address - and neither has anything drilled into from it, which only
	 * the frame below identifies.
	 * </p>
	 */
	@Override
	public RouteSegment activeRouteSegment() {
		StringBuilder path = new StringBuilder();
		for (String segment : addressedSegments()) {
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
	 * The label of the innermost frame the address describes that has one - the label the
	 * breadcrumb shows for it. A frame drilled into beyond the address names no page the URL leads
	 * back to, and neither does the initial view of the stack, which leaves the title to the
	 * participants around the stack.
	 * </p>
	 */
	@Override
	public ResKey activeRouteLabel() {
		TileFrame frame = titleFrame();
		return frame == null ? null : frame.getLabel();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * What the frame naming the page displays does not rename it where the route of that frame is
	 * declared the {@link FrameRoute#isFinalTitle() final title}.
	 * </p>
	 */
	@Override
	public boolean titlesContent() {
		int position = titlePosition();
		if (position < 0) {
			return true;
		}
		return !_scope.declarations(_scope.getPath()).get(position).isFinalTitle();
	}

	/**
	 * The innermost frame the address describes that has a label, or {@code null} if there is none.
	 */
	private TileFrame titleFrame() {
		int position = titlePosition();
		return position < 0 ? null : _scope.getPath().get(position);
	}

	/**
	 * The position of the innermost frame the address describes that has a label, {@code -1} if
	 * there is none.
	 */
	private int titlePosition() {
		List<TileFrame> path = _scope.getPath();
		int result = -1;
		for (int n = 0, cnt = addressedSegments().size(); n < cnt; n++) {
			if (path.get(n).getLabel() != null) {
				result = n;
			}
		}
		return result;
	}

	/**
	 * The URL segments of the frames of the path, from the bottom up, as far as the path has an
	 * address.
	 *
	 * <p>
	 * The n-th segment belongs to the n-th frame of the path, and the list ends before the first
	 * frame without an address.
	 * </p>
	 */
	private List<String> addressedSegments() {
		List<String> result = new ArrayList<>();
		List<TileFrame> path = _scope.getPath();
		List<FrameRoute> declarations = _scope.declarations(path);
		for (int n = 0; n < path.size(); n++) {
			TileFrame frame = path.get(n);
			String segment = n < declarations.size() ? declarations.get(n).segment(frame.getParams()) : null;
			if (segment == null) {
				Logger.debug(() -> "Frame '" + frame.getFrameId() + "' has no address, "
					+ "the URL describes the path up to it.", TileFrameRouteParticipant.class);
				break;
			}
			result.add(segment);
		}
		return result;
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
