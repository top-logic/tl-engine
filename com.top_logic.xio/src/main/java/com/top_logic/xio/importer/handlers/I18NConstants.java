/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey3 IMPORT_SPEC__FILE_LINE_COL;

	public static ResKey3 ERROR_UNEXPECTED_ATTRIBUTE_VALUE__HANDLER_GIVEN_EXPECTED;

	public static ResKey1 ERROR_EXPECTED_END_OF_ELEMENT__HANDLER;

	public static ResKey2 ERROR_PROPERTY_FORMAT__HANDLER_MESSAGE;

	public static ResKey2 ERROR_ID_FORMAT__HANDLER_MESSAGE;

	public static ResKey2 ERROR_DUPLICATE_ASSIGNMENT_TO_VARIABLE__HANDLER_VAR;

	public static ResKey2 ERROR_FORWARD_VARIABLE_ALREADY_ASSIGNED__HANDLER_VAR;

	public static ResKey2 ERROR_MANDATORY_ID_ATTRIBUTES_NOT_SET__HANDLER_ATTR;

	public static ResKey1 ERROR_END_ELEMENT_EXPECTED__TAG; // Expected to have consumed all data up to the matching end element of ''{0}''.

	public static ResKey2 ERROR_UNEXPECTED_END_TAG__EXPECTED_FOUND; // Expected to have consumed all data up to the matching end element of '{0}', found: {1}


	static {
		initConstants(I18NConstants.class);
	}
}
