/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.form.StateHandler;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.listen.ObservedObjects;

/**
 * Default {@link ViewChannel} implementation that holds a mutable value.
 *
 * <p>
 * The {@link #set(Object)} method uses {@link Objects#equals(Object, Object)} to detect changes and
 * only notifies listeners when the value actually changes. A listener whose {@link Registration} is
 * disposed while a notification runs is not called by that notification anymore.
 * </p>
 *
 * <p>
 * A channel is session state and is not synchronized: it is read and written by one thread at a
 * time, the one holding the session's interaction (a request, or a background job delivering its
 * progress under that interaction).
 * </p>
 *
 * <p>
 * Before the value is written, all
 * {@link com.top_logic.layout.view.channel.ViewChannel.VetoListener}s are asked, and the handlers
 * they object with are collected into a single {@link ChannelVetoException}. A handler reachable
 * over several {@link VetoForwarder forwarding} paths is reported once.
 * </p>
 *
 * <p>
 * While {@link #attach(ModelScope) attached}, the channel observes the {@link TLObject}s its value
 * holds. When one of them is deleted, the channel drops it with an ordinary change: a single object
 * is replaced by {@code null}, a collection by a copy without the deleted members (see
 * {@link DeletedObjects#withoutDeleted(Object)}). Every listener hears the deletion as the new
 * value, so no reader is left holding an object that is gone - a reader evaluating an expression
 * over the value, a display showing it, a command deciding over it. A selector writing the channel
 * answers that drop with its next selection.
 * </p>
 *
 * <p>
 * The drop is not offered to the veto listeners: nothing can be saved on an object that is gone, so
 * unsaved changes cannot keep it on the channel.
 * </p>
 *
 * <p>
 * Attaching validates the value: a deletion that happened while the channel was detached - the
 * view declaring it was off screen - is announced then.
 * </p>
 */
public class DefaultViewChannel implements ObservingChannel {

	private final String _name;

	private Object _value;

	private final ChannelListeners _listeners = new ChannelListeners(this);

	private final CopyOnWriteArrayList<VetoListener> _vetoListeners = new CopyOnWriteArrayList<>();

	/**
	 * The objects of the value, observed for their deletion while {@link #_attached}; {@code null}
	 * until the channel is attached for the first time.
	 */
	private ObservedObjects _observed;

	private boolean _attached;

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

		if (!_vetoListeners.isEmpty()) {
			VetoCollector dirtyHandlers = new VetoCollector();
			for (VetoListener vl : _vetoListeners) {
				dirtyHandlers.addAll(vl.checkVeto(this, oldValue, newValue));
			}
			if (!dirtyHandlers.isEmpty()) {
				throw new ChannelVetoException(dirtyHandlers.toList(), () -> this.set(newValue));
			}
		}

		update(newValue);
		return true;
	}

	/**
	 * Begins observing the objects of the value, and drops the ones deleted meanwhile.
	 */
	@Override
	public void attach(ModelScope scope) {
		if (_attached) {
			return;
		}
		_attached = true;
		if (_observed == null) {
			_observed = new ObservedObjects(event -> dropDeleted());
		}
		_observed.observeValue(_value);
		_observed.attach(scope);
		dropDeleted();
	}

	/**
	 * Stops observing the objects of the value.
	 */
	@Override
	public void detach() {
		if (!_attached) {
			return;
		}
		_attached = false;
		_observed.detach();
	}

	/**
	 * Stops observing the objects of the value.
	 */
	@Override
	public void release() {
		detach();
	}

	/**
	 * Replaces the value by the value without its deleted objects, where it holds one.
	 */
	private void dropDeleted() {
		Object valid = DeletedObjects.withoutDeleted(_value);
		if (valid != _value) {
			update(valid);
		}
	}

	/**
	 * Stores the given value, observes its objects and announces it to the listeners.
	 */
	private void update(Object newValue) {
		Object oldValue = _value;
		_value = newValue;
		if (_attached) {
			_observed.observeValue(newValue);
		}
		_listeners.notifyChange(oldValue, newValue);
	}

	@Override
	public Registration addListener(ChannelListener listener) {
		return _listeners.register(listener);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void removeListener(ChannelListener listener) {
		_listeners.removeListener(listener);
	}

	@Override
	public List<StateHandler> dirtyHandlers() {
		if (_vetoListeners.isEmpty()) {
			return List.of();
		}
		VetoCollector dirtyHandlers = new VetoCollector();
		for (VetoListener vl : _vetoListeners) {
			dirtyHandlers.addAll(vl.checkDirty(this));
		}
		return dirtyHandlers.toList();
	}

	@Override
	public void addVetoListener(VetoListener listener) {
		_vetoListeners.add(listener);
	}

	@Override
	public void removeVetoListener(VetoListener listener) {
		_vetoListeners.remove(listener);
	}

	@Override
	public String toString() {
		return "ViewChannel[" + _name + "=" + _value + "]";
	}
}
