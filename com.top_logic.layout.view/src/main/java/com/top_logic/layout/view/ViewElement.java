/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.routing.RoutingParticipant;
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.layout.view.channel.ChannelFactory;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.routing.ParamBindingConfig;
import com.top_logic.layout.view.routing.ParamBindingParticipant;
import com.top_logic.layout.view.routing.QueryBindingConfig;
import com.top_logic.layout.view.routing.QueryBindingParticipant;

/**
 * The mandatory root element of every {@code .view.xml} file.
 *
 * <p>
 * Establishes the scope boundary for a view. In the future, this is where channel declarations
 * and view-level configuration will be defined.
 * </p>
 */
public class ViewElement implements UIElement {

	/**
	 * Configuration for {@link ViewElement}.
	 */
	@TagName("view")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(ViewElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getChannels()}. */
		String CHANNELS = "channels";

		/**
		 * Channel declarations for this view.
		 *
		 * <p>
		 * Channels are named reactive values that can be read and written by UI elements within
		 * this view.
		 * </p>
		 */
		@Name(CHANNELS)
		List<ChannelConfig> getChannels();

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/**
		 * The root content element of this view.
		 *
		 * @implNote Single-valued (not a list) so that overlays of the same view path recurse-merge
		 *           into this element rather than replacing or appending it; short element names (e.g.
		 *           {@code <app-shell>}) still resolve directly inside {@code <view>} via
		 *           {@code @DefaultContainer}.
		 */
		@Name(CONTENT)
		@DefaultContainer
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		PolymorphicConfiguration<? extends UIElement> getContent();

		/** Configuration name for {@link #getParamBindings()}. */
		String PARAM_BINDINGS = "param-bindings";

		/**
		 * Bindings from URL route parameters to view channels.
		 *
		 * <p>
		 * Route parameters are values extracted from the URL path (e.g., {@code /view/123} where
		 * {@code 123} is a route parameter).
		 * </p>
		 */
		@Name(PARAM_BINDINGS)
		List<ParamBindingConfig> getParamBindings();

		/** Configuration name for {@link #getQueryBindings()}. */
		String QUERY_BINDINGS = "query-bindings";

		/**
		 * Bindings from URL query parameters to view channels.
		 *
		 * <p>
		 * Query parameters are values from the query string (e.g., {@code ?type=foo&sort=name}).
		 * Unlike a {@link #getParamBindings() route parameter}, a query parameter occupies no path
		 * segment: its name identifies it, so it appears and disappears without moving anything else
		 * in the URL, and changing it alone is no history entry.
		 * </p>
		 */
		@Name(QUERY_BINDINGS)
		List<QueryBindingConfig> getQueryBindings();
	}

	private final List<Map.Entry<String, ChannelFactory>> _channelEntries;

	private final List<ParamBindingConfig> _paramBindings;

	private final List<QueryBindingConfig> _queryBindings;

	private final UIElement _content;

	/**
	 * Creates a new {@link ViewElement} from configuration.
	 */
	@CalledByReflection
	public ViewElement(InstantiationContext context, Config config) {
		_channelEntries = config.getChannels().stream()
			.map(cc -> Map.entry(cc.getName(), context.getInstance(cc)))
			.collect(Collectors.toList());
		_paramBindings = config.getParamBindings();
		_queryBindings = config.getQueryBindings();
		PolymorphicConfiguration<? extends UIElement> contentConfig = config.getContent();
		if (contentConfig == null) {
			context.error("View element must have a content element.");
			_content = null;
		} else {
			_content = context.getInstance(contentConfig);
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// Phase 2a: Create and register channels via factories.
		for (Map.Entry<String, ChannelFactory> entry : _channelEntries) {
			String name = entry.getKey();
			if (context.hasChannel(name)) {
				// Pre-bound by parent via <view-ref> binding. Skip local instantiation.
				continue;
			}
			ChannelFactory factory = entry.getValue();
			context.registerChannel(name, factory.createChannel(context));
		}

		// Phase 2b: Create the routing participants of the bindings (registered on attach, not here).
		List<RoutingParticipant> participants = createBindingParticipants(context);

		// Phase 3: Create the content control.
		IReactControl rootControl = _content != null
			? _content.createControl(context)
			: new ReactStackControl(context, List.of());

		// Phase 4: Anchor the participants in the display and wire attach/detach —
		// register/unregister them with the RouteManager.
		if (!participants.isEmpty() && rootControl instanceof ReactControl rc) {
			RouteManager rm = context.getRouteManager();
			if (rm != null) {
				for (RoutingParticipant participant : participants) {
					rc.addRouteParticipant(participant);
				}
				rc.addAttachListener(() -> {
					for (RoutingParticipant participant : participants) {
						rm.register(participant);
					}
				});
				rc.addDetachListener(() -> {
					for (RoutingParticipant participant : participants) {
						rm.unregister(participant);
					}
				});
			}
		}

		return rootControl;
	}

	/**
	 * Creates the {@link RoutingParticipant} of every configured {@code <param-bindings>} and
	 * {@code <query-bindings>} entry.
	 *
	 * <p>
	 * The participants are not registered with the {@link RouteManager} here — registration is
	 * handled by the attach/detach listeners, so that a view whose control tree is attached again
	 * takes part in the routing again.
	 * </p>
	 *
	 * @return The participants of this view, empty where it declares no binding or where the display
	 *         has no {@link RouteManager}.
	 */
	private List<RoutingParticipant> createBindingParticipants(ViewContext context) {
		if (_paramBindings.isEmpty() && _queryBindings.isEmpty()) {
			return List.of();
		}

		RouteManager rm = context.getRouteManager();
		if (rm == null) {
			return List.of();
		}

		List<RoutingParticipant> result = new ArrayList<>();
		for (ParamBindingConfig binding : _paramBindings) {
			result.add(new ParamBindingParticipant(binding.getPrefix(), binding.getRouteParam(),
				channel(context, binding.getChannel())));
		}
		for (QueryBindingConfig binding : _queryBindings) {
			result.add(new QueryBindingParticipant(binding.getQueryParam(),
				channel(context, binding.getChannel())));
		}
		return result;
	}

	/**
	 * The channel of the given name in the given context.
	 */
	private static ViewChannel channel(ViewContext context, String name) {
		return context.resolveChannel(new ChannelRef(name));
	}
}
