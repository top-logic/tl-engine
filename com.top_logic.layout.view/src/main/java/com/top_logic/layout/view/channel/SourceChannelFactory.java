/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.view.ViewContext;

/**
 * {@link ChannelFactory} creating {@link SourceViewChannel}s fed by a {@link ChannelSource}.
 *
 * <p>
 * The source is created once, together with the factory, and is shared by all channels the factory
 * creates.
 * </p>
 */
public class SourceChannelFactory implements ChannelFactory {

	private final String _name;

	private final ChannelSource _source;

	private final long _updateInterval;

	/**
	 * Creates a {@link SourceChannelFactory} from configuration.
	 */
	@CalledByReflection
	public SourceChannelFactory(InstantiationContext context, SourceChannelConfig config) {
		_name = config.getName();
		_source = context.getInstance(config.getSource());
		_updateInterval = config.getUpdateInterval();
	}

	@Override
	public ViewChannel createChannel(ViewContext context) {
		return new SourceViewChannel(_name, context, _source, _updateInterval);
	}

}
