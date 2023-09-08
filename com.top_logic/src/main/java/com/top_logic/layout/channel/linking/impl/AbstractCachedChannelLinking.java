/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.impl;

import com.top_logic.basic.Log;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Base class for {@link ChannelLinking} implementations caching the channel in the context
 * {@link LayoutComponent}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class AbstractCachedChannelLinking<T extends ModelSpec> extends AbstractChannelLinking<T> {

	private TypedAnnotatable.Property<ComponentChannel> _channel =
		TypedAnnotatable.property(ComponentChannel.class, "channel");

	/**
	 * Creates a {@link AbstractCachedChannelLinking}.
	 */
	public AbstractCachedChannelLinking(InstantiationContext context, T config) {
		super(context, config);
	}

	@Override
	public ComponentChannel resolveChannel(Log log, LayoutComponent contextComponent) {
		ComponentChannel channel = contextComponent.get(_channel);

		if (channel == null) {
			channel = createChannel(log, contextComponent);
			contextComponent.set(_channel, channel);
		}

		return channel;
	}

	/**
	 * Creates the component channel.
	 */
	protected abstract ComponentChannel createChannel(Log log, LayoutComponent contextComponent);

}
