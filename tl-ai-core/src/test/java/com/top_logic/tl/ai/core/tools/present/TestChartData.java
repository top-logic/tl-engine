/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.present;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;

/**
 * How {@link ChartData} turns what a query computed into the one Chart.js configuration a chart
 * draws, and what it refuses.
 *
 * <p>
 * The values here are what TL-Script hands over - dictionaries in insertion order, lists, numbers -
 * built in Java, because the forms and the refusals are the subject, not the language. That a query
 * really produces them, and that the tool answers with these messages, is {@code TestPresentTool}'s
 * (tl-chat).
 * </p>
 */
public class TestChartData extends TestCase {

	private static final ReadFilter ALL = ReadFilter.forSecurity(ModelSecurity.UNCHECKED);

	/** What {@code groupBy(key, g -> $g.size())} returns is one series, named by the title. */
	public void testADictionaryOfNumbersIsOneSeries() {
		ChartData chart = of(dict("Draft", 2, "Planned", 5, "Done", 1), "Events per status");

		assertEquals("bar", chart.type());
		assertEquals(List.of("Draft", "Planned", "Done"), chart.labels());
		assertEquals(List.of(new ChartData.Series("Events per status", List.of(2, 5, 1))), chart.series());
		assertTrue(chart.isTabular());
		assertNull("Nothing was cut.", chart.cut());
	}

	/**
	 * A dictionary of dictionaries - {@code groupBy(key, g -> {"count": ..., "sum": ...})} - is one
	 * series per inner key, in the order the keys first occur; a key a group lacks is a gap.
	 */
	public void testADictionaryOfDictionariesIsOneSeriesPerKey() {
		ChartData chart = of(dict(
			"Draft", dict("count", 2, "sum", 300.5),
			"Planned", dict("count", 5),
			"Done", dict("sum", 10, "count", 1)), null);

		assertEquals(List.of("Draft", "Planned", "Done"), chart.labels());
		List<ChartData.Series> series = chart.series();
		assertEquals(2, series.size());
		assertEquals(new ChartData.Series("count", List.of(2, 5, 1)), series.get(0));
		assertEquals(new ChartData.Series("sum", Arrays.asList(300.5, null, 10)), series.get(1));
	}

	/**
	 * A list of records is one series per numeric key; the label is the key {@code label}, or the
	 * record's first value that is not a number.
	 */
	public void testRecordsAreOneSeriesPerNumericKey() {
		ChartData named = of(List.of(
			dict("planned", 5, "label", "Jan", "actual", 3),
			dict("planned", 4, "label", "Feb", "actual", 6)), null);
		assertEquals(List.of("Jan", "Feb"), named.labels());
		assertEquals(List.of(new ChartData.Series("planned", List.of(5, 4)),
			new ChartData.Series("actual", List.of(3, 6))), named.series());

		ChartData unnamed = of(List.of(dict("month", "Jan", "orders", 7), dict("month", "Feb", "orders", 9)), null);
		assertEquals(List.of("Jan", "Feb"), unnamed.labels());
		assertEquals(List.of(new ChartData.Series("orders", List.of(7, 9))), unnamed.series());
	}

	/**
	 * A whole Chart.js configuration, as a chart component's script returns it, is taken as
	 * written - a line over stacked bars, its options - except for the handler names a model does
	 * not get to choose.
	 */
	public void testAConfigurationIsTakenAsWritten() {
		Map<String, Object> config = dict(
			"type", "bar",
			"data", dict(
				"labels", List.of("Q1", "Q2"),
				"datasets", List.of(
					dict("label", "A", "data", List.of(1, 2), "stack", "s", "onClick", "delete"),
					dict("label", "B", "data", List.of(3, 4), "stack", "s", "tooltip", "secret"),
					dict("label", "Total", "data", List.of(4, 6), "type", "line"))),
			"options", dict("scales", dict("x", dict("stacked", true), "y", dict("stacked", true))));

		ChartData chart = of(config, "ignored for a configuration");

		Map<String, Object> shown = chart.config();
		assertEquals(dict("scales", dict("x", dict("stacked", true), "y", dict("stacked", true))), shown.get("options"));
		List<?> datasets = (List<?>) ((Map<?, ?>) shown.get("data")).get("datasets");
		assertEquals(dict("label", "A", "data", List.of(1, 2), "stack", "s"), datasets.get(0));
		assertEquals(dict("label", "B", "data", List.of(3, 4), "stack", "s"), datasets.get(1));
		assertEquals("line", ((Map<?, ?>) datasets.get(2)).get("type"));
		assertEquals(List.of("Q1", "Q2"), chart.labels());
		assertTrue(chart.isTabular());
	}

	/** A chart whose points carry their own coordinates draws, but reads as no table. */
	public void testPointsWithCoordinatesAreNoTable() {
		ChartData chart = of(dict("type", "scatter", "data", dict("datasets", List.of(
			dict("label", "Orders", "data", List.of(dict("x", 1, "y", 2), dict("x", 3, "y", 1)))))), null);

		assertEquals("scatter", chart.type());
		assertFalse(chart.isTabular());
	}

	/** A value that is not a number is refused, naming the label it belongs to. */
	public void testANonNumericValueIsRefused() {
		assertRefused("The value for 'Planned' is the text 'many', not a number", dict("Draft", 1, "Planned", "many"));
		assertRefused("Compute the number in the query, e.g. groupBy(x -> ..., g -> $g.size())",
			dict("Draft", List.of("a", "b")));
		assertRefused("record 'Feb': 'orders' is the text 'nine', which is not a number",
			List.of(dict("month", "Jan", "orders", 7), dict("month", "Feb", "orders", "nine")));
		assertRefused("Dataset 1, point 2: the text 'x' is not a number", dict("type", "bar",
			"data", dict("labels", List.of("a", "b"), "datasets", List.of(dict("data", List.of(1, "x"))))));
	}

	/** A result whose parts do not agree on their shape is refused rather than guessed at. */
	public void testMixedShapesAreRefused() {
		assertRefused("mixes numbers and dictionaries", dict("Draft", 1, "Planned", dict("count", 2)));
		assertRefused("Record 2 has no 'label'", List.of(dict("label", "Jan", "n", 1), dict("name", "Feb", "n", 2)));
		assertRefused("this one holds only numbers", List.of(dict("planned", 5, "actual", 3)));
		assertFalse("A list of numbers is not data.", ChartData.isData(List.of(1, 2)));
		assertFalse("A list mixing records with other values is not data.",
			ChartData.isData(List.of(dict("label", "Jan", "n", 1), "Feb")));
	}

	/** A configuration names a type this chart draws, and has datasets. */
	public void testABrokenConfigurationIsRefused() {
		assertRefused("The chart's 'type' is 'funnel', which this chart cannot draw",
			dict("type", "funnel", "data", dict("datasets", List.of(dict("data", List.of(1))))));
		assertRefused("Dataset 1's 'type' is 'gauge'",
			dict("type", "bar", "data", dict("datasets", List.of(dict("type", "gauge", "data", List.of(1))))));
		assertRefused("has no 'datasets'", dict("type", "bar", "data", dict("labels", List.of("a"))));
	}

	/**
	 * Too many points are cut, and the chart says so: a part shown as the whole would be a chart that
	 * lies.
	 */
	public void testTooManyPointsAreCutAndSaidSo() {
		Map<String, Object> many = new LinkedHashMap<>();
		for (int n = 0; n < ChartData.MAX_POINTS + 50; n++) {
			many.put("P" + n, n);
		}
		ChartData chart = of(many, null);

		assertEquals(ChartData.MAX_POINTS, chart.labels().size());
		assertEquals(ChartData.MAX_POINTS, chart.series().get(0).values().size());
		assertEquals("Only the first 200 of 250 points per series are shown. Tell the user, or group the data more"
			+ " coarsely.", chart.cut());
	}

	/** Too many series are cut the same way. */
	public void testTooManySeriesAreCutAndSaidSo() {
		List<Object> datasets = new ArrayList<>();
		for (int n = 0; n < ChartData.MAX_SERIES + 5; n++) {
			datasets.add(dict("label", "S" + n, "data", List.of(n)));
		}
		ChartData chart = of(dict("type", "line", "data", dict("labels", List.of("x"), "datasets", datasets)), null);

		assertEquals(ChartData.MAX_SERIES, chart.series().size());
		assertTrue(chart.cut(), chart.cut().contains("the first 20 of 25 series"));
	}

	/** A division by zero is a gap: JSON has no NaN, and the page must not break over one. */
	public void testNotANumberIsAGap() {
		ChartData chart = of(dict("type", "bar", "data", dict("labels", List.of("a", "b"),
			"datasets", List.of(dict("data", List.of(1, Double.NaN))))), null);

		assertEquals(Arrays.asList(1, null), chart.series().get(0).values());
	}

	/** What the caller gets is a copy: a page adding its look changes nothing the next call sees. */
	@SuppressWarnings("unchecked")
	public void testTheConfigurationIsACopy() {
		ChartData chart = of(dict("A", 1), null);
		Map<String, Object> first = chart.config();
		((Map<String, Object>) first.get("data")).put("labels", List.of("changed"));
		first.put("options", dict("responsive", false));

		assertEquals(List.of("A"), chart.labels());
		assertFalse(chart.config().containsKey("options"));
	}

	private static ChartData of(Object result, String seriesName) {
		assertTrue(ChartData.isData(result));
		return ChartData.of(result, ChartKind.BAR, seriesName, ALL);
	}

	private static void assertRefused(String expected, Object result) {
		try {
			ChartData.of(result, ChartKind.BAR, null, ALL);
			fail("Expected a refusal saying '" + expected + "'.");
		} catch (IllegalArgumentException ex) {
			assertTrue("Expected '" + expected + "' in: " + ex.getMessage(), ex.getMessage().contains(expected));
		}
	}

	/** A dictionary in insertion order, as TL-Script builds one. */
	private static Map<String, Object> dict(Object... keysAndValues) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (int n = 0; n < keysAndValues.length; n += 2) {
			result.put((String) keysAndValues[n], keysAndValues[n + 1]);
		}
		return result;
	}
}
