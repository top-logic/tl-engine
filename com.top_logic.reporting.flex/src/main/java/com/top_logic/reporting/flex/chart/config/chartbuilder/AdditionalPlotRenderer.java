/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.data.general.Dataset;

import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;

/**
 * A simple {@link ChartConfig} describes a single {@link ChartTree} and a single-layer chart. An
 * {@link AdditionalPlotRenderer} can be configured to add additional datasets and renderers to a
 * {@link JFreeChart}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public interface AdditionalPlotRenderer {

	/**
	 * Hook to modify the chart
	 * 
	 * @param model
	 *        the {@link JFreeChart} with the {@link Plot} to modify
	 * @param chartData
	 *        the {@link ChartData} for context-information
	 */
	public void adaptPlot(JFreeChart model, ChartData<? extends Dataset> chartData);

	/**
	 * Simple implementation of {@link AdditionalPlotRenderer} that does not modify anything.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public static class NoAdditionalPlotRenderer implements AdditionalPlotRenderer {

		/**
		 * Singleton <code>INSTANCE</code>
		 */
		public static final NoAdditionalPlotRenderer INSTANCE = new NoAdditionalPlotRenderer();

		private NoAdditionalPlotRenderer() {
		}

		@Override
		public void adaptPlot(JFreeChart model, ChartData<? extends Dataset> chartData) {
			// do nothing
		}

	}

}