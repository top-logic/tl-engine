/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.handler;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey FAILED = legacyKey("reporting.create.failed");

	public static ResKey DELETE = legacyKey("reporting.report.delete");

	public static ResKey NO_REPORT = legacyKey("tl.executable.disabled.noReport");

	public static ResKey OPEN_CHART_DETAIL = legacyKey("reporting.matrix.chart.openChartDetails");

	/**
	 * @en Created search report.
	 */
	public static ResKey CREATED_SEARCH_REPORT;

	/**
	 * @en Deleted search report.
	 */
	public static ResKey DELETED_SEARCH_REPORT;

	/**
	 * @en Updated search report.
	 */
	public static ResKey UPDATED_SEARCH_REPORT;

	static {
		initConstants(I18NConstants.class);
	}
}
