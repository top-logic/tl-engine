/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.routing.RouteChangeListener;
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.react.routing.RouteSegment;
import com.top_logic.layout.react.routing.RoutingParticipant;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.tiles.TileLabelProvider;

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
 *
 * <p>
 * A binding with a {@link ParamBindingConfig#getLabel() label} names the page by it while it
 * contributes its segment. The label follows the channels it is computed from for as long as the
 * participant has route change listeners - while it is registered with the route manager of a
 * display, that is - so that the page title changes with them.
 * </p>
 */
public class ParamBindingParticipant implements RoutingParticipant {

	private final String _routeParamName;

	private final ViewChannel _channel;

	private final RoutePattern _pattern;

	private final TileLabelProvider _label;

	private final ViewContext _labelContext;

	private final List<RouteChangeListener> _listeners = new ArrayList<>();

	/**
	 * The subscriptions to the channels the label is computed from, held while the participant has
	 * listeners.
	 */
	private final List<Registration> _labelRegistrations = new ArrayList<>();

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
		this(prefix, routeParamName, channel, null, null);
	}

	/**
	 * Creates a new {@link ParamBindingParticipant} naming the page by a label.
	 *
	 * @param prefix
	 *        Static path segments placed in front of the value, or {@code null} for a route that is
	 *        the value segment alone.
	 * @param routeParamName
	 *        The route parameter name (e.g., "companyId").
	 * @param channel
	 *        The view channel bound to this parameter.
	 * @param label
	 *        The provider of the label naming the page while the binding contributes a segment, or
	 *        {@code null} for a binding that names no page.
	 * @param labelContext
	 *        The context the label is computed in, the context of the view declaring the binding.
	 *        May be {@code null} where no label is given.
	 */
	public ParamBindingParticipant(String prefix, String routeParamName, ViewChannel channel,
			TileLabelProvider label, ViewContext labelContext) {
		_routeParamName = routeParamName;
		_channel = channel;
		_label = label;
		_labelContext = labelContext;
		_pattern = RoutePattern.compile(pattern(prefix, routeParamName), routeParamName);

		// Listen to channel changes (forward: selection -> URL). A cleared channel is reported as
		// well, so that the segment leaves the address bar with the value it described.
		_channel.addListener((sender, oldValue, newValue) -> {
			RouteSegment segment = segment(newValue);
			notifyRouteChange(segment != null ? segment : new RouteSegment(""));
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
	 * {@inheritDoc}
	 *
	 * <p>
	 * The {@link ParamBindingConfig#getLabel() label} of the binding while it contributes a segment.
	 * Without a value there is nothing the label could name, and a binding without a label names
	 * nothing either: an identifier in the URL is no title.
	 * </p>
	 */
	@Override
	public ResKey activeRouteLabel() {
		if (_label == null || activeRouteSegment() == null) {
			return null;
		}
		return _label.compute(_labelContext);
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

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The first listener starts following the channels the label is computed from.
	 * </p>
	 */
	@Override
	public void addRouteChangeListener(RouteChangeListener listener) {
		if (_listeners.isEmpty()) {
			observeLabel();
		}
		_listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The last listener ends following the channels the label is computed from: nobody is left to
	 * tell about a new label.
	 * </p>
	 */
	@Override
	public void removeRouteChangeListener(RouteChangeListener listener) {
		_listeners.remove(listener);
		if (_listeners.isEmpty()) {
			unobserveLabel();
		}
	}

	private void observeLabel() {
		if (_label == null) {
			return;
		}
		for (ViewChannel input : _label.observedChannels(_labelContext)) {
			_labelRegistrations.add(input.addListener((sender, oldValue, newValue) -> onLabelInputChange()));
		}
	}

	private void unobserveLabel() {
		for (Registration registration : _labelRegistrations) {
			registration.dispose();
		}
		_labelRegistrations.clear();
	}

	/**
	 * Reports a possible change of the label, which matters only while the binding names the page.
	 */
	private void onLabelInputChange() {
		RouteSegment segment = activeRouteSegment();
		if (segment != null) {
			notifyRouteChange(segment);
		}
	}

	private void notifyRouteChange(RouteSegment reported) {
		for (RouteChangeListener l : new ArrayList<>(_listeners)) {
			l.onRouteChange(this, reported);
		}
	}
}
