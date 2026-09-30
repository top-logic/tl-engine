/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.expr;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The XML import failed: {0}
	 */
	public static ResKey1 ERROR_IMPORT_FAILED__EXPR;

	/**
	 * @en The argument of {1} must be binary data, but was: {0}
	 */
	public static ResKey2 ERROR_NO_BINARY_INPUT__VALUE_EXPR;

	static {
		initConstants(I18NConstants.class);
	}
}
