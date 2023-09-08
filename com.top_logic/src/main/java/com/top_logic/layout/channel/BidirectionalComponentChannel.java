/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Bi-directional {@link ComponentChannel} that listens on all {@link #sources()} and
 * {@link #destinations()} and sets the received new value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class BidirectionalComponentChannel extends AbstractInputChannel implements ChannelListener {

	/**
	 * Creates a {@link BidirectionalComponentChannel}.
	 *
	 * @param component
	 *        See {@link #getComponent()}.
	 * @param name
	 *        Channel name for debugging.
	 */
	public BidirectionalComponentChannel(LayoutComponent component, String name) {
		super(component, name);
	}

	@Override
	public void link(ComponentChannel source) {
		set(source.get());
		source.addListener(this);

		super.link(source);
	}

	@Override
	public void unlink(ComponentChannel source) {
		source.removeListener(this);

		super.unlink(source);
	}

	@Override
	public void internalLinkBack(ComponentChannel destination) {
		set(destination.get());
		destination.addListener(this);

		super.internalLinkBack(destination);
	}

	@Override
	public void internalUnlinkFrom(ComponentChannel destination) {
		super.internalUnlinkFrom(destination);

		destination.removeListener(this);
	}

	@Override
	public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
		set(newValue);
	}

}
