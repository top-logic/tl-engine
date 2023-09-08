/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles change of the tooltip of a given object.
 * 
 * @see FormMember#TOOLTIP_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TooltipChangedListener extends PropertyListener {

	/**
	 * Handles change of the tooltip of the given object.
	 * 
	 * @param sender
	 *        The object whose tooltip changed.
	 * @param oldValue
	 *        The former tooltip.
	 * @param newValue
	 *        The current tooltip.
	 * @return Whether this event shall bubble.
	 */
	Bubble handleTooltipChanged(Object sender, String oldValue, String newValue);

}

