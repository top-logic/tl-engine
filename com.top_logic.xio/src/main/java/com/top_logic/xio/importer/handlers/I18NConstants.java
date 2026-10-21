/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

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

	public static ResKey3 IMPORT_SPEC__FILE_LINE_COL;

	public static ResKey3 ERROR_UNEXPECTED_ATTRIBUTE_VALUE__HANDLER_GIVEN_EXPECTED;

	public static ResKey1 ERROR_EXPECTED_END_OF_ELEMENT__HANDLER;

	public static ResKey3 ERROR_PROPERTY_FORMAT__HANDLER_VALUE_MESSAGE;

	public static ResKey2 ERROR_ID_FORMAT__HANDLER_MESSAGE;

	public static ResKey2 ERROR_DUPLICATE_ASSIGNMENT_TO_VARIABLE__HANDLER_VAR;

	public static ResKey2 ERROR_FORWARD_VARIABLE_ALREADY_ASSIGNED__HANDLER_VAR;

	public static ResKey2 ERROR_MANDATORY_ID_ATTRIBUTES_NOT_SET__HANDLER_ATTR;

	public static ResKey1 ERROR_END_ELEMENT_EXPECTED__TAG; // Expected to have consumed all data up to the matching end element of ''{0}''.

	public static ResKey2 ERROR_UNEXPECTED_END_TAG__EXPECTED_FOUND; // Expected to have consumed all data up to the matching end element of ''{0}'', found: {1}

	/**
	 * @en Warning: Ignoring unknown tag "{0}".
	 */
	public static ResKey1 WARN_IGNORING_UNKNOWN__TAG;

	/**
	 * @en Unknown element "{0}" skipped.
	 */
	public static ResKey1 WARN_UNKNOWN_ELEMENT__TAG;

	/**
	 * @en Unknown element "{0}".
	 */
	public static ResKey1 ERROR_UNKNOWN_ELEMENT__TAG;

	/**
	 * @en Ambiguous key "{1}" of existing objects ({0}): Using "{2}", ignoring "{3}".
	 */
	public static ResKey4 WARN_AMBIGUOUS_EXISTING_KEY__HANDLER_KEY_USED_IGNORED;

	/**
	 * @en No language given for the translation ({0}).
	 */
	public static ResKey1 ERROR_NO_LANGUAGE__HANDLER;

	/**
	 * @en Invalid language "{1}" for the translation ({0}).
	 */
	public static ResKey2 ERROR_INVALID_LANGUAGE__HANDLER_VALUE;

	static {
		initConstants(I18NConstants.class);
	}
}
