/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * {@link ViewAction} that writes the input value to a named channel.
 *
 * <p>
 * Passes the input through as output so the chain can continue.
 * </p>
 */
public class WriteChannelAction implements ViewAction {

	/**
	 * Configuration for {@link WriteChannelAction}.
	 */
	@TagName("write-channel")
	public interface Config extends PolymorphicConfiguration<WriteChannelAction> {

		@Override
		@ClassDefault(WriteChannelAction.class)
		Class<? extends WriteChannelAction> getImplementationClass();

		/**
		 * Name of the channel to write to.
		 */
		@Name("name")
		@Mandatory
		String getName();
	}

	private final String _channelName;

	/**
	 * Creates a new {@link WriteChannelAction}.
	 */
	@CalledByReflection
	public WriteChannelAction(InstantiationContext context, Config config) {
		_channelName = config.getName();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (context instanceof ViewContext) {
			ViewContext viewContext = (ViewContext) context;
			if (viewContext.hasChannel(_channelName)) {
				ViewChannel channel = viewContext.resolveChannel(new ChannelRef(_channelName));
				channel.set(input);
			}
		}
		return input;
	}
}
