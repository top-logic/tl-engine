/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.Date;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ReactRelativeTimeControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Declarative {@link UIElement} displaying a channel's date value as relative time ("5 minutes
 * ago"), with the exact timestamp as tooltip.
 */
public class RelativeTimeElement implements UIElement {

	/**
	 * Configuration for {@link RelativeTimeElement}.
	 */
	@TagName("relative-time")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(RelativeTimeElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/**
		 * Channel whose date value is displayed.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final Config _config;

	/**
	 * Creates a new {@link RelativeTimeElement} from configuration.
	 */
	@CalledByReflection
	public RelativeTimeElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel channel = context.resolveChannel(_config.getInput());
		ReactRelativeTimeControl control = new ReactRelativeTimeControl(context, toDate(channel.get()));
		ChannelListener listener =
			(sender, oldValue, newValue) -> control.setValue(toDate(newValue));
		channel.addListener(listener);
		control.addCleanupAction(() -> channel.removeListener(listener));
		return control;
	}

	private static Date toDate(Object value) {
		return value instanceof Date ? (Date) value : null;
	}
}
