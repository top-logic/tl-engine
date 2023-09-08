/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import static java.util.Collections.*;

import java.util.Collection;
import java.util.Set;

/**
 * A {@link ChartDataSource} that returns an empty {@link Set}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public final class EmptyChartDataSource implements ChartDataSource<DataContext> {

	/**
	 * The {@link EmptyChartDataSource} instance.
	 */
	public static final EmptyChartDataSource INSTANCE = new EmptyChartDataSource();

	@Override
	public Collection<? extends Object> getRawData(DataContext context) {
		return emptySet();
	}

}
