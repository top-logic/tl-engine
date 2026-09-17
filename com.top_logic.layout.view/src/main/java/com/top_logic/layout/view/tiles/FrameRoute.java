/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * The compiled route of one frame view of a {@link TileStackElement tile stack}: the URL segments a
 * frame of that view occupies, the conversions of its parameters, and the label naming it.
 *
 * <p>
 * Translates in both directions between a {@link TileFrame} and the piece of URL describing it:
 * {@link #segment(Map)} produces the segments for the parameters a frame holds,
 * {@link #resolveParams(RouteMatch)} turns the parameters a URL carries back into the values a frame
 * is mounted with. Either direction yields nothing where a value has no counterpart on the other
 * side, which is how a frame that cannot be addressed stays out of the URL and a URL that names
 * nothing displayable restores no frame.
 * </p>
 */
public final class FrameRoute {

	private final String _viewRef;

	private final RoutePattern _pattern;

	private final Map<String, Function<Object, String>> _toUrl;

	private final Map<String, Function<String, Object>> _fromUrl;

	private final TileLabelProvider _label;

	/**
	 * Creates a {@link FrameRoute}.
	 *
	 * @param viewRef
	 *        Path of the frame view this route describes, relative to {@code /WEB-INF/views/}.
	 * @param route
	 *        Pattern of the URL segments a frame of that view occupies, e.g.
	 *        {@code "person/:person"}.
	 * @param toUrl
	 *        Conversions of parameter values to URL texts, by parameter name. A parameter without an
	 *        entry is converted by its own text representation.
	 * @param fromUrl
	 *        Conversions of URL texts to parameter values, by parameter name. A parameter without an
	 *        entry is taken as the text it is.
	 * @param label
	 *        Provider naming a frame of this view, or {@code null} for frames named by whoever
	 *        pushes them.
	 */
	public FrameRoute(String viewRef, String route, Map<String, Function<Object, String>> toUrl,
			Map<String, Function<String, Object>> fromUrl, TileLabelProvider label) {
		_viewRef = viewRef;
		_pattern = RoutePattern.compile(route, viewRef);
		_toUrl = Map.copyOf(toUrl);
		_fromUrl = Map.copyOf(fromUrl);
		_label = label;
	}

	/**
	 * Creates a {@link FrameRoute} from its configuration.
	 *
	 * @param context
	 *        Context for instantiating the label provider.
	 * @param config
	 *        The declaration to compile.
	 */
	public static FrameRoute create(InstantiationContext context, FrameRouteConfig config) {
		Map<String, Function<Object, String>> toUrl = new HashMap<>();
		Map<String, Function<String, Object>> fromUrl = new HashMap<>();
		for (FrameParamConfig param : config.getParams()) {
			Expr expr = param.getExpr();
			if (expr != null) {
				QueryExecutor executor = QueryExecutor.compile(expr);
				toUrl.put(param.getName(), value -> asText(executor.execute(value)));
			}
			Expr reverse = param.getReverse();
			if (reverse != null) {
				QueryExecutor executor = QueryExecutor.compile(reverse);
				fromUrl.put(param.getName(), text -> executor.execute(text));
			}
		}
		PolymorphicConfiguration<? extends TileLabelProvider> labelConfig = config.getLabel();
		TileLabelProvider label = labelConfig != null ? context.getInstance(labelConfig) : null;
		return new FrameRoute(config.getView(), config.getRoute(), toUrl, fromUrl, label);
	}

	/**
	 * Path of the frame view this route describes, relative to {@code /WEB-INF/views/}.
	 */
	public String viewRef() {
		return _viewRef;
	}

	/**
	 * The pattern of the URL segments a frame of this view occupies.
	 */
	public RoutePattern pattern() {
		return _pattern;
	}

	/**
	 * The URL segments describing a frame with the given parameters.
	 *
	 * @param params
	 *        The parameters of the frame, as {@link TileFrame#getParams()} holds them.
	 * @return The segments, or {@code null} if a parameter of the route has no value that a URL can
	 *         carry - a frame that cannot be addressed.
	 */
	public String segment(Map<String, Object> params) {
		Map<String, String> texts = new HashMap<>();
		for (String name : _pattern.paramNames()) {
			Function<Object, String> conversion = _toUrl.get(name);
			Object value = params.get(name);
			String text = conversion != null ? conversion.apply(value) : asText(value);
			if (text == null || text.isEmpty()) {
				return null;
			}
			texts.put(name, text);
		}
		return _pattern.produce(texts);
	}

	/**
	 * The frame parameters described by the given match of this route.
	 *
	 * @param match
	 *        A match of {@link #pattern()}.
	 * @return The parameter values, or {@code null} if the URL names something the display cannot
	 *         show - a frame that must not be restored.
	 */
	public Map<String, Object> resolveParams(RouteMatch match) {
		Map<String, Object> params = new LinkedHashMap<>();
		for (String name : _pattern.paramNames()) {
			String text = match.params().get(name);
			if (text == null) {
				return null;
			}
			Function<String, Object> conversion = _fromUrl.get(name);
			Object value = conversion != null ? conversion.apply(text) : text;
			if (value == null) {
				return null;
			}
			params.put(name, value);
		}
		return params;
	}

	/**
	 * The label naming a frame with the given parameters.
	 *
	 * @param params
	 *        The parameters of the frame, as {@link TileFrame#getParams()} holds them.
	 * @return The label, or {@code null} if this route declares none.
	 */
	public ResKey computeLabel(Map<String, Object> params) {
		if (_label == null) {
			return null;
		}
		return _label.compute(paramContext(params));
	}

	/**
	 * A {@link ViewContext} whose channels are the given frame parameters.
	 *
	 * <p>
	 * The label of a frame is computed before the frame exists, so the parameters it will be mounted
	 * with are all there is to compute it from. Reading them as channels of their names is what makes
	 * every {@link TileLabelProvider} usable here, the {@link ScriptedTileLabel scripted} one
	 * included.
	 * </p>
	 */
	private static ViewContext paramContext(Map<String, Object> params) {
		ViewContext context = new DefaultViewContext(null);
		for (Map.Entry<String, Object> param : params.entrySet()) {
			DefaultViewChannel channel = new DefaultViewChannel(param.getKey());
			channel.set(param.getValue());
			context.registerChannel(param.getKey(), channel);
		}
		return context;
	}

	/**
	 * The text naming the given value in a URL, or {@code null} for a value that names nothing.
	 */
	private static String asText(Object value) {
		return value == null ? null : value.toString();
	}

	/**
	 * The route among the given ones that the given pattern belongs to.
	 *
	 * @param routes
	 *        The declared routes to search.
	 * @param pattern
	 *        A pattern taken from one of them, typically {@link RouteMatch#pattern()}.
	 * @return The route declaring the pattern, or {@code null} if none does.
	 */
	public static FrameRoute byPattern(List<FrameRoute> routes, RoutePattern pattern) {
		for (FrameRoute route : routes) {
			if (route.pattern() == pattern) {
				return route;
			}
		}
		return null;
	}

	/**
	 * The route among the given ones that describes frames of the given view.
	 *
	 * @param routes
	 *        The declared routes to search.
	 * @param viewRef
	 *        Path of a frame view, as {@link TileFrame#getViewRef()} holds it.
	 * @return The route describing that view, or {@code null} if none does.
	 */
	public static FrameRoute byView(List<FrameRoute> routes, String viewRef) {
		for (FrameRoute route : routes) {
			if (route.viewRef().equals(viewRef)) {
				return route;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "FrameRoute[" + _viewRef + " -> " + _pattern + "]";
	}
}
