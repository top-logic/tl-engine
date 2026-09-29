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
 * Declarative {@link UIElement} that wraps its content in a scroll anchor.
 *
 * <p>
 * The anchor is keyed either by an object from the {@link Config#getInput() input} channel (its
 * {@link AnchorControl#anchorId(Object) anchor id}) or by a literal {@link Config#getName() name}
 * for a fixed region without a model object. A {@link ScrollLinkElement &lt;scroll-link&gt;} or a
 * {@code <scroll-to-anchor>} action targeting the same key scrolls the browser here - e.g. wrap
 * each comment card in {@code <anchor input="comment">} to jump to a cited comment, or the new
 * comment editor in {@code <anchor name="composer">} to bring it into view on reply.
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

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		@Override
		@ClassDefault(AnchorElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel providing the object this anchor marks; the anchor key is the object's
		 * {@link AnchorControl#anchorId(Object) anchor id}. Mutually exclusive with
		 * {@link #getName()}.
		 */
		@Name(INPUT)
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Literal anchor key for a fixed region without a model object. Mutually exclusive with
		 * {@link #getInput()}.
		 */
		@Name(NAME)
		String getName();

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
		ReactControl child = (ReactControl) _content.createControl(context.withChildSlotPath("content"));

		if (_config.getInput() != null) {
			ViewChannel channel = context.resolveChannel(_config.getInput());
			AnchorControl control = new AnchorControl(context, child, AnchorControl.anchorId(channel.get()));
			ChannelListener listener =
				(sender, oldValue, newValue) -> control.setKey(AnchorControl.anchorId(newValue));
			channel.addListener(listener);
			control.addCleanupAction(() -> channel.removeListener(listener));
			return control;
		}
		return new AnchorControl(context, child, _config.getName());
	}

}
