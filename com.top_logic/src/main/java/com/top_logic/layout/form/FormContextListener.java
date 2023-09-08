/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.layout.form.model.FormContext;

/**
 * {@link PropertyListener} handling change of {@link FormHandler#getFormContext()} of a
 * {@link FormHandler}.
 * 
 * @see FormHandler#FORM_CONTEXT_EVENT
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface FormContextListener extends PropertyListener {

	/**
	 * Handles change of the {@link FormHandler#getFormContext()} of the given {@link FormHandler}.
	 * 
	 * @param sender
	 *        The {@link FormHandler} whose context changed.
	 * @param oldContext
	 *        Former {@link FormContext}.
	 * @param newFormContext
	 *        Current {@link FormContext}.
	 * @return Whether this event shall bubble.
	 * 
	 * @see FormHandler#getFormContext()
	 */
	Bubble handleFormContextChanged(FormHandler sender, FormContext oldContext, FormContext newFormContext);

}

