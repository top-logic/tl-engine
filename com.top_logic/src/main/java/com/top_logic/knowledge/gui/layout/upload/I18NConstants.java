/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.upload;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 ERROR_FILENAME_WITH_INVALID_CHAR = legacyKey1("constraint.filename.containsInvalid");

	public static ResKey1 ERROR_WRONG_FILENAME__FILENAMES = legacyKey1("constraint.filename.mustBe");

	public static ResKey1 ERROR_FILENAME_WITH_UNSUPPORTED_EXTENSION__EXTENSIONS =
		legacyKey1("constraint.filename.mustEndWith");

	public static ResKey1 ERROR_WRONG_FILENAME__DISALLOWED = legacyKey1("constraint.filename.mustNotBe");

	public static ResKey FORBIDDEN_FILE_NAME_EMPTY;

	public static ResKey1 FORBIDDEN_FILE_NAME_FOLDER_TRAVERSAL__NAME;

	public static ResKey1 FORBIDDEN_FILE_NAME_SUB_FOLDER__NAME;

	public static ResKey3 FORBIDDEN_FILE_NAME_TOO_LONG__NAME_SIZE_MAX;

	public static ResKey2 FORBIDDEN_FILE_NAME_WINDOWS_INCOMPATIBLE_CHARACTERS__NAME_CHARACTERS;

	public static ResKey2 FORBIDDEN_FILE_NAME_WRONG_ENDING__NAME_ENDINGS;

	static {
		initConstants(I18NConstants.class);
	}
}
