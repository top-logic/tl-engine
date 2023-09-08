/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search;

import org.jfree.data.general.Dataset;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.chart.component.AbstractChartComponent;
import com.top_logic.reporting.flex.chart.component.handler.PostPreparationHandler;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;

/**
 * Implementation of {@link PostPreparationHandler} that updates the {@link ChartContextObserver}
 * with the newly created {@link ChartTree} and {@link Dataset}.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class SearchResultPostPreparationHandler implements PostPreparationHandler {

	/**
	 * Singleton <code>INSTANCE</code> of SearchResultPostPreparationHandler
	 */
	public static SearchResultPostPreparationHandler INSTANCE = new SearchResultPostPreparationHandler();

	@Override
	public void onChartPrepared(AbstractChartComponent component, Dataset dataSet, ChartTree tree) {
		LayoutComponent master = component.getMaster();
		if (master instanceof SearchResultChartConfigComponent) {
			((SearchResultChartConfigComponent) master).notify(dataSet, tree);
		}
	}

}