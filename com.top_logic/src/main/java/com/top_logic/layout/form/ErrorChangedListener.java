/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles {@link FormField#getError() error} change of a {@link FormField}.
 * 
 * @see FormField#ERROR_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ErrorChangedListener extends PropertyListener {

	/**
	 * Handles change of the {@link FormField#getError() error} of the given {@link FormField}.
	 * 
	 * @param sender
	 *        The field whose error changed.
	 * @param oldError
	 *        The old error.
	 * @param newError
	 *        The new error.
	 * @return Whether this event shall bubble.
	 * 
	 * @see FormField#getError()
	 */
	Bubble handleErrorChanged(FormField sender, String oldError, String newError);

}

