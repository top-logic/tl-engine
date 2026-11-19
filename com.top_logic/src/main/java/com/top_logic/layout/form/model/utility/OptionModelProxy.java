/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link OptionModel} implementation delegating calls to another {@link OptionModel}.
 *
 * <p>
 * A change of the {@link #delegate()} is reported to the listeners of this proxy with the proxy as
 * sender, so that a listener observing this proxy receives events from the model it observes.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class OptionModelProxy<T> implements OptionModel<T> {

	private final List<OptionModelListener> _listeners = new CopyOnWriteArrayList<>();

	private final OptionModelListener _forwarder = sender -> notifyListeners();

	@Override
	public Iterator<T> iterator() {
		return delegate().iterator();
	}

	@Override
	public Object getBaseModel() {
		return delegate().getBaseModel();
	}

	@Override
	public int getOptionCount() {
		return delegate().getOptionCount();
	}

	@Override
	public boolean addOptionListener(OptionModelListener listener) {
		if (_listeners.contains(listener)) {
			return false;
		}
		if (_listeners.isEmpty()) {
			delegate().addOptionListener(_forwarder);
		}
		return _listeners.add(listener);
	}

	@Override
	public boolean removeOptionListener(OptionModelListener listener) {
		boolean removed = _listeners.remove(listener);
		if (removed && _listeners.isEmpty()) {
			delegate().removeOptionListener(_forwarder);
		}
		return removed;
	}

	/**
	 * Notifies the listeners of this proxy about a change of its options.
	 */
	private void notifyListeners() {
		for (OptionModelListener listener : _listeners) {
			listener.notifyOptionsChanged(this);
		}
	}

	@Override
	public void resetBaseModel() {
		delegate().resetBaseModel();
	}

	/**
	 * The {@link OptionModel} to delegate methods to. Repeated calls of this method delivers the
	 * same {@link OptionModel} instance.
	 * 
	 * @return Must not be <code>null</code>.
	 */
	public abstract OptionModel<T> delegate();

}

