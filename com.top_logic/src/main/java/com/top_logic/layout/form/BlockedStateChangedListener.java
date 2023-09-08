/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles change of the {@link FormField#isBlocked()} state of a {@link FormField}.
 * 
 * @see FormField#BLOCKED_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface BlockedStateChangedListener extends PropertyListener {

	/**
	 * Handles change of the {@link FormField#isBlocked()} state of the given field.
	 * 
	 * @param sender
	 *        {@link FormField} whose blocked state changed.
	 * @param oldValue
	 *        Whether the field was blocked before.
	 * @param newValue
	 *        Whether the field is now blocked.
	 * @return Whether this event shall bubble.
	 * 
	 * @see FormField#isBlocked()
	 */
	Bubble handleIsBlockedChanged(FormField sender, Boolean oldValue, Boolean newValue);

}

