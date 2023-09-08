/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.axis;

import java.text.DateFormat;

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.data.general.Dataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.reporting.common.format.ReportingDateFormat;
import com.top_logic.reporting.common.period.BYHalfYear;
import com.top_logic.reporting.common.period.BYQuarter;
import com.top_logic.reporting.common.period.BYYear;
import com.top_logic.reporting.common.period.HalfYear;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;

/**
 * Configurable {@link AbstractAxisBuilder} that creates a {@link PeriodAxis}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class PeriodAxisBuilder extends AbstractAxisBuilder {

	/**
	 * Config-interface for {@link PeriodAxisBuilder}.
	 */
	public interface Config extends AbstractAxisBuilder.Config {

		@Override
		@ClassDefault(PeriodAxisBuilder.class)
		public Class<? extends PeriodAxisBuilder> getImplementationClass();
	}

	/**
	 * Config-constructor for {@link PeriodAxisBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public PeriodAxisBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Axis createAxis(Axis axis, ChartAxis chartAxis, ChartData<? extends Dataset> chartData,
			JFreeChartBuilder.Config parent) {
		JFreeChartBuilder.Config builder = getConfig().getBuilder();
		if (builder == null) {
			builder = parent;
		}
		String label = chartAxis.getLabel(builder);

		TimeSeriesCollection timeseries = (TimeSeriesCollection) chartData.getDataset();
		if (timeseries.getSeriesCount() == 0) {
			return null;
		}

		TimeSeriesDataItem item = timeseries.getSeries(0).getDataItem(0);
		RegularTimePeriod period = item.getPeriod();
		PeriodAxis result = createAxis(period.getClass());
		result.setLabel(label);
		return result;
	}

	private PeriodAxis createAxis(Class<? extends RegularTimePeriod> clazz) {
		if (clazz == Year.class) {
			return createPeriodAxisWithLabelInfo(Year.class, HalfYear.class, Year.class);
		}
		else if (clazz == HalfYear.class) {
			return createPeriodAxisWithLabelInfo(HalfYear.class, HalfYear.class, Year.class);
		}
		else if (clazz == Month.class) {
			return createPeriodAxisWithLabelInfo(Month.class, Month.class, Year.class);
		}
		else if (clazz == Week.class) {
			return createPeriodAxisWithLabelInfo(Week.class, Week.class, Month.class);
		}
		else if (clazz == Day.class) {
			return createPeriodAxisWithLabelInfo(Day.class, Day.class, Week.class);
		}
		else if (clazz == Hour.class) {
			return createPeriodAxisWithLabelInfo(Hour.class, Hour.class, Day.class);
		}
		else {
			throw new IllegalArgumentException("Granularity " + clazz + " not supported for a date axis");
		}
	}

	private PeriodAxis createPeriodAxisWithLabelInfo(Class<? extends RegularTimePeriod> tickUnit,
			Class<? extends RegularTimePeriod> axis1, Class<? extends RegularTimePeriod> axis2) {
		PeriodAxis axis = new PeriodAxis("");
		axis.setMajorTickTimePeriodClass(tickUnit);
		axis.setAutoRangeTimePeriodClass(tickUnit);
		PeriodAxisLabelInfo[] info = new PeriodAxisLabelInfo[2];
		info[0] = new PeriodAxisLabelInfo(axis1, getDateFormat(axis1));
		info[1] = new PeriodAxisLabelInfo(axis2, getDateFormat(axis2));
		axis.setLabelInfo(info);
		return axis;
	}

	private DateFormat getDateFormat(Class<? extends RegularTimePeriod> aPeriod) {
		if (aPeriod.equals(Hour.class)) {
			return CalendarUtil.newSimpleDateFormat("HH");
		}
		if (aPeriod.equals(Day.class)) {
			return CalendarUtil.newSimpleDateFormat("dd");
		}
		else if (aPeriod.equals(Week.class)) {
			return CalendarUtil.newSimpleDateFormat("WW");
		}
		else if (aPeriod.equals(Month.class)) {
			return CalendarUtil.newSimpleDateFormat("MMM");
		}
		else if (aPeriod.equals(Quarter.class)) {
			return ReportingDateFormat.getQuarterFormatTooltip();
		}
		else if (aPeriod.equals(BYQuarter.class)) {
			return ReportingDateFormat.getQuarterFormatBusinessYearTooltip();
		}
		else if (aPeriod.equals(HalfYear.class)) {
			return ReportingDateFormat.getHalfYearFormat();
		}
		else if (aPeriod.equals(BYHalfYear.class)) {
			return ReportingDateFormat.getHalfYearFormatBusinessYearTooltip();
		}
		else if (aPeriod.equals(Year.class)) {
			return ReportingDateFormat.getYearFormatTooltip();
		}
		else if (aPeriod.equals(BYYear.class)) {
			return ReportingDateFormat.getYearFormatBusinessYearTooltip();
		}
		return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm");
	}

}
