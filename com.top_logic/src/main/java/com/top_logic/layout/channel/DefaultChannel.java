/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Plain {@link ComponentChannel} implementation that manages the {@link #get() model value}
 * independent of its {@link #getComponent()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultChannel extends BidirectionalComponentChannel {

	private Object _value;

	/**
	 * Creates a {@link DefaultChannel}.
	 *
	 * @param component
	 *        See {@link #getComponent()}.
	 * @param name
	 *        Channel name for debugging.
	 * @param initialValue
	 *        The initial value of this channel, see {@link #get()}.
	 */
	public DefaultChannel(LayoutComponent component, String name, Object initialValue) {
		super(component, name);

		_value = initialValue;
	}

	@Override
	public Object get() {
		return _value;
	}

	@Override
	protected void storeValue(Object newValue, Object oldValue) {
		_value = newValue;
		notifyNewValue(oldValue, newValue);
	}

}
