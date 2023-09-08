/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.spider.tooltip;

import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;

/**
 * {@link StandardCategoryToolTipGenerator} for spider-charts that ignores the special-series.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class DefaultSpiderChartTooltipGenerator extends StandardCategoryToolTipGenerator {

	private int _size;

	/**
	 * Creates a new {@link DefaultSpiderChartTooltipGenerator}
	 * 
	 * @param ignoreSeries
	 *        the number of series where no tooltips should be generated.
	 */
	public DefaultSpiderChartTooltipGenerator(int ignoreSeries) {
		_size = ignoreSeries;
	}

	@Override
	public String generateToolTip(CategoryDataset dataset, int row, int column) {
		if (row < _size) {
			return null;
		}
		return super.generateToolTip(dataset, row, column);
	}

}