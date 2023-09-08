/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.layout.DisplayContext;

/**
 * Element which is invalid and must be validated.
 * 
 * @see ValidationQueue#notifyInvalid(ToBeValidated)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ToBeValidated {

	/**
	 * Validates this object. When the method call returns the implementation must be valid, i.e.
	 * pure access to it must not have any effect to the validity of any third party.
	 * 
	 * @param context
	 *        The context in which the command was executed
	 */
	void validate(DisplayContext context);
}
