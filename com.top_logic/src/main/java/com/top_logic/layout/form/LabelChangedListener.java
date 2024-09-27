/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.util.ResKey;

/**
 * Handles change of the label of a given object.
 * 
 * @see FormMember#LABEL_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface LabelChangedListener extends PropertyListener {

	/**
	 * Handles change of the label of the given object.
	 * 
	 * @param sender
	 *        The object whose label changed.
	 * @param oldLabel
	 *        The former label.
	 * @param newLabel
	 *        The current label.
	 * @return Whether this event shall bubble.
	 */
	Bubble handleLabelChanged(Object sender, ResKey oldLabel, ResKey newLabel);

}

