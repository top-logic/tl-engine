/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ChannelSPI} with an initial channel value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultChannelSPI extends ChannelSPI {

	private Object _initialValue;

	/**
	 * Creates a {@link DefaultChannelSPI}.
	 *
	 * @param name
	 *        See {@link ChannelSPI#ChannelSPI(String)}.
	 * @param initialValue
	 *        See {@link #getInitialValue()}.
	 */
	public DefaultChannelSPI(String name, Object initialValue) {
		super(name);
		_initialValue = initialValue;
	}

	/**
	 * The initial value to use for the created channel.
	 */
	public Object getInitialValue() {
		return _initialValue;
	}

	@Override
	protected ComponentChannel createImpl(LayoutComponent component) {
		return new DefaultChannel(component, getName(), _initialValue);
	}

}
