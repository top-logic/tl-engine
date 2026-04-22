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

	private String _currentValue;

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

		// Listen to channel changes (forward: selection -> URL).
		_channel.addListener((sender, oldValue, newValue) -> {
			String newStr = newValue != null ? newValue.toString() : null;
			if (newStr != null && !newStr.equals(_currentValue)) {
				_currentValue = newStr;
				RouteSegment segment = new RouteSegment(_currentValue);
				for (RouteChangeListener l : new ArrayList<>(_listeners)) {
					l.onRouteChange(this, segment);
				}
			} else if (newStr == null && _currentValue != null) {
				_currentValue = null;
				// Parameter cleared - still notify to update URL.
				for (RouteChangeListener l : new ArrayList<>(_listeners)) {
					l.onRouteChange(this, new RouteSegment(""));
				}
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
			_currentValue = value;
			_channel.set(value);
		}
	}

	@Override
	public RouteSegment activeRouteSegment() {
		if (_currentValue != null && !_currentValue.isEmpty()) {
			return new RouteSegment(_currentValue);
		}
		return null;
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
