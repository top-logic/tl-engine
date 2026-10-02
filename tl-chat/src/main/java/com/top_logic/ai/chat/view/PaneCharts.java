/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.tools.present.ChartKind;
import com.top_logic.tl.ai.core.tools.present.Presentation;

/**
 * The Chart.js configuration the pane's {@code <chart>} draws for a chart {@link Presentation}.
 *
 * <p>
 * One dataset, one point per object: the label and value the agent's functions computed, and the
 * object itself as the point's metadata, so that a click on a bar, a point or a slice opens that
 * object. Colours, grid and text are left to the engine's chart, which takes them from the theme -
 * a configuration that named its own would look wrong in one of the two.
 * </p>
 */
final class PaneCharts {

	private PaneCharts() {
		// Static utility.
	}

	/**
	 * Computes the configuration from the objects as they are now.
	 *
	 * @param presentation
	 *        A presentation {@link com.top_logic.tl.ai.core.tools.present.PresentAs#CHART as a
	 *        chart}.
	 * @param clickHandler
	 *        The name of the {@code <chart>} handler a click on a point runs.
	 * @return The Chart.js configuration.
	 * @throws IllegalArgumentException
	 *         If a function of the presentation fails on an object; see
	 *         {@link Presentation#points()}.
	 */
	static Map<String, Object> config(Presentation presentation, String clickHandler) {
		List<Presentation.Point> points = presentation.points();
		List<Object> labels = new ArrayList<>(points.size());
		List<Object> values = new ArrayList<>(points.size());
		List<TLObject> objects = new ArrayList<>(points.size());
		for (Presentation.Point point : points) {
			labels.add(point.label());
			values.add(point.value());
			objects.add(point.object());
		}
		ChartKind kind = presentation.chart().kind();

		Map<String, Object> dataset = new LinkedHashMap<>();
		dataset.put("label", presentation.title() == null ? "" : presentation.title());
		dataset.put("data", values);
		dataset.put("metadata", objects);
		dataset.put("onClick", clickHandler);
		if (kind == ChartKind.BAR) {
			dataset.put("borderRadius", Integer.valueOf(4));
			dataset.put("maxBarThickness", Integer.valueOf(56));
		}

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("labels", labels);
		data.put("datasets", List.of(dataset));

		Map<String, Object> legend = new LinkedHashMap<>();
		// One dataset needs no legend - except a pie, whose legend names the slices.
		legend.put("display", Boolean.valueOf(kind == ChartKind.PIE));
		legend.put("position", "bottom");
		Map<String, Object> plugins = new LinkedHashMap<>();
		plugins.put("legend", legend);

		Map<String, Object> options = new LinkedHashMap<>();
		options.put("responsive", Boolean.TRUE);
		options.put("maintainAspectRatio", Boolean.FALSE);
		options.put("plugins", plugins);
		if (kind != ChartKind.PIE) {
			Map<String, Object> y = new LinkedHashMap<>();
			y.put("beginAtZero", Boolean.TRUE);
			Map<String, Object> scales = new LinkedHashMap<>();
			scales.put("y", y);
			options.put("scales", scales);
		}

		Map<String, Object> config = new LinkedHashMap<>();
		config.put("type", kind.chartJsType());
		config.put("data", data);
		config.put("options", options);
		return config;
	}
}
