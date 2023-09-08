/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-cast {@link ConfigurationListener}.
 * <p>
 * The {@link ConfigurationListener} list is updated with lazy copy-on-write.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class MultiCast implements ConfigurationListener {

	private boolean iterating = false;

	private List<ConfigurationListener> _listeners;

	/** Creates a {@link MultiCast} with the given list of {@link ConfigurationListener}s. */
	public MultiCast(ConfigurationListener... listeners) {
		_listeners = new ArrayList<>(listeners.length);
		for (ConfigurationListener listener : listeners) {
			add(listener); // Prevents duplicates
		}
	}

	public int size() {
		return _listeners.size();
	}

	public ConfigurationListener get(int index) {
		return _listeners.get(index);
	}

	public boolean add(ConfigurationListener listener) {
		if (_listeners.contains(listener)) {
			// Not added again.
			return false;
		} else {
			if (isIterating()) {
				// Copy on write.
				ArrayList<ConfigurationListener> newListeners =
					new ArrayList<>(_listeners.size() + 1);
				newListeners.addAll(_listeners);
				newListeners.add(listener);
				_listeners = newListeners;
				// No need to copy list until the new _listeners are processed.
				resetIterating();
			} else {
				_listeners.add(listener);
			}
			return true;
		}
	}

	public boolean remove(ConfigurationListener listener) {
		if (isIterating()) {
			if (_listeners.contains(listener)) {
				// Copy on write.
				ArrayList<ConfigurationListener> newListeners =
					new ArrayList<>(_listeners.size() - 1);
				for (ConfigurationListener other : _listeners) {
					if (other == listener) {
						continue;
					}
					newListeners.add(other);
				}
				_listeners = newListeners;
				// No need to copy list until the new _listeners are processed.
				resetIterating();
				return true;
			} else {
				return false;
			}
		} else {
			return _listeners.remove(listener);
		}
	}

	@Override
	public void onChange(ConfigurationChange change) {
		boolean before = beginIterating();
		try {
			for (ConfigurationListener listener : _listeners) {
				listener.onChange(change);
			}
		} finally {
			// Note: Iterating may have been reset due to a modification. Only copy once, even
			// for multiple modification in the same notification loop.
			if (isIterating()) {
				endIterating(before);
			}
		}
	}

	private boolean beginIterating() {
		boolean before = isIterating();
		setIterating(true);
		return before;
	}

	private void endIterating(boolean before) {
		setIterating(before);
	}

	private void resetIterating() {
		setIterating(false);
	}

	private boolean isIterating() {
		return iterating;
	}

	private void setIterating(boolean iterating) {
		this.iterating = iterating;
	}

}
