/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.basic.listener.PropertyObservableBase;

/**
 * Adapter for a {@link LayoutModel}.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class LayoutModelAdapter extends PropertyObservableBase implements LayoutModel {

	/**
	 * Listener attached to the actual implementation to receive events from it, modify it, and
	 * forward it to listener attached to this model
	 */
	protected final GenericPropertyListener _adapterListener = new GenericPropertyListener() {

		@Override
		public Bubble handlePropertyChanged(EventType<?, ?, ?> type, Object sender, Object oldValue, Object newValue) {
			return LayoutModelAdapter.this.internalForwardEvent(type, oldValue, newValue);
		}
	};

	/**
	 * Returns the actual {@link LayoutModel} implementation to dispatch to.
	 * 
	 * @return not <code>null</code>
	 */
	protected abstract LayoutModel getLayoutModelImplementation();

	@Override
	public LayoutData getLayoutData() {
		return getLayoutModelImplementation().getLayoutData();
	}

	@Override
	public void setLayoutData(LayoutData newValue, boolean layoutSizeUpdate) {
		getLayoutModelImplementation().setLayoutData(newValue, false);
	}

	@Override
	public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener) {
		if (!hasListeners()) {
			getLayoutModelImplementation().addListener(PropertyObservable.GLOBAL_LISTENER_TYPE, _adapterListener);
		}
		return super.addListener(type, listener);
	}

	@Override
	public <L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, L listener) {
		boolean result = super.removeListener(type, listener);
		if (!hasListeners()) {
			getLayoutModelImplementation().removeListener(PropertyObservable.GLOBAL_LISTENER_TYPE, _adapterListener);
		}
		return result;
	}

	/**
	 * Forwards the event the listener attached to this model
	 * 
	 * <p>
	 * It is expected that event that occurs on the implementation may also be send by this adapter.
	 * </p>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	final Bubble internalForwardEvent(EventType type, Object oldValue, Object newValue) {
		return notifyListeners(type, this, oldValue, newValue);
	}

}

