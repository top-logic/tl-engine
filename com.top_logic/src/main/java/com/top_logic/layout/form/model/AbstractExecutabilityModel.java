/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.listener.EventType;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Base class for {@link ExecutabilityModel} implementations.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractExecutabilityModel extends ExecutabilityObservable {

	/** The calculated {@link ExecutableState}. */
	private ExecutableState _executability;

    @Override
	public ExecutableState getExecutability() {
		if (_executability == null) {
			updateExecutabilityState();
        }

		return _executability;
    }

	@Override
	protected void firstListenerAdded(EventType<?, ?, ?> type) {
		super.firstListenerAdded(type);

		// Bring the cached state in sync.
		updateExecutabilityState();
	}

    @Override
	public final void updateExecutabilityState() {
		final ExecutableState oldValue = _executability;
		ExecutableState newValue = calculateExecutability();
		_executability = newValue;

		notifyExecutabilityChange(oldValue, newValue);
    }

	/**
	 * Calculate the new {@link ExecutableState} for this model.
	 */
	protected abstract ExecutableState calculateExecutability();

}
