/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.gui.ThemeVar;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;

/**
 * Theme variable declarations for chart colors used in {@link ReactChartJsControl}.
 *
 * @see ThemeVar
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	/** First color in the chart palette. */
	@DefaultValue("#4e79a7")
	public static ThemeVar<String> CHART_COLOR_1;

	/** Second color in the chart palette. */
	@DefaultValue("#f28e2b")
	public static ThemeVar<String> CHART_COLOR_2;

	/** Third color in the chart palette. */
	@DefaultValue("#e15759")
	public static ThemeVar<String> CHART_COLOR_3;

	/** Fourth color in the chart palette. */
	@DefaultValue("#76b7b2")
	public static ThemeVar<String> CHART_COLOR_4;

	/** Fifth color in the chart palette. */
	@DefaultValue("#59a14f")
	public static ThemeVar<String> CHART_COLOR_5;

	/** Sixth color in the chart palette. */
	@DefaultValue("#edc948")
	public static ThemeVar<String> CHART_COLOR_6;

	/** Seventh color in the chart palette. */
	@DefaultValue("#b07aa1")
	public static ThemeVar<String> CHART_COLOR_7;

	/** Eighth color in the chart palette. */
	@DefaultValue("#ff9da7")
	public static ThemeVar<String> CHART_COLOR_8;

	/** Ninth color in the chart palette. */
	@DefaultValue("#9c755f")
	public static ThemeVar<String> CHART_COLOR_9;

	/** Tenth color in the chart palette. */
	@DefaultValue("#bab0ac")
	public static ThemeVar<String> CHART_COLOR_10;

	/** Color used for grid lines in charts. */
	@DefaultValue("rgba(0,0,0,0.1)")
	public static ThemeVar<String> CHART_GRID_COLOR;

	/** Color used for text labels and tick marks in charts. */
	@DefaultValue("#374151")
	public static ThemeVar<String> CHART_TEXT_COLOR;

	/** Background color of the chart canvas area. */
	@DefaultValue("transparent")
	public static ThemeVar<String> CHART_BACKGROUND_COLOR;

	/** Border color used for chart elements such as dataset bars and lines. */
	@DefaultValue("rgba(0,0,0,0.1)")
	public static ThemeVar<String> CHART_BORDER_COLOR;

	/**
	 * Returns the current theme colors as a map for use in the React chart component.
	 *
	 * <p>
	 * The returned map contains:
	 * <ul>
	 * <li>{@code palette} &mdash; a {@link List} of ten color strings</li>
	 * <li>{@code gridColor} &mdash; the grid line color</li>
	 * <li>{@code textColor} &mdash; the text/tick color</li>
	 * <li>{@code backgroundColor} &mdash; the chart background color</li>
	 * <li>{@code borderColor} &mdash; the chart element border color</li>
	 * </ul>
	 * </p>
	 */
	public static Map<String, Object> getChartThemeColors() {
		List<String> palette = new ArrayList<>();
		palette.add(CHART_COLOR_1.get());
		palette.add(CHART_COLOR_2.get());
		palette.add(CHART_COLOR_3.get());
		palette.add(CHART_COLOR_4.get());
		palette.add(CHART_COLOR_5.get());
		palette.add(CHART_COLOR_6.get());
		palette.add(CHART_COLOR_7.get());
		palette.add(CHART_COLOR_8.get());
		palette.add(CHART_COLOR_9.get());
		palette.add(CHART_COLOR_10.get());

		Map<String, Object> result = new HashMap<>();
		result.put("palette", palette);
		result.put("gridColor", CHART_GRID_COLOR.get());
		result.put("textColor", CHART_TEXT_COLOR.get());
		result.put("backgroundColor", CHART_BACKGROUND_COLOR.get());
		result.put("borderColor", CHART_BORDER_COLOR.get());
		return result;
	}

}
