/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model.evaluation;

import com.top_logic.basic.util.ResKey;
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

	public static ResKey HEADER_AVERAGE__VALUE;

	public static ResKey HEADER_MAX__VALUE;

	public static ResKey HEADER_MEDIAN__VALUE;

	public static ResKey HEADER_MIN__VALUE;

	public static ResKey HEADER_NULL_COUNT__VALUE;

	public static ResKey HEADER_NULL_PERCENTAGE__VALUE;

	public static ResKey HEADER_SUM__VALUE;

	static {
		initConstants(I18NConstants.class);
	}

}
