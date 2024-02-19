/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.InlineList;
import com.top_logic.basic.thread.StackTrace;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.error.TopLogicException;

/**
 * Common base class for all {@link ComponentChannel} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractComponentChannel implements ComponentChannel {

	private final LayoutComponent _component;

	private Object _destinations = InlineList.newInlineList();

	private List<ChannelListener> _listeners = new CopyOnWriteArrayList<>();

	private List<ChannelValueFilter> _vetoListeners = new CopyOnWriteArrayList<>();

	private String _name;

	private int _depth;

	private Iterator<ChannelListener> _iteration;

	/**
	 * Creates a {@link AbstractComponentChannel}.
	 * 
	 * @param name
	 *        Channel name for debugging.
	 */
	public AbstractComponentChannel(LayoutComponent component, String name) {
		_component = component;
		_name = name;
	}

	@Override
	public String name() {
		return _name;
	}

	@Override
	public LayoutComponent getComponent() {
		return _component;
	}

	@Override
	public boolean set(Object newValue) {
		newValue = tranformInput(newValue);
		Object oldValue = get();
		if (CollectionUtil.equals(newValue, oldValue)) {
			return false;
		}
		if (!acceptNewValue(oldValue, newValue)) {
			return false;
		}

		_depth++;
		try {
			if (_depth > 1) {
				Logger.warn("Overwriting channel " + this + " = " + newValue + " (was " + oldValue + ").",
					new StackTrace(), AbstractComponentChannel.class);

				if (_depth > 3) {
					List<Object> values = new ArrayList<>();
					values.add(newValue);
					throw new TopLogicException(
						I18NConstants.ERROR_TOO_MUCH_RECURSION__CHANNEL_VALUES.fill(this, values));
				}
			} else {
				if (Logger.isDebugEnabled(AbstractComponentChannel.class)) {
					Logger.debug("Channel " + this + " = " + newValue + ".", AbstractComponentChannel.class);
				}
			}

			try {
				storeValue(newValue, oldValue);
			} catch (TopLogicException ex) {
				ResKey errorKey = ex.getErrorKey();
				if (errorKey.plain() == I18NConstants.ERROR_TOO_MUCH_RECURSION__CHANNEL_VALUES) {
					if (errorKey.arguments()[0] == this) {
						@SuppressWarnings("unchecked")
						List<Object> values = (List<Object>) errorKey.arguments()[1];
						values.add(newValue);
					}
				}
				throw ex;
			}
		} finally {
			_depth--;
			if (_depth == 0) {
				_iteration = null;
			}
		}

		return true;
	}

	/**
	 * Transforms the input value to the value to store.
	 */
	@FrameworkInternal
	protected Object tranformInput(Object newValue) {
		return newValue;
	}

	/**
	 * Stores the given new value that is different from the old value and accepted by all channel
	 * filters.
	 */
	protected abstract void storeValue(Object newValue, Object oldValue);

	/**
	 * Linked {@link com.top_logic.layout.channel.ComponentChannel.ChannelListener}s observing the
	 * current channel.
	 */
	public List<ChannelListener> getListeners() {
		return new ArrayList<>(_listeners);
	}

	@Override
	public void internalLinkBack(ComponentChannel destination) {
		_destinations = InlineList.add(ComponentChannel.class, _destinations, destination);
	}

	@Override
	public void internalUnlinkFrom(ComponentChannel destination) {
		_destinations = InlineList.remove(_destinations, destination);
	}

	@Override
	public final Collection<ComponentChannel> destinations() {
		return InlineList.toList(ComponentChannel.class, _destinations);
	}

	@Override
	public void unlinkAll() {
		for (ComponentChannel destination : destinations()) {
			destination.unlink(this);
		}
		for (ComponentChannel source : sources()) {
			unlink(source);
		}
	}

	@Override
	public boolean addListener(ChannelListener other) {
		if (_listeners.contains(other)) {
			return false;
		}

		return _listeners.add(other);
	}

	@Override
	public boolean removeListener(ChannelListener other) {
		return _listeners.remove(other);
	}

	/**
	 * Whether some {@link com.top_logic.layout.channel.ComponentChannel.ChannelListener}s are
	 * {@link #addListener(com.top_logic.layout.channel.ComponentChannel.ChannelListener)
	 * registered}.
	 */
	protected boolean hasListeners() {
		return !_listeners.isEmpty();
	}

	@Override
	public boolean addVetoListener(ChannelValueFilter other) {
		if (_vetoListeners.contains(other)) {
			return false;
		}

		return _vetoListeners.add(other);
	}

	@Override
	public boolean removeVetoListener(ChannelValueFilter other) {
		return _vetoListeners.remove(other);
	}

	/**
	 * Service method for informing registered listeners about a new {@link #get() model value} of
	 * this {@link ComponentChannel}.
	 * 
	 * @param oldValue
	 *        The old model value.
	 * @param newValue
	 *        The new model value that has been set.
	 * @return Whether there was no veto.
	 */
	protected boolean acceptNewValue(Object oldValue, Object newValue) {
		if (_vetoListeners.isEmpty()) {
			// No need for complex iteration.
			return true;
		}
		for (ChannelValueFilter listener : _vetoListeners) {
			if (!listener.accept(this, oldValue, newValue)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Service method for informing registered listeners about a new {@link #get() model value} of
	 * this {@link ComponentChannel}.
	 * 
	 * @param oldValue
	 *        The old model value.
	 * @param newValue
	 *        The new model value that has been set.
	 */
	protected void notifyNewValue(Object oldValue, Object newValue) {
		if (_listeners.isEmpty()) {
			// No need for complex iteration.
			return;
		}

		// Note: When called recursively, a pending iteration of an outer level is canceled (by
		// replacing it with the current iteration). This ensures that no event waves can occur that
		// result in endless recursion.
		for (_iteration = _listeners.iterator(); _iteration.hasNext();) {
			_iteration.next().handleNewValue(this, oldValue, newValue);
		}
	}

	@Override
	public void resetListeners() {
		_listeners.clear();
		_vetoListeners.clear();
	}

	@Override
	public void destroy() {
		unlinkAll();

		// Do not allow new listeners.
		_listeners = Collections.emptyList();
		_vetoListeners = Collections.emptyList();
	}

	@Override
	public String toString() {
		return _name + "(" + componentName() + ")";
	}

	private String componentName() {
		return _component == null ? "unknown" : _component.getName().localName();
	}

}
