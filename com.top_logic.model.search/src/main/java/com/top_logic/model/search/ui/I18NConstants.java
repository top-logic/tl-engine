/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/** @en No search expression selected. */
	public static ResKey ERROR_NO_SEARCH_EXPRESSION;

	/** @en Not valid TLScript expression */
	public static ResKey MODEL_SEARCH_PARSE_ERROR;

	/** @en Error when executing the script. */
	public static ResKey1 ERROR_EXECUTING_SEARCH__EXPR;

	static {
		initConstants(I18NConstants.class);
	}
}
