/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en There is no column with the identifier {0}. It is important that the inner columns are
	 *     defined in a previous column configuration before it is used in a column group
	 *     configuration.
	 */
	public static ResKey1 NO_SUCH_COLUMN__COLUMN;
	
	/**
	 * {@link ResKey} when table could not be exported.
	 */
	public static ResKey ERROR_TABLE_EXPORT;

	/**
	 * {@link ResKey} when parsing {@link PageSizeOptionsFormat page size options} failed .
	 */
	public static ResKey1 ILLEGAL_PAGE_SIZE_OPTIONS__ALL_OPTION;

	/** {@link ResPrefix} for the dialog to edit multiple table settings. */
	public static ResPrefix EDIT_TABLE_SETTINGS_DIALOG;

	/** {@link ResPrefix} for the dialog to select a named table settings. */
	public static ResPrefix SELECT_NAMED_TABLE_CONFIGURATION;

	static {
		initConstants(I18NConstants.class);
	}
}
