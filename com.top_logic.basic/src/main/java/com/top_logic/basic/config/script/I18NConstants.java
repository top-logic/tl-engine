/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.script;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The interface {0} cannot be implemented by a script: Its abstract method ''{1}'' is
	 *     overloaded. Give the overloaded methods different property names with a @Name
	 *     annotation.
	 */
	public static ResKey2 ERROR_OVERLOADED_SCRIPT_METHOD__INTERFACE_METHOD;

	/**
	 * @en The interface {0} cannot be implemented by a script: Its abstract method ''{1}'' declares
	 *     type parameters.
	 */
	public static ResKey2 ERROR_GENERIC_SCRIPT_METHOD__INTERFACE_METHOD;

	/**
	 * @en The interface {0} cannot be implemented by a script: The methods {2} have the same
	 *     property name ''{1}''.
	 */
	public static ResKey3 ERROR_METHOD_PROPERTY_CLASH__INTERFACE_PROPERTY_METHODS;

	/**
	 * @en The interface {0} cannot be implemented by a script: The property name ''{1}'' of the
	 *     method ''{2}'' is reserved for another property of the script configuration.
	 */
	public static ResKey3 ERROR_RESERVED_METHOD_PROPERTY__INTERFACE_PROPERTY_METHOD;

	static {
		initConstants(I18NConstants.class);
	}

}
