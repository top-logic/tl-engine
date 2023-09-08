/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.time;

import java.util.Date;

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.data.general.Dataset;
import org.jfree.data.time.DateRange;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.reporting.flex.chart.config.axis.AbstractAxisBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;

/**
 * Default {@link AbstractAxisBuilder} for the domain-axis
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class DefaultDomainAxis extends AbstractAxisBuilder {

	/**
	 * Creates a {@link DefaultDomainAxis} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultDomainAxis(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Axis createAxis(Axis axis, ChartAxis chartAxis, ChartData<? extends Dataset> chartData,
			JFreeChartBuilder.Config parent) {

		DateAxis dateAxis = (DateAxis) axis;
		TimeSeriesChartBuilder.Config builder = (TimeSeriesChartBuilder.Config) getConfig().getBuilder();
		if (builder == null) {
			builder = (TimeSeriesChartBuilder.Config) parent;
		}
		DateRange range = (DateRange) dateAxis.getRange();
		Date lower = builder.getLowerBound();
		Date upper = builder.getUpperBound();
		boolean change = !((lower == null) && (upper == null));
		if (change && lower == null) {
			lower = range.getLowerDate();
		}
		if (change && upper == null) {
			upper = range.getUpperDate();
		}
		if (change) {
			dateAxis.setRange(lower, upper);
		}
		return dateAxis;
	}

}