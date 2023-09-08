/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
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

	public static ResPrefix DATA_IMPORT = legacyPrefix("abstractdataimport.");

	public static ResKey DATA_IMPORT_IN_USE;

	public static ResKey1 DATA_IMPORT_ERROR;

	public static ResKey NO_VALUE_IMPORT_NOT_EXECUTABLE;

	public static ResKey ZIP_ENDING_ERROR;

	public static ResKey IMPORT;

	static {
		initConstants(I18NConstants.class);
	}
}
