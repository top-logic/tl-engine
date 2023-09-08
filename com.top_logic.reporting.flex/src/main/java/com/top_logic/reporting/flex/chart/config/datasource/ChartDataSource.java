/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import java.util.Collection;

/**
 * A {@link ChartDataSource} provides the initial objects to create a chart for.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface ChartDataSource<C extends DataContext> {

	/**
	 * Provides the initial objects to create a chart for.
	 */
	Collection<? extends Object> getRawData(C context);

}
