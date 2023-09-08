/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.handler;

import org.jfree.data.general.Dataset;

import com.top_logic.reporting.flex.chart.component.AbstractChartComponent;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;

/**
 * Handler interface for additional modifications after the {@link ChartTree} has been calculated.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public interface PostPreparationHandler {

	/**
	 * Callback after the chart model was prepared and the {@link Dataset} has been calculated.
	 * 
	 * @param component
	 *        the target component that displays the chart
	 * @param dataSet
	 *        the JFreeChart-model
	 * @param tree
	 *        the universl chart-model
	 */
	public void onChartPrepared(AbstractChartComponent component, Dataset dataSet, ChartTree tree);

}