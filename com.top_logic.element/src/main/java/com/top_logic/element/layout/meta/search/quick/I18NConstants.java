/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search.quick;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey PLACEHOLDER;

	/**
	 * @en More results available, refine search...
	 */
	public static ResKey MORE_RESULTS;

	/**
	 * @en No results available, generalise search...
	 */
	public static ResKey NO_RESULTS;

	/**
	 * @en Search entry requires at least {0} characters.
	 */
	public static ResKey1 SEARCH_STRING_TOO_SHORT__MIN;

	public static ResKey1 INVALID_SEARCH_COMPONENT;

	public static ResKey1 NO_SUCH_SEARCH_COMPONENT;

	public static ResKey1 MULTIPLE_SEARCH_COMPONENTS;

	public static ResKey EXECUTE_QUICK_SEARCH;

	public static ResPrefix QUICK_SEARCH;

	static {
		initConstants(I18NConstants.class);
	}
}
