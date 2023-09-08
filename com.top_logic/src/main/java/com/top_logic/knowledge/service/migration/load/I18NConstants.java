/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.load;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix UPLOAD_DUMP;

	public static ResKey CONFIRM_IMPORT;

	public static ResKey DOWNLOAD_SCRIPT;

	public static ResKey DOWNLOAD_SQL;

	public static ResKey DUMP_DATA_REQUIRED;

	public static ResKey ERROR_CONVERSION_FAILED;

	public static ResKey1 ERROR_EXTRACTING_DATA_VERSION__MESSAGE;

	public static ResKey ERROR_IMPORT_FAILED;

	public static ResKey ERROR_NO_DOWNGRADE_POSSIBLE;

	public static ResKey ERROR_RESTART_FAILED;

	public static ResKey ERROR_SQL_PREPROCESSING_REQUIRED;

	public static ResKey IMPORT_DATA_JSP_TITLE;

	public static ResKey IMPORT_DATA_JSP_DESCRIPTION;

	public static ResKey NO_DATA_VERSION;

	public static ResKey NO_MIGRATIONS;

	public static ResKey START_IMPORT;

	static {
		initConstants(I18NConstants.class);
	}
}
