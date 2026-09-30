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

import com.top_logic.basic.util.ResKey;
import com.top_logic.table.FilterCodec;
import com.top_logic.table.FilterState;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;

/**
 * Converts the {@link NamedFilter}s a user saved for a table to and from a plain JSON value model
 * (nested {@link Map}/{@link List}/{@link String}/{@link Boolean}).
 *
 * <p>
 * A filter is stored as its identifier, its {@link ResKey#encode(ResKey) encoded} label, the JSON
 * of each column state as produced by the caller-supplied {@link FilterCodec}, and the search term
 * in the {@link TextColumnFilter#textToJson(TextFilterState) shape of a text pattern}. Reading is
 * lenient: an entry that does not parse - a malformed record, or a column state the codec cannot
 * restore because the table no longer has that column - is skipped as a whole, since a filter
 * missing one of its criteria would select other rows than the one the user saved.
 * </p>
 *
 * <p>
 * Like {@link TableViewStateCodec} the class is toolkit-neutral: it produces and consumes only
 * standard JSON value types, so a {@code NamedFilterStore} can hand the result to any JSON-based
 * personalization backend. All filters it produces are {@link NamedFilter.Origin#SAVED} ones,
 * which are the only ones stored per user.
 * </p>
 */
public final class NamedFilterCodec {

	/** Key of {@link NamedFilter#id()}. */
	private static final String ID = "id";

	/** Key of the {@link ResKey#encode(ResKey) encoded} {@link NamedFilter#label()}. */
	private static final String LABEL = "label";

	/** Key of {@link NamedFilter#filters()}, a map from column name to the column's state. */
	private static final String FILTERS = "filters";

	/** Key of {@link NamedFilter#search()}. */
	private static final String SEARCH = "search";

	private NamedFilterCodec() {
		// Utility class.
	}

	/**
	 * Serializes the given filters into a JSON value list.
	 *
	 * <p>
	 * A filter holding a column state the given codec cannot serialize is left out, so that
	 * reading back never yields a filter with fewer criteria than it was saved with.
	 * </p>
	 *
	 * @param filters
	 *        The filters to serialize.
	 * @param codec
	 *        The column-aware codec serializing the column states.
	 */
	public static List<Object> toJson(List<NamedFilter> filters, FilterCodec codec) {
		List<Object> result = new ArrayList<>(filters.size());
		for (NamedFilter filter : filters) {
			Map<String, Object> json = toJson(filter, codec);
			if (json != null) {
				result.add(json);
			}
		}
		return result;
	}

	private static Map<String, Object> toJson(NamedFilter filter, FilterCodec codec) {
		Map<String, Object> states = new LinkedHashMap<>();
		for (Map.Entry<String, FilterState> entry : filter.filters().entrySet()) {
			Object state = codec.toJson(entry.getKey(), entry.getValue());
			if (state == null) {
				return null;
			}
			states.put(entry.getKey(), state);
		}
		Map<String, Object> json = new LinkedHashMap<>();
		json.put(ID, filter.id());
		json.put(LABEL, ResKey.encode(filter.label()));
		json.put(FILTERS, states);
		if (filter.search() != null) {
			json.put(SEARCH, TextColumnFilter.textToJson(filter.search()));
		}
		return json;
	}

	/**
	 * The filters of a value previously produced by {@link #toJson(List, FilterCodec)}, skipping
	 * every entry that does not parse.
	 *
	 * @param json
	 *        The stored value, {@code null} for nothing stored.
	 * @param codec
	 *        The column-aware codec restoring the column states.
	 * @return The restored filters, empty if the value holds none.
	 */
	public static List<NamedFilter> read(Object json, FilterCodec codec) {
		List<NamedFilter> result = new ArrayList<>();
		if (!(json instanceof List<?> entries)) {
			return result;
		}
		for (Object entry : entries) {
			NamedFilter filter = readFilter(entry, codec);
			if (filter != null) {
				result.add(filter);
			}
		}
		return result;
	}

	private static NamedFilter readFilter(Object json, FilterCodec codec) {
		if (!(json instanceof Map<?, ?> map)) {
			return null;
		}
		Object id = map.get(ID);
		Object label = map.get(LABEL);
		if (id == null || label == null) {
			return null;
		}
		ResKey name;
		try {
			name = ResKey.decode(String.valueOf(label));
		} catch (RuntimeException ex) {
			return null;
		}
		Map<String, FilterState> states = new LinkedHashMap<>();
		if (map.get(FILTERS) instanceof Map<?, ?> stored) {
			for (Map.Entry<?, ?> entry : stored.entrySet()) {
				String column = String.valueOf(entry.getKey());
				FilterState state = codec.fromJson(column, entry.getValue());
				if (state == null) {
					return null;
				}
				states.put(column, state);
			}
		}
		return new NamedFilter(String.valueOf(id), name, NamedFilter.Origin.SAVED, states,
			TextColumnFilter.textFromJson(map.get(SEARCH)));
	}

}
