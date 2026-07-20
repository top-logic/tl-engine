/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.AnchorControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Declarative {@link UIElement} that wraps its content in a scroll anchor identified by an object
 * from the {@link Config#getInput() input} channel.
 *
 * <p>
 * A {@link ScrollLinkElement &lt;scroll-link&gt;} pointing at the same object scrolls the browser to
 * this anchor - e.g. wrap each comment card of a conversation in {@code <anchor input="comment">} to
 * make replies jump to the cited comment.
 * </p>
 */
public class AnchorElement implements UIElement {

	/**
	 * Configuration for {@link AnchorElement}.
	 */
	@TagName("anchor")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		@Override
		@ClassDefault(AnchorElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel providing the object this anchor marks.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * The wrapped content.
		 */
		@Name(CONTENT)
		@Mandatory
		@DefaultContainer
		@TreeProperty
		PolymorphicConfiguration<? extends UIElement> getContent();
	}

	private final Config _config;

	private final UIElement _content;

	/**
	 * Creates a new {@link AnchorElement} from configuration.
	 */
	@CalledByReflection
	public AnchorElement(InstantiationContext context, Config config) {
		_config = config;
		_content = context.getInstance(config.getContent());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel channel = context.resolveChannel(_config.getInput());
		ReactControl child = (ReactControl) _content.createControl(context.withChildSlotPath("content"));

		AnchorControl control = new AnchorControl(context, child, channel.get());
		ChannelListener listener = (sender, oldValue, newValue) -> control.setTarget(newValue);
		channel.addListener(listener);
		control.addCleanupAction(() -> channel.removeListener(listener));
		return control;
	}

}
