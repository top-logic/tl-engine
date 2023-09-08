/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.listener;

import com.top_logic.basic.listener.EventType.Bubble;

/**
 * Base class for {@link PropertyObservable} proxy implementations.
 * 
 * <p>
 * A proxy implementation for an observable interface <code>S</code> can be implemented as subclass
 * of {@link PropertyObservableProxy}. In that case, listeners with {@link EventType}s for the
 * <code>S</code> interface can be registered at the proxy implementation. Those listeners in turn
 * receive events from the proxy instance instead of the underlying implementation detail instance.
 * </p>
 * 
 * @see #impl()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class PropertyObservableProxy implements PropertyObservable {

	@Override
	public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, final L listener) {
		return impl().addListener(GLOBAL_LISTENER_TYPE, new ListenerProxy<>(type, listener));
	}

	@Override
	public <L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, final L listener) {
		return impl().removeListener(GLOBAL_LISTENER_TYPE, new ListenerProxy<>(type, listener));
	}

	/**
	 * The underlying delegate.
	 */
	protected abstract PropertyObservable impl();

	private final class ListenerProxy<L extends PropertyListener> implements GenericPropertyListener {
	
		private final EventType<L, ?, ?> _type;

		private final L _listener;

		private final boolean _concrete;
	
		public ListenerProxy(EventType<L, ?, ?> type, L listener) {
			_type = type;
			_listener = listener;

			_concrete = type != PropertyObservable.GLOBAL_LISTENER_TYPE;
		}
	
		@Override
		public Bubble handlePropertyChanged(EventType<?, ?, ?> type, Object sender, Object oldValue, Object newValue) {
			if (_concrete && type != _type) {
				// Filter events that are dispatched to this listener due to the fact that observing
				// the delegate can only be done through the generic event key that receives all
				// events, even those for which the client has not registered.
				return Bubble.BUBBLE;
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			Bubble result =
				((EventType) _type).delegate(type, _listener, PropertyObservableProxy.this, oldValue, newValue);
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((_listener == null) ? 0 : _listener.hashCode());
			result = prime * result + ((_type == null) ? 0 : _type.hashCode());
			return result;
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ListenerProxy<?> other = (ListenerProxy<?>) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (_listener == null) {
				if (other._listener != null)
					return false;
			} else if (!_listener.equals(other._listener))
				return false;
			if (_type == null) {
				if (other._type != null)
					return false;
			} else if (!_type.equals(other._type))
				return false;
			return true;
		}
	
		private PropertyObservableProxy getOuterType() {
			return PropertyObservableProxy.this;
		}
	
	}

}
