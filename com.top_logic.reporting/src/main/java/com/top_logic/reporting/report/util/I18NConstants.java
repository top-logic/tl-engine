/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.util;

import com.top_logic.basic.i18n.CustomKey;
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

	@CustomKey("element.search.calendarweek")
	public static ResKey CALENDAR_WEEK;

	@CustomKey("reporting.chart.waterfall-chart.total")
	public static ResKey WATERFALL_CHART_TOTAL;

	@CustomKey("reporting.timePeriod.days")
	public static ResKey GRANULARITY_DAYS;

	@CustomKey("reporting.timePeriod.weeks")
	public static ResKey GRANULARITY_WEEKS;

	@CustomKey("reporting.timePeriod.months")
	public static ResKey GRANULARITY_MONTHS;

	@CustomKey("reporting.timePeriod.quarters")
	public static ResKey GRANULARITY_QUARTERS;

	@CustomKey("reporting.timePeriod.halfyears")
	public static ResKey GRANULARITY_HALF_YEARS;

	@CustomKey("reporting.timePeriod.years")
	public static ResKey GRANULARITY_YEARS;

	static {
		initConstants(I18NConstants.class);
	}
}
