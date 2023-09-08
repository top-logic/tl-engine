/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey NO_VALUE;
	public static ResKey NO_FILTER_ENTRIES;

	public static ResKey FILTER_VALUES;

	public static ResKey FALSE_LABEL = legacyKey("layout.table.filter.falseLabel");

	public static ResKey TRUE_LABEL = legacyKey("layout.table.filter.trueLabel");

	static {
		initConstants(I18NConstants.class);
	}
}
