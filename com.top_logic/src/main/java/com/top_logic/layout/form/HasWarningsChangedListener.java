/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles {@link FormField#hasWarnings() has warnings} state of a {@link FormField}.
 * 
 * @see FormField#HAS_WARNINGS_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface HasWarningsChangedListener extends PropertyListener {

	/**
	 * Handles change of the {@link FormField#hasWarnings() has warnings} state of the given
	 * {@link FormField}.
	 * 
	 * @param field
	 *        The field whose "has warnings" state changed.
	 * @param oldWarnings
	 *        Whether the field had warnings before.
	 * @param newWarnings
	 *        Whether the field has warnings now.
	 * @return Whether this event shall bubble.
	 * 
	 * @see FormField#hasWarnings()
	 */
	Bubble hasWarningsChanged(FormField field, Boolean oldWarnings, Boolean newWarnings);

}

