/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ComponentChannel} that only receives values from {@link #link(ComponentChannel) linked}
 * sources, but does not observe its destinations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ReceivingChannel extends AbstractInputChannel implements ChannelListener {

	private Object _received;

	/**
	 * Creates a {@link ReceivingChannel}.
	 * 
	 * @param name
	 *        Channel name for debugging.
	 */
	public ReceivingChannel(LayoutComponent component, String name) {
		super(component, name);
	}

	@Override
	public Object get() {
		return _received;
	}

	@Override
	protected void storeValue(Object newValue, Object oldValue) {
		_received = newValue;
		notifyNewValue(oldValue, newValue);
	}

	@Override
	public void link(ComponentChannel source) {
		source.addListener(this);
		set(source.get());

		super.link(source);
	}

	@Override
	public void unlink(ComponentChannel source) {
		super.unlink(source);

		source.removeListener(this);
	}

	@Override
	public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
		_received = newValue;
		notifyNewValue(oldValue, newValue);
	}
}