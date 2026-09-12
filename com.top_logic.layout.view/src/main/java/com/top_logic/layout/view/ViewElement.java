/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.top_logic.layout.view.channel.ObservingChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.navigation.RevealPath;
import com.top_logic.layout.view.navigation.RevealRegistry;
import com.top_logic.layout.view.routing.ParamBindingConfig;
import com.top_logic.layout.view.routing.ParamBindingParticipant;
import com.top_logic.layout.view.routing.QueryBindingConfig;
import com.top_logic.layout.view.routing.QueryBindingParticipant;
import com.top_logic.model.listen.ModelScope;

/**
 * The mandatory root element of every {@code .view.xml} file.
 *
 * <p>
 * Establishes the scope boundary for a view: its channel declarations, the bindings of URL
 * parameters to those channels, and the content displayed within them.
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

	private String _viewRef;

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
	public List<ChildGroup> getChildGroups() {
		return List.of(ChildGroup.elements(_content));
	}

	/**
	 * Path of the view file this element was read from, relative to
	 * {@link ViewLoader#VIEW_BASE_PATH}.
	 *
	 * @return The path, or {@code null} for a view built from a configuration that no file backs.
	 */
	public String getViewRef() {
		return _viewRef;
	}

	/**
	 * Names the view file this element was read from.
	 *
	 * @param viewRef
	 *        See {@link #getViewRef()}.
	 *
	 * @implNote Called by {@link ViewLoader} right after instantiation: the element is built from a
	 *           configuration, which does not carry the path it was read from, while every instance
	 *           the loader hands out has one.
	 */
	public void initViewRef(String viewRef) {
		_viewRef = viewRef;
	}

	/**
	 * The names of the channels this view declares, in declaration order.
	 *
	 * <p>
	 * These are the names a {@code <view-ref>} to this view can bind to.
	 * </p>
	 */
	public Set<String> getChannelNames() {
		return _channelEntries.stream()
			.map(Map.Entry::getKey)
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// Phase 2a: Create and register channels via factories.
		List<ObservingChannel> observingChannels = new ArrayList<>();
		for (Map.Entry<String, ChannelFactory> entry : _channelEntries) {
			String name = entry.getKey();
			if (context.hasChannel(name)) {
				// Pre-bound by parent via <view-ref> binding. Skip local instantiation.
				continue;
			}
			ChannelFactory factory = entry.getValue();
			ViewChannel channel = factory.createChannel(context);
			context.registerChannel(name, channel);
			if (channel instanceof ObservingChannel observing) {
				observingChannels.add(observing);
			}
		}

		// Phase 2b: Create the routing participants of the bindings (registered on attach, not here).
		List<RoutingParticipant> participants = createBindingParticipants(context);

		// Phase 3: Create the content control.
		IReactControl rootControl = _content != null
			? _content.createControl(context)
			: new ReactStackControl(context, List.of());

		// Phase 3b: Announce this instance as what is displayed at its place, so that displaying an
		// object here finds the channels to write. Cached content (a sidebar item, a tab visited
		// earlier) stays announced while it lives, hence cleanup rather than detach.
		registerDisplay(context, rootControl);

		// Phase 4: Anchor the participants and the observing channels in the display and wire
		// attach/detach — the participants register/unregister with the RouteManager, the channels
		// observe the objects their inputs hold only while the view is on screen.
		if (rootControl instanceof ReactControl rc) {
			RouteManager rm = context.getRouteManager();
			if (!participants.isEmpty() && rm != null) {
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
			if (!observingChannels.isEmpty()) {
				rc.addAttachListener(() -> {
					ModelScope scope = context.getModelScope();
					for (ObservingChannel channel : observingChannels) {
						channel.attach(scope);
					}
				});
				rc.addDetachListener(() -> {
					for (ObservingChannel channel : observingChannels) {
						channel.detach();
					}
				});
			}
		}

		return rootControl;
	}

	/**
	 * Announces this instance in the window's {@link RevealRegistry} for as long as its control
	 * lives.
	 */
	private void registerDisplay(ViewContext context, IReactControl rootControl) {
		if (_viewRef == null || !(rootControl instanceof ReactControl control)) {
			return;
		}
		RevealRegistry registry = context.getRevealRegistry();
		if (registry == null) {
			return;
		}
		control.addCleanupAction(registry.registerView(_viewRef, RevealPath.of(context), context));
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
