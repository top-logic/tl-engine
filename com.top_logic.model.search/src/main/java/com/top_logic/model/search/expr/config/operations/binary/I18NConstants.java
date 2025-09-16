/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Invalid content type "{0}" specified: {1}
	 */
	public static ResKey2 ERROR_INVALID_CONTENT_TYPE_SPECIFIED__VALUE_MSG;

	/**
	 * @en Binary data expected in "{1}" but got "{0}".
	 */
	public static ResKey2 ERROR_BINARY_DATA_EXPECTED__ACTUAL_EXPR;

	/**
	 * @en Conversion in "{1}" failed: {0}
	 */
	public static ResKey2 ERROR_CONVERSION_FAILED__MSG_EXPR;

	/**
	 * @en Headers should be in the same row "{1}" but got "{0}".
	 */
	public static ResKey2 ERROR_INVALID_CELL_ENTRY__VALUE_MSG;

	/**
	 * @en Given content type "{0}" is not supported.
	 */
	public static ResKey1 ERROR_UNSUPPORTED_CONTENT_TYPE__VALUE_MSG;

	/**
	 * @en Failed to unzip data from "{0}": {1}
	 */
	public static ResKey2 ERROR_UNZIP_FAILED__NAME_MSG;

	/**
	 * @en Instance import of file "{0}" failed: {1}
	 */
	public static ResKey2 INSTANCE_IMPORT_FAILED__FILE_MSG;

	static {
		initConstants(I18NConstants.class);
	}
}
