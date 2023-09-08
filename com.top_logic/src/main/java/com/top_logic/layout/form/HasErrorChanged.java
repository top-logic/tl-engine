/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles {@link FormField#hasError() has error} change of a
 * {@link FormField}.
 * 
 * @see FormField#HAS_ERROR_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface HasErrorChanged extends PropertyListener {

	/**
	 * Handles change of the {@link FormField#hasError() has error} of the given {@link FormField}.
	 * 
	 * @param sender
	 *        The field whose error state changed.
	 * @param oldError
	 *        Whether the field had error before.
	 * @param newError
	 *        Whether the field has now an error.
	 * @return Whether this event shall bubble.
	 * 
	 * @see FormField#hasError()
	 */
	Bubble hasErrorChanged(FormField sender, Boolean oldError, Boolean newError);
}

