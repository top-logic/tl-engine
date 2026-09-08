/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.routing;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.routing.RouteChangeListener;
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.react.routing.RouteSegment;
import com.top_logic.layout.react.routing.RoutingParticipant;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Lightweight {@link RoutingParticipant} that connects a view channel to a URL path segment via
 * param-binding.
 *
 * <p>
 * Created by {@link com.top_logic.layout.view.ViewElement} when {@code <param-bindings>} are
 * configured. The participant listens to the bound channel for forward propagation (selection to
 * URL) and accepts route activation for backward propagation (deep-link to selection).
 * </p>
 */
public class ParamBindingParticipant implements RoutingParticipant {

	private final String _routeParamName;

	private final ViewChannel _channel;

	private final RoutePattern _pattern;

	private final List<RouteChangeListener> _listeners = new ArrayList<>();

	/**
	 * Creates a new {@link ParamBindingParticipant}.
	 *
	 * @param routeParamName
	 *        The route parameter name (e.g., "companyId").
	 * @param channel
	 *        The view channel bound to this parameter.
	 */
	public ParamBindingParticipant(String routeParamName, ViewChannel channel) {
		_routeParamName = routeParamName;
		_channel = channel;
		_pattern = RoutePattern.compile(":" + routeParamName, routeParamName);

		// Listen to channel changes (forward: selection -> URL). A cleared channel is reported as
		// well, so that the segment leaves the address bar with the value it described.
		_channel.addListener((sender, oldValue, newValue) -> {
			RouteSegment segment = segment(newValue);
			RouteSegment reported = segment != null ? segment : new RouteSegment("");
			for (RouteChangeListener l : new ArrayList<>(_listeners)) {
				l.onRouteChange(this, reported);
			}
		});
	}

	@Override
	public List<RoutePattern> declaredRoutes() {
		return List.of(_pattern);
	}

	@Override
	public void activateRoute(RouteMatch match) {
		// Backward: deep-link -> write param value into channel.
		String value = match.params().get(_routeParamName);
		if (value != null) {
			_channel.set(value);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The segment is the value the channel holds, not the one the URL delivered: a segment the
	 * binding resolved to nothing - a deleted object, a mistyped link - has no value to describe and
	 * must not stay in the address bar beside a view that does not display it.
	 * </p>
	 */
	@Override
	public RouteSegment activeRouteSegment() {
		return segment(_channel.get());
	}

	/**
	 * The segment describing the given channel value, or {@code null} if the value contributes none.
	 */
	private static RouteSegment segment(Object value) {
		if (value == null) {
			return null;
		}
		String path = value.toString();
		return path.isEmpty() ? null : new RouteSegment(path);
	}

	@Override
	public void addRouteChangeListener(RouteChangeListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeRouteChangeListener(RouteChangeListener listener) {
		_listeners.remove(listener);
	}
}
