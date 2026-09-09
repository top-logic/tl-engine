/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 *
 * <p>
 * The route the binding occupies is a single value segment, optionally preceded by the static
 * segments of a prefix. The segment is produced by the same {@link RoutePattern} that matches it, so
 * the value is percent-encoded on its way into the URL and decoded on its way back, and the prefix
 * appears exactly when there is a value to introduce.
 * </p>
 */
public class ParamBindingParticipant implements RoutingParticipant {

	private final String _routeParamName;

	private final ViewChannel _channel;

	private final RoutePattern _pattern;

	private final List<RouteChangeListener> _listeners = new ArrayList<>();

	/**
	 * Creates a new {@link ParamBindingParticipant} whose route is the value segment alone.
	 *
	 * @param routeParamName
	 *        The route parameter name (e.g., "companyId").
	 * @param channel
	 *        The view channel bound to this parameter.
	 */
	public ParamBindingParticipant(String routeParamName, ViewChannel channel) {
		this(null, routeParamName, channel);
	}

	/**
	 * Creates a new {@link ParamBindingParticipant}.
	 *
	 * @param prefix
	 *        Static path segments placed in front of the value, or {@code null} for a route that is
	 *        the value segment alone.
	 * @param routeParamName
	 *        The route parameter name (e.g., "companyId").
	 * @param channel
	 *        The view channel bound to this parameter.
	 */
	public ParamBindingParticipant(String prefix, String routeParamName, ViewChannel channel) {
		_routeParamName = routeParamName;
		_channel = channel;
		_pattern = RoutePattern.compile(pattern(prefix, routeParamName), routeParamName);

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

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * A route that carries no value for the bound parameter leaves the channel untouched: the URL
	 * says nothing about the value, so what the view establishes itself stands.
	 * </p>
	 */
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
	private RouteSegment segment(Object value) {
		if (value == null) {
			return null;
		}
		String path = value.toString();
		if (path.isEmpty()) {
			return null;
		}
		return new RouteSegment(_pattern.produce(Map.of(_routeParamName, path)));
	}

	/**
	 * The pattern string for the given prefix and parameter name.
	 */
	private static String pattern(String prefix, String routeParamName) {
		String param = RoutePattern.PARAM_PREFIX + routeParamName;
		return prefix == null || prefix.isEmpty() ? param : prefix + '/' + param;
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
