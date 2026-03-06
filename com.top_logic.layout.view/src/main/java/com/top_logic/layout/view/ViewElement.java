/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.layout.view.channel.ViewChannel;

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
		List<PolymorphicConfiguration<? extends UIElement>> getContent();
	}

	private final List<ChannelConfig> _channelConfigs;

	private final List<UIElement> _content;

	/**
	 * Creates a new {@link ViewElement} from configuration.
	 */
	@CalledByReflection
	public ViewElement(InstantiationContext context, Config config) {
		_channelConfigs = config.getChannels();
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
	public ViewControl createControl(ViewContext context) {
		// Phase 2a: Create and register channels.
		for (ChannelConfig channelConfig : _channelConfigs) {
			String name = channelConfig.getName();
			if (context.hasChannel(name)) {
				// Pre-bound by parent via <view-ref> binding. Skip local instantiation.
				continue;
			}
			ViewChannel channel = new DefaultInstantiationContext(ViewElement.class).getInstance(channelConfig);
			context.registerChannel(name, channel);
		}

		if (_content.size() == 1) {
			return _content.get(0).createControl(context);
		}
		List<ReactControl> children = _content.stream()
			.map(e -> (ReactControl) e.createControl(context))
			.collect(Collectors.toList());
		return new ReactStackControl(children);
	}
}
