/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} handling change of the fixed options of a {@link SelectField}.
 * 
 * @see SelectField#FIXED_FILTER_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface FixedOptionsListener extends PropertyListener {

	/**
	 * Handles change of the {@link SelectField#getFixedOptions()} of the given {@link SelectField}.
	 * 
	 * @param sender
	 *        The {@link SelectField} whose fixed options changed.
	 * @param oldValue
	 *        Former filter matching fixed options.
	 * @param newValue
	 *        Current filter matching fixed options.
	 * @return Whether this event shall bubble.
	 * 
	 * @see SelectField#getFixedOptions()
	 */
	Bubble handleFixedOptionsChanged(SelectField sender, Filter oldValue, Filter newValue);

}

