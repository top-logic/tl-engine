/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.tooltip;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYZToolTipGenerator;
import org.jfree.data.general.AbstractSeriesDataset;

import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface XYZToolTipGeneratorProvider {
	/**
	 * the generator to be applied to a Bubble Chart
	 */
	public XYZToolTipGenerator getXYZTooltipGenerator(JFreeChart model, ChartContext context,
			ChartData<? extends AbstractSeriesDataset> chartData);

}
