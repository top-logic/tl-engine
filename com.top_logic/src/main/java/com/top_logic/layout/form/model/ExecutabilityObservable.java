/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Common listener handling in {@link ExecutabilityModel}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ExecutabilityObservable extends PropertyObservableBase implements ExecutabilityModel {

	@Override
	public void addExecutabilityListener(ExecutabilityListener listener) {
		addListener(EXECUTABILITY, listener);
	}

	@Override
	public void removeExecutabilityListener(ExecutabilityListener listener) {
		removeListener(EXECUTABILITY, listener);
	}

	/**
	 * Informs about a changed executability, if there is a visible change.
	 * 
	 * @param oldExecutability
	 *        The executability before.
	 * @param newExecutability
	 *        The new executability.
	 */
	protected final void notifyExecutabilityChange(ExecutableState oldExecutability, ExecutableState newExecutability) {
		if (hasListeners() && needsEvent(oldExecutability, newExecutability)) {
			notifyListeners(EXECUTABILITY, self(), oldExecutability, newExecutability);
		}
	}

	/**
	 * The sender used in {@link ExecutabilityModel#EXECUTABILITY} events.
	 */
	protected ExecutabilityModel self() {
		return this;
	}

	private static boolean needsEvent(ExecutableState oldExecutability, ExecutableState newExecutability) {
		if (oldExecutability == null) {
			return true;
		}
		if (oldExecutability.isVisible() != newExecutability.isVisible()) {
			return true;
		}
		if (oldExecutability.isExecutable() != newExecutability.isExecutable()) {
			return true;
		}
		if (!oldExecutability.isExecutable()) {
			return !oldExecutability.getI18NReasonKey().equals(newExecutability.getI18NReasonKey());
		}
		return false;
	}

}
