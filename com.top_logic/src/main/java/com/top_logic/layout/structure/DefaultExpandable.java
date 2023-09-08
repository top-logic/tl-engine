/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.listener.PropertyObservableBase;

/**
 * Trivial default implementation of {@link Expandable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultExpandable extends PropertyObservableBase implements Expandable {

	private ExpansionState _state = ExpansionState.NORMALIZED;

	/**
	 * The current display {@link Expandable.ExpansionState}.
	 * 
	 * <p>
	 * This property can be observed using the {@link #STATE} property key.
	 * </p>
	 */
	@Override
	public ExpansionState getExpansionState() {
		return _state;
	}

	/**
	 * @see #getExpansionState()
	 */
	@Override
	public void setExpansionState(ExpansionState state) {
		ExpansionState before = _state;
		if (state == before) {
			return;
		}

		_state = state;

		notifyListeners(STATE, this, before, _state);
	}

}
