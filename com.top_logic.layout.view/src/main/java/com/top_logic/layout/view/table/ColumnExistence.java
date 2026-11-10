/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.channel.ChannelCondition;
import com.top_logic.table.TableView;

/**
 * Which of the columns of a table currently exist, as the {@link ColumnSetup#exists() conditions}
 * of their declarations decide.
 *
 * <p>
 * A column whose condition does not hold does not exist for the table: it is
 * {@link TableView#setAbsentColumns(Set) absent} - neither displayed nor offered in the column
 * selection - while the user's arrangement of it is kept. The conditions are followed for as long
 * as the table is displayed, so that a column appears and disappears with the values of the
 * channels its condition reads, without the table being built again.
 * </p>
 */
public final class ColumnExistence {

	/** The conditions of the columns that do not always exist, by column name. */
	private final Map<String, ChannelCondition> _conditions;

	private ColumnExistence(Map<String, ChannelCondition> conditions) {
		_conditions = conditions;
	}

	/**
	 * The existence of the given columns.
	 */
	public static ColumnExistence of(List<ColumnSetup> setups) {
		Map<String, ChannelCondition> conditions = new LinkedHashMap<>();
		for (ColumnSetup setup : setups) {
			ChannelCondition exists = setup.exists();
			if (exists != null && exists != ChannelCondition.ALWAYS) {
				conditions.put(setup.name(), exists);
			}
		}
		return new ColumnExistence(conditions);
	}

	/**
	 * Whether every column always exists, so that there is nothing to follow.
	 */
	public boolean isEmpty() {
		return _conditions.isEmpty();
	}

	/**
	 * The names of the columns that do not exist for the current values of the channels their
	 * conditions read.
	 */
	public Set<String> absentColumns() {
		Set<String> result = new LinkedHashSet<>();
		for (Map.Entry<String, ChannelCondition> entry : _conditions.entrySet()) {
			if (!entry.getValue().holds()) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	/**
	 * Follows the conditions: hands the {@link #absentColumns() absent columns} to the given
	 * consumer whenever one of the channels the conditions read takes a new value.
	 *
	 * @param absentColumns
	 *        Takes up the absent columns, e.g. by
	 *        {@link com.top_logic.layout.react.control.table.TableViewControl#setAbsentColumns(Set)}.
	 * @return The registration ending the observation, to be disposed with the table.
	 */
	public Registration observe(Consumer<Set<String>> absentColumns) {
		if (isEmpty()) {
			return Registration.NONE;
		}
		return ChannelCondition.observe(_conditions.values(), () -> absentColumns.accept(absentColumns()));
	}

}
