/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change of the raw value of a {@link FormField}.
 * 
 * @see FormField#VALUE_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface RawValueListener extends PropertyListener {

	/**
	 * Handles change of the {@link FormField#getRawValue()} of the given field.
	 * 
	 * @param field
	 *        {@link FormField} whose raw value changed.
	 * @param oldValue
	 *        Former raw value.
	 * @param newValue
	 *        Current raw value.
	 * @return Whether this event shall bubble.
	 */
	Bubble handleRawValueChanged(FormField field, Object oldValue, Object newValue);

}

