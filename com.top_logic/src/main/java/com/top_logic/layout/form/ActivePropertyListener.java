/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles {@link FormMember#isActive() immutable state} change of a {@link FormMember}.
 * 
 * @see FormMember#ACTIVE_PROPERTY
 */
public interface ActivePropertyListener extends PropertyListener {

	/**
	 * Handles change of the {@link FormMember#isActive() active state} of the given
	 * {@link FormMember}.
	 * 
	 * @param sender
	 *        The object whose immutable state changed.
	 * @param oldValue
	 *        The old state.
	 * @param newValue
	 *        The new state.
	 * @return Whether this event shall bubble.
	 * 
	 * @see FormMember#isActive()
	 */
	Bubble handleActiveChanged(FormMember sender, Boolean oldValue, Boolean newValue);
}

