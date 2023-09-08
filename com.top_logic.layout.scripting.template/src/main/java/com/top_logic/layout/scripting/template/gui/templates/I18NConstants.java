/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix UPLOAD_SCRIPT_RECORDER_TEMPLATE_DIALOG_PREFIX;

	public static ResKey2 DEFAULT_LABEL__LABEL_DEFAULT;

	public static ResKey ERROR_NO_TEMPLATE_SELECTED;

	public static ResKey1 EMPLTY_DEFAULT_LABEL__LABEL;

	public static ResKey PARAMETER_HEADING;

	public static ResKey DEFAULT_VALUE_HEADIN;

	public static ResKey2 ERROR_TEMPLATE_LOADING_FAILED__NAME_ERROR;

	public static ResKey ERROR_FILE_NAME_MUST_NOT_BE_EMPTY;

	public static ResKey ERROR_FILE_NAME_MUST_NOT_START_WITH_DOT;

	public static ResKey ERROR_FILE_NAME_MUST_NOT_START_OR_END_WITH_WHITE_SPACE;

	public static ResKey1 ERROR_FILE_NAME_MUST_END_IN__SUFFIX;

	public static ResKey1 ERROR_FILE_NAME_MUST_NOT_CONTAIN__CHARS;

	public static ResKey NO_TEMPLATE_PARAMETERS;

	public static ResKey2 ERROR_TEMPLATE_DELETING_FAILED__NAME_ERROR;

	public static ResKey1 REALLY_DELETE__NAME;

	public static ResKey ERROR_TEMPLATE_WITH_NON_STRING_PARAMETER;

	public static ResKey1 ERROR_TEMPLATE_INVALID__ERROR;

	public static ResKey1 ERROR_DUPLICATE_PARAMETER__NAME;

	public static ResKey NO_VALID_SCRIPT_RECORDER_TEMPLATE_FILE;

	static {
		initConstants(I18NConstants.class);
	}
}
