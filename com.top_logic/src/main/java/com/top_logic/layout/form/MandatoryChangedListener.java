/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles {@link FormField#isMandatory() mandatory} change of a
 * {@link FormField}.
 * 
 * @see FormField#MANDATORY_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MandatoryChangedListener extends PropertyListener {

	/**
	 * Handles change of the {@link FormField#isMandatory() mandatory} state of the given
	 * {@link FormField}.
	 * 
	 * @param sender
	 *        The field whose mandatory state changed.
	 * @param oldValue
	 *        Whether the field was mandatory before.
	 * @param newValue
	 *        Whether the field is mandatory now.
	 * @return Whether this event shall bubble.
	 * 
	 * @see FormField#isMandatory()
	 */
	Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue);

}

