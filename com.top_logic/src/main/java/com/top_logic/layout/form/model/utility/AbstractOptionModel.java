/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base class for implementing {@link OptionModel}s.
 */
public abstract class AbstractOptionModel<T> implements OptionModel<T> {

	private List<OptionModelListener> _observers = new CopyOnWriteArrayList<>();

	/**
	 * Notifies all observers about a change.
	 */
	protected void notifyChanged() {
		for (OptionModelListener l : _observers) {
			l.notifyOptionsChanged(this);
		}
	}

	@Override
	public boolean addOptionListener(OptionModelListener listener) {
		if (_observers.contains(listener)) {
			return false;
		}
		return _observers.add(listener);
	}

	@Override
	public boolean removeOptionListener(OptionModelListener listener) {
		return _observers.remove(listener);
	}

}
