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
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.layout.view.channel.ChannelFactory;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.routing.ParamBindingConfig;
import com.top_logic.layout.view.routing.ParamBindingParticipant;
import com.top_logic.layout.view.routing.QueryBindingConfig;

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
		 * <p>
		 * Exactly one element is expected. The list type enables {@code @TagName} resolution so that
		 * short element names (e.g. {@code <app-shell>}) can be used directly inside {@code <view>}.
		 * </p>
		 */
		@Name(CONTENT)
		@DefaultContainer
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getContent();

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
		 * </p>
		 */
		@Name(QUERY_BINDINGS)
		List<QueryBindingConfig> getQueryBindings();
	}

	private final List<Map.Entry<String, ChannelFactory>> _channelEntries;

	private final List<ParamBindingConfig> _paramBindings;

	private final List<UIElement> _content;

	/**
	 * Creates a new {@link ViewElement} from configuration.
	 */
	@CalledByReflection
	public ViewElement(InstantiationContext context, Config config) {
		_channelEntries = config.getChannels().stream()
			.map(cc -> Map.entry(cc.getName(), context.getInstance(cc)))
			.collect(Collectors.toList());
		_paramBindings = config.getParamBindings();
		List<PolymorphicConfiguration<? extends UIElement>> contentList = config.getContent();
		if (contentList.isEmpty()) {
			context.error("View element must have at least one content element.");
			_content = List.of();
		} else {
			_content = contentList.stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
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

		// Phase 2b: Create param-binding participants (registered on attach, not here).
		List<ParamBindingParticipant> participants = createParamBindingParticipants(context);

		// Phase 3: Create content controls.
		IReactControl rootControl;
		if (_content.size() == 1) {
			rootControl = _content.get(0).createControl(context);
		} else {
			List<ReactControl> children = _content.stream()
				.map(e -> (ReactControl) e.createControl(context))
				.collect(Collectors.toList());
			rootControl = new ReactStackControl(context, children);
		}

		// Phase 4: Wire attach/detach — register/unregister participants with RouteManager.
		if (!participants.isEmpty() && rootControl instanceof ReactControl rc) {
			RouteManager rm = context.getRouteManager();
			if (rm != null) {
				rc.addAttachListener(() -> {
					for (ParamBindingParticipant p : participants) {
						rm.register(p);
					}
				});
				rc.addDetachListener(() -> {
					for (ParamBindingParticipant p : participants) {
						rm.unregister(p);
					}
				});
			}
		}

		return rootControl;
	}

	/**
	 * Creates {@link ParamBindingParticipant}s for each configured {@code <param-bindings>} entry.
	 * The participants are NOT registered with the RouteManager here — registration is handled
	 * by the attach/detach listeners in Phase 4 to support cached view re-attachment.
	 *
	 * @return The list of participants (empty if none configured or no RouteManager).
	 */
	private List<ParamBindingParticipant> createParamBindingParticipants(ViewContext context) {
		if (_paramBindings.isEmpty()) {
			return List.of();
		}

		RouteManager rm = context.getRouteManager();
		if (rm == null) {
			return List.of();
		}

		List<ParamBindingParticipant> result = new ArrayList<>();
		for (ParamBindingConfig binding : _paramBindings) {
			ViewChannel channel = context.resolveChannel(new ChannelRef(binding.getChannel()));
			ParamBindingParticipant participant = new ParamBindingParticipant(
				binding.getRouteParam(), channel);
			result.add(participant);
		}
		return result;
	}
}
