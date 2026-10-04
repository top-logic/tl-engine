/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.present.ChartData;
import com.top_logic.tl.ai.core.tools.present.ChartKind;

/**
 * What the pane adds to the configuration it draws - and, as much, what it leaves alone.
 */
public class TestPaneCharts extends TestCase {

	private static final ReadFilter ALL = ReadFilter.forSecurity(ModelSecurity.UNCHECKED);

	/**
	 * A point of computed data stands for no object: its dataset gets no click handler, so a click
	 * on a bar of "events per status" opens nothing rather than something arbitrary.
	 */
	public void testDataHasNoClickHandler() {
		Map<String, Object> config = PaneCharts.config(data(dict("Draft", 2, "Done", 1)), "open");

		assertFalse(dataset(config, 0).containsKey("onClick"));
	}

	/**
	 * A dataset whose points have no object left - none the user may read - gets no handler either.
	 * That a point of an object opens it is {@link TestPresentTool}'s, which has objects.
	 */
	public void testAPointWithoutAnObjectOpensNothing() {
		ChartData chart = ChartData.ofPoints(ChartKind.BAR, "Revenue", List.of("A"), List.of(3),
			Arrays.asList((TLObject) null));

		assertFalse(dataset(PaneCharts.config(chart, "open"), 0).containsKey("onClick"));
	}

	/**
	 * The pane's look fills in only what the script left open: a script that chose its colours, its
	 * legend or its axes keeps them.
	 */
	public void testWhatTheScriptSetIsKept() {
		ChartData chart = data(dict(
			"type", "bar",
			"data", dict("labels", List.of("Q1"), "datasets", List.of(
				dict("label", "A", "data", List.of(1), "borderRadius", 0),
				dict("label", "B", "data", List.of(2)))),
			"options", dict(
				"plugins", dict("legend", dict("display", false)),
				"scales", dict("y", dict("ticks", dict("color", "red")),
					"y2", dict("position", "right", "title", dict("display", true, "text", "Revenue"))))));

		Map<String, Object> config = PaneCharts.config(chart, "open");

		assertEquals("Set by the script.", 0, dataset(config, 0).get("borderRadius"));
		assertEquals("Left open, filled in.", 4, dataset(config, 1).get("borderRadius"));
		Map<?, ?> options = (Map<?, ?>) config.get("options");
		assertEquals(false, path(options, "plugins", "legend").get("display"));
		assertEquals("red", path(options, "scales", "y", "ticks").get("color"));
		assertEquals("A second axis the script added is legible too.", "#8b919c",
			path(options, "scales", "y2", "ticks").get("color"));
		assertEquals("So is its title.", "#8b919c", path(options, "scales", "y2", "title").get("color"));
	}

	/** Several series need a legend to tell them apart; one series of bars needs none. */
	public void testALegendWhereItNamesSomething() {
		Map<String, Object> one = PaneCharts.config(data(dict("Draft", 2)), "open");
		Map<String, Object> two = PaneCharts.config(data(dict("Draft", dict("count", 2, "sum", 30))), "open");

		assertEquals(false, path((Map<?, ?>) one.get("options"), "plugins", "legend").get("display"));
		assertEquals(true, path((Map<?, ?>) two.get("options"), "plugins", "legend").get("display"));
	}

	/** A count has whole steps on its axis; a chart of fractions keeps Chart.js's own. */
	public void testWholeNumbersHaveWholeSteps() {
		Map<String, Object> counts = PaneCharts.config(data(dict("Draft", 2, "Done", 1.0)), "open");
		Map<String, Object> amounts = PaneCharts.config(data(dict("Draft", 2.5, "Done", 1)), "open");

		assertEquals(0, path((Map<?, ?>) counts.get("options"), "scales", "y", "ticks").get("precision"));
		assertNull(path((Map<?, ?>) amounts.get("options"), "scales", "y", "ticks").get("precision"));
	}

	private static ChartData data(Object result) {
		return ChartData.of(result, ChartKind.BAR, null, ALL);
	}

	private static Map<?, ?> dataset(Map<String, Object> config, int index) {
		return (Map<?, ?>) ((List<?>) ((Map<?, ?>) config.get("data")).get("datasets")).get(index);
	}

	private static Map<?, ?> path(Map<?, ?> map, String... keys) {
		Map<?, ?> result = map;
		for (String key : keys) {
			result = (Map<?, ?>) result.get(key);
		}
		return result;
	}

	private static Map<String, Object> dict(Object... keysAndValues) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (int n = 0; n < keysAndValues.length; n += 2) {
			result.put((String) keysAndValues[n], keysAndValues[n + 1]);
		}
		return result;
	}
}
