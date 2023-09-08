/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	static {
		initConstants(I18NConstants.class);
	}

	public static ResKey2 ERROR_INVALID_CONFIGURATION_VALUE__PROPERTY_DETAIL;

	public static ResKey3 ERROR_INVALID_CONFIGURATION_VALUE__PROPERTY_VALUE_DETAIL;

	public static ResKey ERROR_ACCESSING_CLASS_CONSTRUCTOR;

	public static ResKey ERROR_DURING_INSTANTIATION;

	public static ResKey ERROR_ACCESSING_CLASS;

	public static ResKey ERROR_WHILE_INSTANTIATING_CLASS;

	public static ResKey ERROR_SINGLETON_LOOKUP_FAILED;

	public static ResKey1 ERROR_ACCESS_DENIED__TARGET;

	public static ResKey ERROR_NOT_A_SINGLETON_CLASS;

	public static ResKey ERROR_BOOLEAN_EXPECTED;

	public static ResKey ERROR_CHARACTER_EXPECTED;

	public static ResKey ERROR_SHORT_EXPECTED;

	public static ResKey ERROR_BYTE_EXPECTED;

	public static ResKey ERROR_FLOAT_EXPECTED;

	public static ResKey ERROR_LONG_EXPECTED;

	public static ResKey ERROR_INTEGER_EXPECTED;

	public static ResKey ERROR_DOUBLE_VALUE_EXPECTED;

	public static ResKey2 ERROR_INVALID_VALUE__VALUE_OPTIONS;

	public static ResKey1 ERROR_FACTORY_METHOD_PROVIDED_NULL__NAME;

	public static ResKey2 ERROR_FACTORY_METHOD_PROVIDED_INCOMPATIBLE_VALUE__NAME_TYPE;

	public static ResKey1 ERROR_CLASS_DOES_NOT_EXIST__NAME;

	public static ResKey2 ERROR_SINGLETON_REFERENCE_HAS_INCOMPATIBLE_TYPE__FIELD_TYPE;

	public static ResKey2 ERROR_CONFIGURED_CLASS_IS_NOT_SUBLASS_OF_EXPECTED_TYPE__CLASS_TYPE;

	public static ResKey2 ERROR_SIGNATURE_MISSMATCH;

	public static ResKey2 ERROR_NO_SUCH_ENUM_CONSTANT__ENUM_INDEX;

	public static ResKey1 ERROR_SINGLETON_REFERENCE_IS_NULL__FIELD;

	public static ResKey PROPERTY_IS_MANDATORY_BUT_VALUE_WAS_EMPTY;

	public static ResKey2 ERROR_INSTANTIATION_FAILED__CLASS_LOCATION;

	public static ResKey3 ERROR_INVALID_FORMAT__VALUE_PROPERTY_DETAIL;

	public static ResKey2 ERROR_INVALID_DEFAULT_VALUE_FOR_TYPE__TYPE___VALUE;

}
