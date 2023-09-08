/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey SEARCH_RESULT = legacyKey("searchQuery.configChartReport.searchResult");

	public static ResKey STORED_QUERY = legacyKey("searchQuery.configChartReport.storedQuery");

	public static ResKey NO_CHART_CONFIG;

	static {
		initConstants(I18NConstants.class);
	}
}
