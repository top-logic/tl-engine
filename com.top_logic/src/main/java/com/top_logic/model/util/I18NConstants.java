/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 ERROR_INVALID_PART_REFERENCE__VALUE;

	public static ResKey2 ERROR_NOT_A_CLASS__VALUE_ACTUAL;

	/**
	 * @en No singleton with name "{1}" found in module "{0}".
	 */
	public static ResKey2 ERROR_NO_SUCH_SINGLETON__MODULE_NAME;

	/**
	 * @en No module with name "{0}" found.
	 */
	public static ResKey1 ERROR_NO_SUCH_MODULE__NAME;

	/**
	 * @en Not a qualified part name: "{0}"
	 */
	public static ResKey1 ERROR_NOT_A_QUALIFIED_NAME__NAME;

	/**
	 * @en No such type "{1}" defined in module "{0}".
	 */
	public static ResKey2 ERROR_NO_SUCH_TYPE__MODULE_NAME;

	/**
	 * @en No such type "{2}" (from module "{1}") found in scope "{0}".
	 */
	public static ResKey3 ERROR_NO_SUCH_TYPE__SCOPE_MODULE_NAME;

	/**
	 * @en Not a structured type "{0}".
	 */
	public static ResKey1 ERROR_NOT_A_STRUCTURED_TYPE__TYPE;

	/**
	 * @en No such part "{1}" in type "{0}".
	 */
	public static ResKey2 ERROR_NO_SUCH_PART_IN_TYPE__TYPE_NAME;

	/**
	 * @en Invalid scope reference: "{0}"
	 */
	public static ResKey1 ERROR_INVALID_SCOPE_REFERENCE__VALUE;

	/**
	 * @en Table "{1}" referenced in scope "{0}" not found.
	 */
	public static ResKey2 ERROR_NO_SUCH_TABLE__SCOPE_TABLE;

	/**
	 * @en Scope object with ID "{1}" does not exist, cannot resolve type scope "{0}".
	 */
	public static ResKey2 ERROR_NO_SUCH_SCOPE_OBJECT__SCOPE_ID;

	/**
	 * @en Object "{1}" is not a type scope (referenced by "{0}").
	 */
	public static ResKey2 ERROR_NOT_A_SCOPE__SCOPE_OBJ;

	/**
	 * @en Singleton "{1}" in module "{0}" is not a type scope.
	 */
	public static ResKey2 ERROR_NOT_A_TYPE_SCOPE__MODULE_SINGLETON;

	/**
	 * @en Type without module: "{0}"
	 */
	public static ResKey1 ERROR_UNQUALIFIED_TYPE_NAME_WITHOUT_SCOPE__NAME;

	/**
	 * @en Invalid legacy type format "{1}", expected: {0}
	 */
	public static ResKey2 ERROR_INVALID_LEGACY_TYPE_FORMAT__EXPECTED_ACTUAL;

	/**
	 * @en No type "{0}" found in any module.
	 */
	public static ResKey1 ERROR_NO_SUCH_GLOBAL_TYPE__NAME;

	/**
	 * @en Type name must not be empty.
	 */
	public static ResKey ERROR_TYPE_NAME_MUST_NOT_BE_EMPTY;

	/**
	 * @en The value {2} for attribute {1} in object {0} produces a cycle: {3}
	 */
	public static ResKey4 ERROR_CYLCE_NOT_ALLOWED__OBJECT_ATTRIBUTE_VALUE_CYCLE;

	static {
		initConstants(I18NConstants.class);
	}
}
