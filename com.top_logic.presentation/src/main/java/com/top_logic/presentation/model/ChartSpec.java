/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.List;

/**
 * A chart with bars or columns, showing data series over categories.
 *
 * @param bounds
 *        See {@link ElementSpec#bounds()}.
 * @param horizontal
 *        Whether the bars are horizontal, the categories listed from top to bottom; otherwise
 *        vertical columns, the categories from left to right.
 * @param categories
 *        The labels of the categories.
 * @param series
 *        The data series, each with a value for each category.
 * @param stacked
 *        Whether the bars of the series are stacked; otherwise they stand side by side.
 * @param labels
 *        Whether the values are shown at the bars. Then the value axis is hidden.
 * @param legend
 *        The position of the legend.
 * @param max
 *        The largest value of the value axis, <code>null</code> for a value derived from the data.
 * @param gap
 *        The distance between the categories in percent of the width of a bar.
 * @param format
 *        The number format of the values in the notation of spreadsheets, e.g.
 *        <code>0.0 %</code>, <code>null</code> for the general format.
 * @param title
 *        The title shown above the chart, <code>null</code> for none.
 */
public record ChartSpec(Bounds bounds, boolean horizontal, List<String> categories, List<Series> series,
		boolean stacked, boolean labels, ChartLegend legend, Double max, int gap, String format, String title)
		implements ElementSpec {

	/**
	 * A data series of a chart.
	 *
	 * @param name
	 *        The name of the series, shown in the legend.
	 * @param values
	 *        The value for each category, <code>null</code> for a missing value.
	 * @param role
	 *        The color role of the bars of the series.
	 */
	public record Series(String name, List<Double> values, String role) {
		// Pure data.
	}

}
