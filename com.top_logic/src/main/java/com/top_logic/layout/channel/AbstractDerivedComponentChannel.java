/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Base class for read-only {@link ComponentChannel}s that derive their value from some source
 * channels.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDerivedComponentChannel extends AbstractComponentChannel implements ChannelListener {

	private Object _value;

	private final ChannelListener _updateValue = (sender, oldValue, newValue) -> set(newValue);

	/**
	 * Creates a {@link AbstractDerivedComponentChannel}.
	 * 
	 * @param name
	 *        Channel name for debugging.
	 */
	public AbstractDerivedComponentChannel(LayoutComponent component, String name) {
		super(component, name);
	}

	@Override
	protected void storeValue(Object newValue, Object oldValue) {
		_value = newValue;
		notifyNewValue(oldValue, newValue);
	}

	@Override
	public Object get() {
		if (hasListeners()) {
			return _value;
		} else {
			return lookupValue();
		}
	}

	@Override
	public void link(ComponentChannel source) {
		source.addListener(this);
		source.internalLinkBack(this);
	}

	/**
	 * Old channel value.
	 */
	protected Object oldValue() {
		return _value;
	}

	@Override
	public void unlink(ComponentChannel source) {
		source.internalUnlinkFrom(this);
		source.removeListener(this);
	}

	@Override
	public void internalLinkBack(ComponentChannel destination) {
		set(destination.get());
		destination.addListener(_updateValue);

		super.internalLinkBack(destination);
	}

	@Override
	public void internalUnlinkFrom(ComponentChannel destination) {
		super.internalUnlinkFrom(destination);

		destination.removeListener(_updateValue);
	}

	@Override
	public boolean addListener(ChannelListener other) {
		boolean unobserved = !hasListeners();
		boolean result = super.addListener(other);
		if (unobserved && result) {
			attach();
			update();
		}
		return result;
	}

	/**
	 * Starts observing its sources.
	 */
	protected abstract void attach();

	@Override
	public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
		// Some receiving channel has a new value.
		update();
	}

	/**
	 * Triggers a {@link #lookupValue() lookup} of a potentially new value.
	 * 
	 * <p>
	 * Must be called upon events that may change the result of {@link #lookupValue()}.
	 * </p>
	 */
	protected final void update() {
		set(lookupValue());
	}

	/**
	 * Looks up a new version of the current channel value.
	 */
	protected abstract Object lookupValue();

	@Override
	public boolean removeListener(ChannelListener other) {
		boolean result = super.removeListener(other);
		if (result && !hasListeners()) {
			detach();
		}
		return result;
	}

	/**
	 * Stops observing its sources.
	 */
	protected abstract void detach();

	@Override
	public void destroy() {
		detach();
		super.destroy();
	}

}
