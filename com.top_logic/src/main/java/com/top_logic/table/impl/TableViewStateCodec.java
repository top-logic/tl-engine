/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.table.FilterCodec;
import com.top_logic.table.FilterState;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.SortColumn;
import com.top_logic.table.TableViewState;

/**
 * Converts the fully-serializable subset of a {@link TableViewState} to and from a plain JSON
 * value model (nested {@link Map}/{@link List}/{@link String}/{@link Integer}/{@link Boolean}).
 *
 * <p>
 * Persisted are the parts of the view state that are pure value data: column order, per-column
 * widths, the frozen column count, the multi-column sort, and the grouping. Column filters are
 * persisted too, but only through a caller-supplied {@link FilterCodec}: their state can carry
 * arbitrary business-object values ({@code TLObject} classifiers, row keys) whose stable
 * cross-session serialization is the owning column's responsibility. Without a codec
 * ({@link FilterCodec#NONE}) filters are skipped. Expansion and selection are not persisted.
 * </p>
 *
 * <p>
 * The class is toolkit-neutral: it produces and consumes only standard JSON value types, so a
 * {@code ViewStateStore} can hand the result straight to any JSON-based personalization backend.
 * </p>
 */
public final class TableViewStateCodec {

	private static final String COLUMN_ORDER = "columnOrder";

	private static final String WIDTHS = "widths";

	private static final String FROZEN_COUNT = "frozenCount";

	private static final String SORT = "sort";

	private static final String SORT_COLUMN = "column";

	private static final String SORT_ASCENDING = "ascending";

	private static final String GROUPING = "grouping";

	private static final String FILTERS = "filters";

	private TableViewStateCodec() {
		// Utility class.
	}

	/**
	 * Serializes the persistable subset of the given state into a JSON value map, without filters.
	 */
	public static Map<String, Object> toJson(TableViewState state) {
		return toJson(state, FilterCodec.NONE);
	}

	/**
	 * Serializes the persistable subset of the given state into a JSON value map, persisting each
	 * column filter the given {@link FilterCodec} can serialize.
	 */
	public static Map<String, Object> toJson(TableViewState state, FilterCodec filters) {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put(COLUMN_ORDER, new ArrayList<>(state.getColumnOrder()));

		Map<String, Object> widths = new LinkedHashMap<>();
		for (Map.Entry<String, Integer> entry : state.getWidths().entrySet()) {
			widths.put(entry.getKey(), entry.getValue());
		}
		json.put(WIDTHS, widths);

		json.put(FROZEN_COUNT, Integer.valueOf(state.getFrozenCount()));

		List<Object> sort = new ArrayList<>();
		for (SortColumn sortColumn : state.getSort()) {
			Map<String, Object> step = new LinkedHashMap<>();
			step.put(SORT_COLUMN, sortColumn.column());
			step.put(SORT_ASCENDING, Boolean.valueOf(sortColumn.ascending()));
			sort.add(step);
		}
		json.put(SORT, sort);

		json.put(GROUPING, new ArrayList<>(state.getGrouping().columns()));

		Map<String, Object> filterJson = new LinkedHashMap<>();
		for (Map.Entry<String, FilterState> entry : state.getFilters().entrySet()) {
			Object serialized = filters.toJson(entry.getKey(), entry.getValue());
			if (serialized != null) {
				filterJson.put(entry.getKey(), serialized);
			}
		}
		if (!filterJson.isEmpty()) {
			json.put(FILTERS, filterJson);
		}
		return json;
	}

	/**
	 * Applies a previously {@link #toJson(TableViewState) serialized} JSON value map onto the given
	 * state, without restoring filters.
	 */
	public static void readInto(TableViewState target, Map<?, ?> json) {
		readInto(target, json, FilterCodec.NONE);
	}

	/**
	 * Applies a previously {@link #toJson(TableViewState, FilterCodec) serialized} JSON value map
	 * onto the given state, restoring each column filter the given {@link FilterCodec} can read.
	 * Unknown or malformed entries are ignored, so a stored state from an older column set loads
	 * without error.
	 */
	public static void readInto(TableViewState target, Map<?, ?> json, FilterCodec filters) {
		Object order = json.get(COLUMN_ORDER);
		if (order instanceof List<?> list) {
			target.setColumnOrder(strings(list));
		}

		Object widths = json.get(WIDTHS);
		if (widths instanceof Map<?, ?> map) {
			Map<String, Integer> result = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				Integer width = asInt(entry.getValue());
				if (width != null) {
					result.put(String.valueOf(entry.getKey()), width);
				}
			}
			target.setWidths(result);
		}

		Integer frozen = asInt(json.get(FROZEN_COUNT));
		if (frozen != null) {
			target.setFrozenCount(Math.max(0, frozen.intValue()));
		}

		Object sort = json.get(SORT);
		if (sort instanceof List<?> list) {
			List<SortColumn> result = new ArrayList<>();
			for (Object element : list) {
				if (element instanceof Map<?, ?> step) {
					Object column = step.get(SORT_COLUMN);
					if (column != null) {
						result.add(new SortColumn(String.valueOf(column), asBool(step.get(SORT_ASCENDING))));
					}
				}
			}
			target.setSort(result);
		}

		Object grouping = json.get(GROUPING);
		if (grouping instanceof List<?> list) {
			target.setGrouping(new GroupSpec(strings(list)));
		}

		Object filterJson = json.get(FILTERS);
		if (filterJson instanceof Map<?, ?> map) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				FilterState state = filters.fromJson(String.valueOf(entry.getKey()), entry.getValue());
				if (state != null && !state.isEmpty()) {
					target.getFilters().put(String.valueOf(entry.getKey()), state);
				}
			}
		}
	}

	private static List<String> strings(List<?> list) {
		List<String> result = new ArrayList<>(list.size());
		for (Object element : list) {
			if (element != null) {
				result.add(String.valueOf(element));
			}
		}
		return result;
	}

	private static Integer asInt(Object value) {
		if (value instanceof Number number) {
			return Integer.valueOf(number.intValue());
		}
		if (value instanceof String text) {
			try {
				return Integer.valueOf(text.trim());
			} catch (NumberFormatException ex) {
				return null;
			}
		}
		return null;
	}

	private static boolean asBool(Object value) {
		if (value instanceof Boolean bool) {
			return bool.booleanValue();
		}
		return value != null && Boolean.parseBoolean(String.valueOf(value));
	}

}
