/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en No such part "{0}" in type "{1}" at {2}.
	 */
	public static ResKey3 NO_SUCH_PART__ATTR_TYPE_LOC;

	/**
	 * @en "Failed to resolve value "{0}" at {1}: {2}
	 */
	public static ResKey3 FAILED_TO_RESOLVE_VALUE__VAL_LOC_MSG;

	/**
	 * @en Failed to set value "{0}" to attribute "{1}" at {2}: {3}
	 */
	public static ResKey4 FAILED_SETTING_VALUE__VAL_ATTR_LOC_MSG;

	/**
	 * @en Reference attribute "{0}" must not be assigned plain value "{1}" at {2}.
	 */
	public static ResKey3 CANNOT_ASSIGN_PLAIN_VALUE_TO_REF__ATTR_VAL_LOC;

	/**
	 * @en Multiple values cannot be stored into singleton reference "{0}" at {1}.
	 */
	public static ResKey2 ERROR_STORING_COLLECTION_TO_SINGLE_REF__ATTR_LOC;

	/**
	 * @en Unresolved reference "{0}" at {1}.
	 */
	public static ResKey2 UNRESOLVED_REFERENCE__ID_LOC;

	/**
	 * @en Failed to import object with ID "{0}" of type "{1}": {2}.
	 */
	public static ResKey3 FAILED_TO_IMPORT__ID_TYPE_MSG;

	/**
	 * @en Failed to resolve object of type "{0}" with ID "{1}".
	 */
	public static ResKey2 FAILED_TO_RESOLVE_OBJECT__TYPE_ID;

	/**
	 * @en Failed to resolve object of type "{0}" with ID "{1}": {2}
	 */
	public static ResKey3 FAILED_TO_RESOLVE_OBJECT__TYPE_ID_MSG;

	/**
	 * @en Primitive type "{0}" cannot have part "{1}" at {2}.
	 */
	public static ResKey3 PRIMITIVE_TYPE_WITH_PART__TYPE_PART_LOC;

	/**
	 * @en Enum "{0}" has no classifier "{1}" at {2}.
	 */
	public static ResKey3 NO_SUCH_CLASSIFIER__TYPE_PART_LOC;

	/**
	 * @en Type "{0}" has no part "{1}" at {2}.
	 */
	public static ResKey3 NO_SUCH_PART__TYPE_PART_LOC;

	/**
	 * @en Unknown type kind "{0}" of type "{1}" at {2}.
	 */
	public static ResKey3 UNKNOWN_TYPE__KIND_TYPE_LOC;

	/**
	 * @en No singleton "{0}" defined in module "{1}" at {2}.
	 */
	public static ResKey3 NO_SUCH_SINGLETON__NAME_MODULE_LOC;

	/**
	 * @en Module "{0}" not found in "{1}" at {2}.
	 */
	public static ResKey3 NO_SUCH_MODULE__NAME_REF_LOC;

	/**
	 * @en Type "{0}" not found in "{1}" at {2}.
	 */
	public static ResKey3 NO_SUCH_TYPE__NAME_REF_LOC;

	/**
	 * @en Invalid binary format in "{0}": {1}
	 */
	public static ResKey2 INVALID_BINARY_FORMAT__VAL_MSG;

	/**
	 * @en Invalid date format in "{0}": {1}
	 */
	public static ResKey2 INVALID_DATE_FORMAT__VAL_MSG;

	/**
	 * @en Invalid number format in "{0}": {1}
	 */
	public static ResKey2 INVALID_NUMBER_FORMAT__VAL_MSG;

	/**
	 * @en Invalid ID format in "{0}": {1}
	 */
	public static ResKey2 INVALID_ID_FORMAT__VAL_MSG;

	static {
		initConstants(I18NConstants.class);
	}
}
