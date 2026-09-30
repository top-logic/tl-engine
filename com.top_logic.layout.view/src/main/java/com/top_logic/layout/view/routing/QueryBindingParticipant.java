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
 * {@link RoutingParticipant} that connects a view channel to a URL query parameter.
 *
 * <p>
 * Created by {@link com.top_logic.layout.view.ViewElement} for a configured
 * {@link QueryBindingConfig}. The binding works in both directions: the value of the channel becomes
 * the value of the parameter in the address bar, and the parameter of an opened URL becomes the
 * value of the channel.
 * </p>
 *
 * <p>
 * The binding occupies no path segment, so it does not depend on where in the URL it stands: the
 * name of the parameter identifies it, which is what a value refining the display - a filter term, a
 * sorting - needs, because it appears and disappears without moving anything else. A value the
 * channel does not hold leaves the parameter out of the URL, and a URL without the parameter says
 * nothing about the value: what the view establishes itself stands, and the address bar is completed
 * with it.
 * </p>
 *
 * @see ParamBindingParticipant
 */
public class QueryBindingParticipant implements RoutingParticipant {

	private final String _queryParamName;

	private final ViewChannel _channel;

	private final List<RouteChangeListener> _listeners = new ArrayList<>();

	/**
	 * Creates a new {@link QueryBindingParticipant}.
	 *
	 * @param queryParamName
	 *        The name of the query parameter carrying the value.
	 * @param channel
	 *        The view channel bound to that parameter.
	 */
	public QueryBindingParticipant(String queryParamName, ViewChannel channel) {
		_queryParamName = queryParamName;
		_channel = channel;

		// Forward: the value the user produces reaches the address bar. A cleared channel is reported
		// as well, so that the parameter leaves the URL with the value it described.
		_channel.addListener((sender, oldValue, newValue) -> {
			RouteSegment segment = segment(newValue);
			RouteSegment reported = segment != null ? segment : new RouteSegment("");
			for (RouteChangeListener listener : new ArrayList<>(_listeners)) {
				listener.onRouteChange(this, reported);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Empty: the binding consumes no path segment, so nothing of the path is its to take up.
	 * </p>
	 */
	@Override
	public List<RoutePattern> declaredRoutes() {
		return List.of();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Never called, because the binding {@link #declaredRoutes() declares no route} a path could
	 * match.
	 * </p>
	 */
	@Override
	public void activateRoute(RouteMatch match) {
		// Nothing to activate: the value of this binding travels in the query.
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * A URL carrying no value for the bound parameter leaves the channel untouched: the URL says
	 * nothing about the value, so what the view establishes itself stands.
	 * </p>
	 */
	@Override
	public void activateQuery(Map<String, String> query) {
		String value = query.get(_queryParamName);
		if (value != null) {
			_channel.set(value);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The parameter carries the value the channel holds, and a channel holding nothing contributes
	 * no parameter: what the address bar names is what the view shows.
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
		String text = value.toString();
		if (text.isEmpty()) {
			return null;
		}
		return new RouteSegment("", Map.of(_queryParamName, text));
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
