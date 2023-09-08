/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.tooltip;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;

/**
 * Provider for a tooltip-generator that needs context-information.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public interface CategoryToolTipGeneratorProvider {

	/**
	 * the generator to be applied to a {@link CategoryPlot}
	 */
	public CategoryToolTipGenerator getCategoryTooltipGenerator(JFreeChart model, ChartContext context, ChartData<? extends CategoryDataset> chartData);
	
}