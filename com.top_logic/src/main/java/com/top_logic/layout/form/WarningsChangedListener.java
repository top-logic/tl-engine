/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import java.util.List;

import com.top_logic.basic.listener.EventType.Bubble;

/**
 * Handles {@link FormField#getWarnings() warnings} chnage of a {@link FormField}.
 * 
 * @see FormField#WARNINGS_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WarningsChangedListener extends HasWarningsChangedListener {

	/**
	 * Handles change of the {@link FormField#getWarnings() warnings} of the given {@link FormField}
	 * .
	 * 
	 * @param field
	 *        The field whose warnings changed.
	 * @param oldWarnings
	 *        Former list of warnings.
	 * @param newWarnings
	 *        Current list of warnings.
	 * @return Whether this event shall bubble.
	 * 
	 * @see FormField#getWarnings()
	 */
	Bubble warningsChanged(FormField field, List<String> oldWarnings, List<String> newWarnings);

}

