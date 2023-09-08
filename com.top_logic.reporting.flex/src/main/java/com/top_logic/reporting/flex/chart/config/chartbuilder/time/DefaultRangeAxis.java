/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.time;

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;

import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.reporting.flex.chart.config.axis.AbstractAxisBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;

/**
 * Default {@link AbstractAxisBuilder} for the range-axis
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class DefaultRangeAxis extends AbstractAxisBuilder {

	/**
	 * Creates a {@link DefaultRangeAxis} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultRangeAxis(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Axis createAxis(Axis axis, ChartAxis chartAxis, ChartData<? extends Dataset> chartData,
			JFreeChartBuilder.Config parent) {
		ValueAxis anAxis = (ValueAxis) axis;
		TimeSeriesChartBuilder.Config builder = (TimeSeriesChartBuilder.Config) getConfig().getBuilder();
		if (builder == null) {
			builder = (TimeSeriesChartBuilder.Config) parent;
		}
		double minFactor = builder.getYAxisMinFactor();
		double maxFactor = builder.getYAxisMaxFactor();
		Range newRange =
			ChartUtil.normalizeRange(anAxis.getLowerBound() * minFactor, anAxis.getUpperBound() * maxFactor);
		anAxis.setRange(newRange);
		anAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		return anAxis;
	}

}