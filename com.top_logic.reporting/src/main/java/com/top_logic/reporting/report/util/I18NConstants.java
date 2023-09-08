/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.util;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Resources of this package.
 * 
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix CHART = legacyPrefix("reporting.chart.");

	public static ResKey CALENDAR_WEEK = legacyKey("element.search.calendarweek");

	public static ResKey WATERFALL_CHART_TOTAL = legacyKey("reporting.chart.waterfall-chart.total");

	public static ResKey GRANULARITY_DAYS = legacyKey("reporting.timePeriod.days");

	public static ResKey GRANULARITY_WEEKS = legacyKey("reporting.timePeriod.weeks");

	public static ResKey GRANULARITY_MONTHS = legacyKey("reporting.timePeriod.months");

	public static ResKey GRANULARITY_QUARTERS = legacyKey("reporting.timePeriod.quarters");

	public static ResKey GRANULARITY_HALF_YEARS = legacyKey("reporting.timePeriod.halfyears");

	public static ResKey GRANULARITY_YEARS = legacyKey("reporting.timePeriod.years");

	static {
		initConstants(I18NConstants.class);
	}
}
