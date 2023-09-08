/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change of the {@link FormMember#isTransient() transient
 * state} of a {@link FormMember}.
 * 
 * @see FormMember#TRANSIENT_STATE_CHANGED
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TransientStateListener extends PropertyListener {

	/**
	 * Handles change of the {@link FormMember#isTransient() transient state} of the given
	 * {@link FormMember}.
	 * 
	 * @param sender
	 *        The {@link FormMember} whose transient state changed.
	 * @param oldValue
	 *        The old state.
	 * @param newValue
	 *        The new state.
	 * @return Whether this event shall bubble.
	 * 
	 * @see FormMember#isTransient()
	 */
	Bubble handleTransientStateChanged(FormMember sender, Boolean oldValue, Boolean newValue);

}

