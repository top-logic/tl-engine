/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.layout.DisplayContext;

/**
 * Observer for {@link LayoutComponent} validation events.
 * 
 * @see LayoutComponent#addValidationListener(ValidationListener)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ValidationListener {

	/**
	 * Validates the model of the given component.
	 * 
	 * @param context
	 *        The context in which the command was executed
	 * @param component
	 *        The {@link LayoutComponent} being validated.
	 * 
	 * @see LayoutComponent#doValidateModel(DisplayContext)
	 */
	void doValidateModel(DisplayContext context, LayoutComponent component);

}
