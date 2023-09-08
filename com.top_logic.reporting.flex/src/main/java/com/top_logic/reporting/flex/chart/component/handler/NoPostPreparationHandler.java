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
 * Default-implementation of {@link PostPreparationHandler} that does nothing.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class NoPostPreparationHandler implements PostPreparationHandler {

	/**
	 * Singleton <code>INSTANCE</code>
	 */
	public static NoPostPreparationHandler INSTANCE = new NoPostPreparationHandler();

	private NoPostPreparationHandler() {
	}

	@Override
	public void onChartPrepared(AbstractChartComponent component, Dataset dataSet, ChartTree tree) {
		// do nothing
	}

}