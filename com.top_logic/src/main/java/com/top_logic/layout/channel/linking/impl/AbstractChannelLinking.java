/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.impl;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.ModelSpec;

/**
 * Base class for {@link ChannelLinking} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractChannelLinking<T extends ModelSpec> extends AbstractConfiguredInstance<T>
		implements ChannelLinking {

	/**
	 * Creates a {@link AbstractChannelLinking}.
	 */
	public AbstractChannelLinking(InstantiationContext context, T config) {
		super(context, config);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		appendTo(result);
		return result.toString();
	}

}
