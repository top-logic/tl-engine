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
 * {@link ChannelFactory} that creates mutable {@link DefaultViewChannel} instances.
 */
public class ValueChannelFactory implements ChannelFactory {

	private final String _name;

	/**
	 * Creates a {@link ValueChannelFactory} from configuration.
	 */
	@CalledByReflection
	public ValueChannelFactory(InstantiationContext context, ValueChannelConfig config) {
		_name = config.getName();
	}

	@Override
	public ViewChannel createChannel(ViewContext context) {
		return new DefaultViewChannel(_name);
	}
}
