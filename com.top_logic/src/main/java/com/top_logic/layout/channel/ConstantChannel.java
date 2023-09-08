/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ComponentChannel} with a constant model value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstantChannel extends AbstractComponentChannel {

	private Object _value;

	/**
	 * Creates a {@link ConstantChannel}.
	 *
	 * @param component
	 *        See {@link #getComponent()}.
	 * @param value
	 *        The constant value to return from this channel, see {@link #get()}.
	 */
	public ConstantChannel(LayoutComponent component, Object value) {
		super(component, "const(" + value + ")");
		_value = value;
	}

	@Override
	public Object get() {
		return _value;
	}

	@Override
	protected void storeValue(Object newValue, Object oldValue) {
		// Ignore, cannot be updated.
	}

	@Override
	public Collection<ComponentChannel> sources() {
		return Collections.emptyList();
	}

	@Override
	public void link(ComponentChannel source) {
		throw new UnsupportedOperationException("A constant channel cannot be linked to some source.");
	}

	@Override
	public void unlink(ComponentChannel source) {
		throw new UnsupportedOperationException("A constant channel cannot be unlinked from a source.");
	}

}
