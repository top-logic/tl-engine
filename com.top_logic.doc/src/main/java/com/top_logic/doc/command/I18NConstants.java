/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command;

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

	public static ResKey UNZIP_FAILED;

	public static ResPrefix IMPORT_DIALOG;

	public static ResKey FAILURE_GETTING_ROOT_FOLDER;

	public static ResKey SELECTION_IS_NO_PAGE;

	public static ResKey NO_HELP_FOR_VIEW;

	/**
	 * @en There is no documentation for the current view.
	 */
	public static ResKey1 NO_DOCUMENTATION_FOR_VIEW__ID;

	public static ResKey NO_IMPORT_FILES;

	public static ResKey2 NO_IMPORT_FILES__LANGUAGE__PATH;

	public static ResKey2 NO_IMPORT_FILES_ZIP__LANGUAGE__FILE;

	public static ResKey1 REQUIRED_IMPORT_STRUCTURE__FILE;

	public static ResKey PAGES_GENERATED;

	public static ResKey NO_PAGES_GENERATED;

	static {
		initConstants(I18NConstants.class);
	}
}
