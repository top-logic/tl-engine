/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Executing the stored query failed
	 */
	public static ResKey EXECUTING_STORED_QUERY_FAILED;

	/**
	 * @en Deleted stored search: {0}
	 */
	public static ResKey1 DELETED_STORED_SEARCH__NAME;

	/**
	 * @en Stored search query.
	 */
	public static ResKey1 STORED_SEARCH_QUERY__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
