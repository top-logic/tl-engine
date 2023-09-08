/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles change of the {@link FormMember#isChanged() changed state} of a {@link FormMember}.
 * 
 * @see FormMember#IS_CHANGED_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ChangeStateListener extends PropertyListener {

	/**
	 * Handles change of the {@link FormMember#isChanged() change state} of the given
	 * {@link FormMember}.
	 * 
	 * @param sender
	 *        The {@link FormMember} whose change state changed.
	 * @param oldValue
	 *        The old state.
	 * @param newValue
	 *        The new state.
	 * @return Whether this event shall bubble.
	 * 
	 * @see FormMember#isChanged()
	 */
	Bubble handleChangeStateChanged(FormMember sender, Boolean oldValue, Boolean newValue);

}

