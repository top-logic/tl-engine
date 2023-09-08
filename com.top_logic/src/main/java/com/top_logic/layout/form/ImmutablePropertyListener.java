/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles {@link FormMember#isImmutable() immutable state} change of a {@link FormMember}.
 * 
 * @see FormMember#IMMUTABLE_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ImmutablePropertyListener extends PropertyListener {

	/**
	 * Handles change of the {@link FormMember#isImmutable() immutable state} of the given
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
	 * @see FormMember#isImmutable()
	 */
	Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue);
}

