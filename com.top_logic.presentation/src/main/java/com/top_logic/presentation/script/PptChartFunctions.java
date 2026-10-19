/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.script;

import static com.top_logic.presentation.data.PresentationData.*;
import static com.top_logic.presentation.script.PptDescriptionFunctions.put;
import static com.top_logic.presentation.script.PptDescriptionFunctions.putBounds;
import static com.top_logic.presentation.script.PptDescriptionFunctions.typed;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions describing charts with bars or columns.
 *
 * <p>
 * The charts are native PowerPoint charts: their data is stored in a workbook embedded in the
 * presentation, so that it can be edited in PowerPoint with "Edit data". The colors of the data
 * series are color roles of the template profile (see <code>pptProfile()</code>), the texts use
 * the fonts of the template's theme.
 * </p>
 *
 * <p>
 * Like the other elements, a chart can be placed with <code>x</code>, <code>y</code>,
 * <code>width</code>, <code>height</code> and <code>weight</code>, or arranged in rows and columns
 * (<code>pptRow()</code>, <code>pptColumn()</code>). All lengths are given in points.
 * </p>
 */
@ScriptPrefix("ppt")
public class PptChartFunctions extends TLScriptFunctions {

	/**
	 * Describes a chart with horizontal bars, the categories listed from top to bottom.
	 *
	 * <p>
	 * Example: <code>pptBarChart(["React UI", "Security", "TL-Script"], [pptSeries("Features",
	 * [59, 20, 11], "topicA"), pptSeries("Fixes", [75, 34, 34], "risk")], stacked: true)</code>
	 * </p>
	 *
	 * <p>
	 * By default, the values are written at the bars instead of showing a value axis, and a chart
	 * with several series has a legend below the bars.
	 * </p>
	 *
	 * @param categories
	 *        The labels of the categories, e.g. the topics.
	 * @param series
	 *        The data series: a list of series (<code>pptSeries()</code>), each a dictionary with
	 *        the entries <code>name</code>, <code>values</code> and <code>role</code>, or a list of
	 *        these values in this order. The values are a list with a number for each category;
	 *        <code>null</code> stands for a missing value. A series without role gets the role
	 *        <code>topicA</code>, <code>topicB</code>, ... of its position.
	 * @param stacked
	 *        Whether the bars of the series are stacked. By default, they stand side by side.
	 * @param labels
	 *        Whether the values are written at the bars. The default is <code>true</code>;
	 *        <code>false</code> shows a value axis with grid lines instead.
	 * @param legend
	 *        The position of the legend: <code>bottom</code>, <code>top</code>, <code>left</code>,
	 *        <code>right</code> or <code>none</code>. By default <code>bottom</code> for several
	 *        series, <code>none</code> for a single one.
	 * @param max
	 *        The largest value of the value axis, e.g. to give several charts the same scale. By
	 *        default derived from the data.
	 * @param gap
	 *        The distance between the categories in percent of the width of a bar, between 0 and
	 *        500. The default is 60.
	 * @param format
	 *        The number format of the values in the notation of spreadsheets, e.g.
	 *        <code>#,##0</code> or <code>0.0%</code>.
	 * @param title
	 *        A title above the chart. Usually not needed, since the slide has a title.
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height.
	 * @param weight
	 *        The share in the free space of a row or column, if no own size is given.
	 * @return The dictionary describing the chart.
	 */
	@Label("Describe a bar chart")
	@SideEffectFree
	public static Map<String, Object> barChart(@Mandatory List<?> categories, @Mandatory List<?> series,
			Boolean stacked, Boolean labels, String legend, Double max, Double gap, String format, Object title,
			Double x, Double y, Double width, Object height, Double weight) {
		return chart(TYPE_BAR_CHART, categories, series, stacked, labels, legend, max, gap, format, title, x, y,
			width, height, weight);
	}

	/**
	 * Describes a chart with vertical columns, the categories listed from left to right.
	 *
	 * <p>
	 * Example: <code>pptColumnChart(["15.07.", "29.07.", "12.08."], [["created", [27, 19, 5],
	 * "muted"], ["closed", [24, 24, 15], "topicC"]])</code>
	 * </p>
	 *
	 * <p>
	 * By default, the values are written above the columns instead of showing a value axis, and a
	 * chart with several series has a legend below the columns.
	 * </p>
	 *
	 * @param categories
	 *        The labels of the categories, e.g. the periods.
	 * @param series
	 *        The data series: a list of series (<code>pptSeries()</code>), each a dictionary with
	 *        the entries <code>name</code>, <code>values</code> and <code>role</code>, or a list of
	 *        these values in this order. The values are a list with a number for each category;
	 *        <code>null</code> stands for a missing value. A series without role gets the role
	 *        <code>topicA</code>, <code>topicB</code>, ... of its position.
	 * @param stacked
	 *        Whether the columns of the series are stacked. By default, they stand side by side.
	 * @param labels
	 *        Whether the values are written at the columns. The default is <code>true</code>;
	 *        <code>false</code> shows a value axis with grid lines instead.
	 * @param legend
	 *        The position of the legend: <code>bottom</code>, <code>top</code>, <code>left</code>,
	 *        <code>right</code> or <code>none</code>. By default <code>bottom</code> for several
	 *        series, <code>none</code> for a single one.
	 * @param max
	 *        The largest value of the value axis, e.g. to give several charts the same scale. By
	 *        default derived from the data.
	 * @param gap
	 *        The distance between the categories in percent of the width of a column, between 0
	 *        and 500. The default is 60.
	 * @param format
	 *        The number format of the values in the notation of spreadsheets, e.g.
	 *        <code>#,##0</code> or <code>0.0%</code>.
	 * @param title
	 *        A title above the chart. Usually not needed, since the slide has a title.
	 * @param x
	 *        The horizontal position of the left border.
	 * @param y
	 *        The vertical position of the top border.
	 * @param width
	 *        The width.
	 * @param height
	 *        The height.
	 * @param weight
	 *        The share in the free space of a row or column, if no own size is given.
	 * @return The dictionary describing the chart.
	 */
	@Label("Describe a column chart")
	@SideEffectFree
	public static Map<String, Object> columnChart(@Mandatory List<?> categories, @Mandatory List<?> series,
			Boolean stacked, Boolean labels, String legend, Double max, Double gap, String format, Object title,
			Double x, Double y, Double width, Object height, Double weight) {
		return chart(TYPE_COLUMN_CHART, categories, series, stacked, labels, legend, max, gap, format, title, x,
			y, width, height, weight);
	}

	/**
	 * Describes a data series of a chart.
	 *
	 * <p>
	 * Example: <code>pptSeries("Fixes", [75, 34, null], "risk")</code>
	 * </p>
	 *
	 * @param name
	 *        The name of the series, shown in the legend.
	 * @param values
	 *        A number for each category of the chart, <code>null</code> for a missing value.
	 * @param role
	 *        The color role of the bars, e.g. <code>topicA</code>. If not given, the role
	 *        <code>topicA</code>, <code>topicB</code>, ... of the position of the series.
	 * @return The dictionary describing the series.
	 */
	@Label("Describe a data series")
	@SideEffectFree
	public static Map<String, Object> series(Object name, @Mandatory List<?> values, String role) {
		Map<String, Object> result = PptDescriptionFunctions.map();
		put(result, NAME, name);
		result.put(VALUES, values);
		put(result, ROLE, role);
		return result;
	}

	private static Map<String, Object> chart(String type, List<?> categories, List<?> series, Boolean stacked,
			Boolean labels, String legend, Double max, Double gap, String format, Object title, Double x, Double y,
			Double width, Object height, Double weight) {
		Map<String, Object> result = typed(type);
		result.put(CATEGORIES, categories);
		result.put(SERIES, series);
		put(result, STACKED, stacked);
		put(result, LABELS, labels);
		put(result, LEGEND, legend);
		put(result, MAX, max);
		put(result, GAP, gap);
		put(result, FORMAT, format);
		put(result, TITLE, title);
		putBounds(result, x, y, width, height, weight);
		return result;
	}

}
