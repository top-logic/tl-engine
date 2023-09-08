/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;


import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change of the {@link SelectField#getOptions()} in a
 * {@link SelectField}.
 * 
 * @see SelectField#OPTIONS_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface OptionsListener extends PropertyListener {

	/**
	 * Handles change of the options of the given {@link SelectField}.
	 * 
	 * @param sender
	 *        {@link SelectField} whose options changed.
	 * @return Whether this event shall bubble.
	 * 
	 * @see SelectField#isOptionsList()
	 * @see SelectField#isOptionsTree()
	 */
	Bubble handleOptionsChanged(SelectField sender);

}

