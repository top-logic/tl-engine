/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles change of the CSS class of an object.
 * 
 * @see FormMember#CLASS_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CSSClassListener extends PropertyListener {

	/**
	 * Handles change of the CSS class of the given object.
	 * 
	 * @param sender
	 *        The object whose CSS class changed.
	 * @param oldValue
	 *        The old class.
	 * @param newValue
	 *        The new class.
	 * @return Whether this event shall bubble.
	 */
	Bubble handleCSSClassChange(Object sender, String oldValue, String newValue);

}

