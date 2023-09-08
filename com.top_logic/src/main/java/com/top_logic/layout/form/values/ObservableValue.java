/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import java.util.ArrayList;

/**
 * {@link AbstractValue} that manages its own listener list.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ObservableValue<T> extends AbstractValue<T> {

	private ArrayList<Listener> _listeners = new ArrayList<>();

	/**
	 * Notifies the listeners about a change.
	 */
	protected void handleChanged() {
		for (Listener listener : _listeners) {
			listener.handleChange(this);
		}
	}

	@Override
	public ListenerBinding addListener(Listener listener) {
		if (_listeners.isEmpty()) {
			startObserving();
		}
		_listeners.add(listener);

		return () -> {
			_listeners.remove(listener);
			if (_listeners.isEmpty()) {
				stopObserving();
			}
		};
	}

	protected void startObserving() {
		// hook for subclasses.
	}

	protected void stopObserving() {
		// hook for subclasses.
	}

}
