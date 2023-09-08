/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 ERROR_METHOD_PATTERN_MISMATCH__VALUE;

	public static ResKey1 ERROR_METHOD_DECLARED_VIOD__VALUE;

	public static ResKey2 ERROR_METHOD_NOT_FOUND__CLASS_METHOD;

	public static ResKey1 ERROR_CANNOT_ACCESS_METHOD__METHOD;

	static {
		initConstants(I18NConstants.class);
	}
}
