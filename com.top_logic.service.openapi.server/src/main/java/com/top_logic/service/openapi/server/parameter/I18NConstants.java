/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The referenced parameter "{0}" does not exist.
	 */
	public static ResKey1 UNDEFINED_PARAMETER_REFERENCE__REFERENCE;

	/**
	 * @en The variable name "{0}" is not a valid TL-Script variable name. A variable name starts
	 *     with a letter or an underscore and contains only letters, digits, and underscores.
	 */
	public static ResKey1 ERROR_INVALID_VARIABLE_NAME__NAME;

	/**
	 * @en A variable name is required, because the parameter name "{0}" is not a valid TL-Script
	 *     variable name.
	 */
	public static ResKey1 ERROR_VARIABLE_NAME_REQUIRED__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
