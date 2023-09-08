/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.basic.util.ResKey5;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey4 ERROR_PROBLEM__FILE_LINE_COL_MSG;

	public static ResKey1 ERROR_NO_LINKING__HANDLER;

	public static ResKey1 ERROR_INVALID_PART_REF__VALUE;

	public static ResKey2 ERROR_UNRESOLVED_ID__HANDLER_ID;

	public static ResKey2 ERROR_DUPLICATE_IDENTIFIER__HANDLER_ID;

	public static ResKey2 ERROR_LINKING_NULL_TARGET__HANDLER_VAR;

	public static ResKey2 ERROR_LINKING_NULL_VALUE__HANDLER_VAR;

	public static ResKey5 ERROR_SETTING_PROPERTY_FAILED__HANDLER_OBJ_PROP_VALUE_MESSAGE;

	static {
		initConstants(I18NConstants.class);
	}
}
