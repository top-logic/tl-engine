/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import static com.top_logic.presentation.data.PresentationData.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.presentation.data.PresentationData;

/**
 * Reads the charts of a presentation description: {@link PresentationData#TYPE_BAR_CHART} with
 * horizontal bars and {@link PresentationData#TYPE_COLUMN_CHART} with vertical columns.
 *
 * <p>
 * A chart is a dictionary with the entries {@link PresentationData#CATEGORIES} (a non-empty list
 * of labels), {@link PresentationData#SERIES} (a list of data series), {@link PresentationData#STACKED},
 * {@link PresentationData#LABELS}, {@link PresentationData#LEGEND} (a {@link ChartLegend}),
 * {@link PresentationData#MAX}, {@link PresentationData#GAP}, {@link PresentationData#FORMAT},
 * {@link PresentationData#TITLE} and the placement entries {@link PresentationData#X},
 * {@link PresentationData#Y}, {@link PresentationData#WIDTH}, {@link PresentationData#HEIGHT} and
 * {@link PresentationData#WEIGHT}.
 * </p>
 *
 * <p>
 * A data series is a dictionary with the entries {@link PresentationData#NAME},
 * {@link PresentationData#VALUES} (a number or nothing for each category) and
 * {@link PresentationData#ROLE}, or a list of these values in this order. A series without role
 * gets the topic role of its position: {@link PresentationData#ROLE_TOPIC_A} for the first one,
 * {@link PresentationData#ROLE_TOPIC_B} for the second one, and so on.
 * </p>
 */
public final class ChartReader {

	/**
	 * The distance between the categories in percent of the width of a bar, if not given.
	 */
	public static final int DEFAULT_GAP = 60;

	/**
	 * The largest distance between the categories in percent of the width of a bar.
	 */
	public static final int MAX_GAP = 500;

	/**
	 * The roles of the series without role, by their position.
	 */
	private static final List<String> SERIES_ROLES =
		List.of(ROLE_TOPIC_A, ROLE_TOPIC_B, ROLE_TOPIC_C, ROLE_TOPIC_D, ROLE_TOPIC_E, ROLE_TOPIC_F);

	private ChartReader() {
		// Static utility.
	}

	/**
	 * Registers the readers of the charts by their type names.
	 */
	static void register(Map<String, ElementReader> types) {
		types.put(TYPE_BAR_CHART, entries -> readChart(entries, true));
		types.put(TYPE_COLUMN_CHART, entries -> readChart(entries, false));
	}

	/**
	 * Reads a chart.
	 *
	 * <p>
	 * By default, the values are shown at the bars ({@link PresentationData#LABELS}), and a chart
	 * with several series has a legend below the plot area.
	 * </p>
	 */
	private static ChartSpec readChart(Entries entries, boolean horizontal) {
		Bounds bounds = DescriptionReader.readBounds(entries);
		Value categoryValues = entries.require(CATEGORIES, I18NConstants.EXPECTED_CATEGORIES);
		if (!categoryValues.isList() || categoryValues.asList().isEmpty()) {
			throw categoryValues.invalid(I18NConstants.EXPECTED_CATEGORIES);
		}
		List<String> categories = new ArrayList<>();
		for (Value category : categoryValues.asList()) {
			categories.add(DescriptionReader.text(category.require(I18NConstants.EXPECTED_TEXT)));
		}
		List<Value> seriesValues = entries.require(SERIES, I18NConstants.EXPECTED_LIST).asListOrSingle();
		List<ChartSpec.Series> series = new ArrayList<>(seriesValues.size());
		for (Value seriesValue : seriesValues) {
			series.add(readSeries(seriesValue, categories.size(), SERIES_ROLES.get(series.size() % SERIES_ROLES.size())));
		}
		Boolean stacked = entries.get(STACKED).optBoolean();
		Boolean labels = entries.get(LABELS).optBoolean();
		ChartLegend legend = entries.get(LEGEND).optEnum(ChartLegend.class);
		Double max = entries.get(MAX).optNumber();
		Integer gap = entries.get(GAP).optInteger(0, MAX_GAP);
		String format = entries.get(FORMAT).optString();
		Value titleValue = entries.get(TITLE);
		String title = titleValue.isAbsent() ? null : DescriptionReader.text(titleValue);
		return new ChartSpec(bounds, horizontal, Collections.unmodifiableList(categories),
			Collections.unmodifiableList(series), stacked != null && stacked.booleanValue(),
			labels == null || labels.booleanValue(),
			legend != null ? legend : series.size() > 1 ? ChartLegend.BOTTOM : ChartLegend.NONE,
			max, gap == null ? DEFAULT_GAP : gap.intValue(), format, title);
	}

	private static ChartSpec.Series readSeries(Value series, int categoryCount, String defaultRole) {
		Value name;
		Value values;
		Value role;
		Entries entries = null;
		if (series.isList()) {
			List<Value> parts = series.asList();
			if (parts.size() < 2 || parts.size() > 3) {
				throw series.invalid(I18NConstants.EXPECTED_SERIES);
			}
			name = parts.get(0);
			values = parts.get(1);
			role = parts.size() > 2 ? parts.get(2) : new Value(null, series.path() + "[2]");
		} else if (series.isMap()) {
			entries = series.asEntries();
			name = entries.get(NAME);
			values = entries.require(VALUES, I18NConstants.EXPECTED_VALUES__COUNT.fill(categoryCount));
			role = entries.get(ROLE);
		} else {
			throw series.invalid(I18NConstants.EXPECTED_SERIES);
		}
		String seriesName = name.isAbsent() ? "" : DescriptionReader.text(name);
		List<Double> numbers = readValues(values, categoryCount);
		String seriesRole = DescriptionReader.readRole(role);
		if (entries != null) {
			entries.checkComplete();
		}
		return new ChartSpec.Series(seriesName, numbers, seriesRole == null ? defaultRole : seriesRole);
	}

	private static List<Double> readValues(Value values, int categoryCount) {
		if (!values.isList() || values.asList().size() != categoryCount) {
			throw values.invalid(I18NConstants.EXPECTED_VALUES__COUNT.fill(categoryCount));
		}
		List<Double> result = new ArrayList<>(categoryCount);
		for (Value value : values.asList()) {
			result.add(value.optNumber());
		}
		return Collections.unmodifiableList(result);
	}

}
