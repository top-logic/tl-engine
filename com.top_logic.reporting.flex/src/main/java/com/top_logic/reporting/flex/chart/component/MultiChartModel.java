/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import java.util.List;

import com.top_logic.reporting.flex.chart.config.ChartConfig;

/**
 * Model object accepted by {@link MultipleChartComponent} referencing multiple charts for display.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class MultiChartModel {

	final List<ChartConfig> _charts;

	/**
	 * Creates a new {@link MultiChartModel}-model for the given chart-configs
	 */
	public MultiChartModel(List<ChartConfig> charts) {
		_charts = charts;
	}

}