/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;


/**
 * Context used in a {@link ChartDataSource} to provide initial objects.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface DataContext {

	/**
	 * Stateless instance of {@link DataContext} providing no further information.
	 */
	public static final DataContext NO_CONTEXT = new DataContext() {
		// no context available
	};

}
