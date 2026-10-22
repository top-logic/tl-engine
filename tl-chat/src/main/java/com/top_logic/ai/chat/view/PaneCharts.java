/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.tl.ai.core.tools.present.ChartData;
import com.top_logic.tl.ai.core.tools.present.Presentation;

/**
 * The Chart.js configuration the pane's {@code <chart>} draws for a chart {@link Presentation}.
 *
 * <p>
 * Every chart arrives in one format, the Chart.js configuration of {@link ChartData} - a chart of
 * objects computed from its objects, a chart of data as the agent's query wrote it. This class only
 * adds what the pane wants of every chart, and only where the configuration says nothing itself: a
 * script that chose its colours, its legend or its axes keeps them.
 * </p>
 *
 * <ul>
 * <li>A point that stands for an object - its dataset's {@code metadata} - opens that object when
 * clicked. A point of computed data has no object, and its dataset no handler.</li>
 * <li>A legend where it names something: the slices of a pie, or more than one series.</li>
 * <li>Bars with rounded tops of a readable thickness.</li>
 * <li>Whole steps on the axes of a chart that holds only whole numbers: a count of four has no
 * 0.1.</li>
 * </ul>
 *
 * <p>
 * <b>Text and grid.</b> The engine resolves its chart colours on the server, from theme variables
 * that the dark theme does not override (the React dark theme is CSS alone), so its default label
 * colour {@code #374151} stands on a dark background unreadably. Until the engine's chart follows the
 * theme, the pane names one mid grey for text and a translucent one for the grid, both of which read
 * on either background.
 * </p>
 */
final class PaneCharts {

	/** Labels, ticks and legend: legible on the light and on the dark background alike. */
	private static final String TEXT = "#8b919c";

	/** Grid lines: present on both backgrounds without competing with the bars. */
	private static final String GRID = "rgba(139, 145, 156, 0.25)";

	/** The types whose data is drawn over an x and a y axis. */
	private static final Set<String> CARTESIAN = Set.of("bar", "line", "scatter", "bubble");

	/** The types whose data is drawn around a centre, on a radial scale. */
	private static final Set<String> RADIAL = Set.of("radar", "polarArea");

	/** The types whose legend names the points rather than the series. */
	private static final Set<String> SLICED = Set.of("pie", "doughnut", "polarArea");

	private PaneCharts() {
		// Static utility.
	}

	/**
	 * The configuration the pane draws.
	 *
	 * @param chart
	 *        What the presentation draws, see {@link Presentation#chartData()}.
	 * @param clickHandler
	 *        The name of the {@code <chart>} handler a click on a point of an object runs.
	 * @return The Chart.js configuration.
	 */
	static Map<String, Object> config(ChartData chart, String clickHandler) {
		Map<String, Object> config = chart.config();
		String type = chart.type();
		Map<String, Object> data = map(config, "data");
		List<?> datasets = (List<?>) data.get("datasets");
		for (Object element : datasets) {
			@SuppressWarnings("unchecked")
			Map<String, Object> dataset = (Map<String, Object>) element;
			if (dataset.get("metadata") instanceof List<?> objects && objects.stream().anyMatch(o -> o != null)) {
				dataset.put("onClick", clickHandler);
			}
			Object own = dataset.get("type");
			if ("bar".equals(own instanceof String name ? name : type)) {
				dataset.putIfAbsent("borderRadius", Integer.valueOf(4));
				dataset.putIfAbsent("maxBarThickness", Integer.valueOf(56));
			}
		}

		Map<String, Object> options = map(config, "options");
		options.putIfAbsent("responsive", Boolean.TRUE);
		options.putIfAbsent("maintainAspectRatio", Boolean.FALSE);

		Map<String, Object> plugins = map(options, "plugins");
		Map<String, Object> legend = map(plugins, "legend");
		legend.putIfAbsent("display", Boolean.valueOf(SLICED.contains(type) || datasets.size() > 1));
		legend.putIfAbsent("position", "bottom");
		map(legend, "labels").putIfAbsent("color", TEXT);
		if (plugins.get("title") instanceof Map<?, ?>) {
			map(plugins, "title").putIfAbsent("color", TEXT);
		}

		boolean whole = wholeNumbers(datasets);
		if (CARTESIAN.contains(type)) {
			Map<String, Object> scales = map(options, "scales");
			map(scales, "x");
			map(scales, "y");
			for (String axis : scales.keySet()) {
				Map<String, Object> scale = map(scales, axis);
				if (!"scatter".equals(type) && !"bubble".equals(type)) {
					scale.putIfAbsent("beginAtZero", Boolean.TRUE);
				}
				Map<String, Object> ticks = map(scale, "ticks");
				ticks.putIfAbsent("color", TEXT);
				if (whole) {
					// Ignored by an axis of categories, so it may stand on every axis.
					ticks.putIfAbsent("precision", Integer.valueOf(0));
				}
				map(scale, "grid").putIfAbsent("color", GRID);
				if (scale.get("title") instanceof Map<?, ?>) {
					map(scale, "title").putIfAbsent("color", TEXT);
				}
			}
		} else if (RADIAL.contains(type)) {
			Map<String, Object> scale = map(map(options, "scales"), "r");
			Map<String, Object> ticks = map(scale, "ticks");
			ticks.putIfAbsent("color", TEXT);
			if (whole) {
				ticks.putIfAbsent("precision", Integer.valueOf(0));
			}
			ticks.putIfAbsent("backdropColor", "transparent");
			map(scale, "grid").putIfAbsent("color", GRID);
			map(scale, "angleLines").putIfAbsent("color", GRID);
			map(scale, "pointLabels").putIfAbsent("color", TEXT);
		}
		return config;
	}

	/**
	 * Whether every value of the datasets is a whole number, or a gap.
	 */
	private static boolean wholeNumbers(List<?> datasets) {
		for (Object dataset : datasets) {
			if (!(((Map<?, ?>) dataset).get("data") instanceof List<?> values)) {
				return false;
			}
			for (Object value : values) {
				if (value != null && !(value instanceof Number number
					&& number.doubleValue() == Math.rint(number.doubleValue()))) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * The dictionary under the given key, put there when there is none.
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> map(Map<String, Object> parent, String key) {
		Object value = parent.get(key);
		if (value instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}
		Map<String, Object> created = new LinkedHashMap<>();
		parent.put(key, created);
		return created;
	}
}
