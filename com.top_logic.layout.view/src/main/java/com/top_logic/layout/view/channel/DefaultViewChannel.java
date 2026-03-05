/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Default {@link ViewChannel} implementation that holds a mutable value.
 *
 * <p>
 * Thread-safe for listener management via {@link CopyOnWriteArrayList}. The {@link #set(Object)}
 * method uses {@link Objects#equals(Object, Object)} to detect changes and only fires listeners
 * when the value actually changes.
 * </p>
 */
public class DefaultViewChannel implements ViewChannel {

	private final String _name;

	private Object _value;

	private final CopyOnWriteArrayList<ChannelListener> _listeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates a {@link DefaultViewChannel} from configuration.
	 *
	 * @param context
	 *        The instantiation context.
	 * @param config
	 *        The channel configuration providing the channel name.
	 */
	@CalledByReflection
	public DefaultViewChannel(InstantiationContext context, ValueChannelConfig config) {
		_name = config.getName();
	}

	/**
	 * Creates a {@link DefaultViewChannel} programmatically.
	 *
	 * @param name
	 *        The channel name (for debugging).
	 */
	public DefaultViewChannel(String name) {
		_name = name;
	}

	@Override
	public Object get() {
		return _value;
	}

	@Override
	public boolean set(Object newValue) {
		Object oldValue = _value;
		if (Objects.equals(oldValue, newValue)) {
			return false;
		}
		_value = newValue;
		notifyListeners(oldValue, newValue);
		return true;
	}

	@Override
	public void addListener(ChannelListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeListener(ChannelListener listener) {
		_listeners.remove(listener);
	}

	private void notifyListeners(Object oldValue, Object newValue) {
		for (ChannelListener listener : _listeners) {
			listener.handleNewValue(this, oldValue, newValue);
		}
	}

	@Override
	public String toString() {
		return "ViewChannel[" + _name + "=" + _value + "]";
	}
}
