/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import com.top_logic.reporting.flex.chart.config.model.ChartTree;

/**
 * Combination of the univeral {@link ChartTree} model with the chart-specific {@link JFreeChart}
 * model.
 * 
 * @param <D>
 *        The concrete {@link JFreeChart} data set implementation specific for the generated chart.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class ChartData<D extends Dataset> {

	private final ChartTree _model;

	private final D _dataset;

	/**
	 * Creates a new {@link ChartData}
	 * 
	 * @param model
	 *        the universal model containing all relevant information
	 * @param dataset
	 *        the {@link JFreeChart} model containing only relevant values to draw a chart
	 */
	public ChartData(ChartTree model, D dataset) {
		_model = model;
		_dataset = dataset;
	}

	/**
	 * the {@link JFreeChart} model
	 */
	public D getDataset() {
		return _dataset;
	}

	/**
	 * the universal chart-model
	 */
	public ChartTree getModel() {
		return _model;
	}

}
