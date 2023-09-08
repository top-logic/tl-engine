/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.DisplayContext;

/**
 * Collection of {@link ValidationListener}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class ValidationListeners implements ValidationListener {

	private List<ValidationListener> _listeners = new ArrayList<>();

	private boolean _iterating;

	@Override
	public void doValidateModel(DisplayContext context, LayoutComponent component) {
		boolean started = !_iterating;

		_iterating = true;
		try {
			for (ValidationListener listener : _listeners) {
				listener.doValidateModel(context, component);
			}
		} finally {
			if (started) {
				_iterating = false;
			}
		}
	}

	/**
	 * Adds a new {@link ValidationListener} to this collection.
	 * 
	 * @param listener
	 *        The new {@link ValidationListener}.
	 * @return Whether the given listener was not part of this collection before.
	 */
	public boolean addListener(ValidationListener listener) {
		int index = _listeners.indexOf(listener);
		if (index >= 0) {
			return false;
		}
		copyWhenIterating();
		_listeners.add(listener);
		return true;
	}

	/**
	 * Removes a {@link ValidationListener} from this collection.
	 * 
	 * @param listener
	 *        The {@link ValidationListener} to remove.
	 * @return Whether the given listener was part of this collection before.
	 */
	public boolean removeListener(ValidationListener listener) {
		int index = _listeners.indexOf(listener);
		if (index < 0) {
			return false;
		}
		copyWhenIterating();
		_listeners.remove(index);
		return true;
	}

	private void copyWhenIterating() {
		if (_iterating) {
			_listeners = new ArrayList<>(_listeners);
			_iterating = false;
		}
	}

}
