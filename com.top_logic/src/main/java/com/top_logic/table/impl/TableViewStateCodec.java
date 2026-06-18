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

import com.top_logic.table.GroupSpec;
import com.top_logic.table.SortColumn;
import com.top_logic.table.TableViewState;

/**
 * Converts the fully-serializable subset of a {@link TableViewState} to and from a plain JSON
 * value model (nested {@link Map}/{@link List}/{@link String}/{@link Integer}/{@link Boolean}).
 *
 * <p>
 * Persisted are the parts of the view state that are pure value data: column order, per-column
 * widths, the frozen column count, the multi-column sort, and the grouping. Filters, expansion
 * and selection are <em>not</em> persisted here - their state can carry arbitrary business-object
 * values ({@code TLObject} classifiers, row keys) whose stable cross-session serialization needs a
 * separate identity strategy.
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

	private TableViewStateCodec() {
		// Utility class.
	}

	/**
	 * Serializes the persistable subset of the given state into a JSON value map.
	 */
	public static Map<String, Object> toJson(TableViewState state) {
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
		return json;
	}

	/**
	 * Applies a previously {@link #toJson(TableViewState) serialized} JSON value map onto the given
	 * state. Unknown or malformed entries are ignored, so a stored state from an older column set
	 * loads without error.
	 */
	public static void readInto(TableViewState target, Map<?, ?> json) {
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
