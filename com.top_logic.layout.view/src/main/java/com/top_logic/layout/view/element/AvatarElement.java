/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ReactAvatarControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Declarative {@link UIElement} displaying a channel's value (e.g. a person) as a circular
 * initials avatar.
 */
public class AvatarElement implements UIElement {

	/**
	 * Configuration for {@link AvatarElement}.
	 */
	@TagName("avatar")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(AvatarElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/**
		 * Channel whose value is displayed; the avatar derives initials and color from the value's
		 * label.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final Config _config;

	/**
	 * Creates a new {@link AvatarElement} from configuration.
	 */
	@CalledByReflection
	public AvatarElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel channel = context.resolveChannel(_config.getInput());
		ReactAvatarControl control = new ReactAvatarControl(context, label(channel.get()));
		ChannelListener listener = (sender, oldValue, newValue) -> control.setName(label(newValue));
		channel.addListener(listener);
		control.addCleanupAction(() -> channel.removeListener(listener));
		return control;
	}

	private static String label(Object value) {
		return value == null ? null : MetaLabelProvider.INSTANCE.getLabel(value);
	}
}
