/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Listener to handle changes of the alternative text of an object.
 * 
 * @see ButtonUIModel#ALT_TEXT_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AltTextChangedListener extends PropertyListener {

	/**
	 * Handles the change of an alternative text.
	 * 
	 * @param sender
	 *        The model whose alt text had changed.
	 * @param oldValue
	 *        Old alternative text.
	 * @param newValue
	 *        New alternative text.
	 * @return Whether this event shall bubble.
	 */
	Bubble handleAltTextChanged(Object sender, String oldValue, String newValue);

}

