/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
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
 * of their declarations and the {@link ColumnSetup#released() access rights} of the user decide.
 *
 * <p>
 * A column whose condition does not hold does not exist for the table: it is
 * {@link TableView#setAbsentColumns(Set) absent} - neither displayed nor offered in the column
 * selection - while the user's arrangement of it is kept. A column the access rights of the user
 * do not release is absent as well, and in addition {@link #withheldColumns() withheld}: nothing
 * the table offers may refer to it.
 * </p>
 *
 * <p>
 * The conditions are followed for as long as the table is displayed, so that a column appears and
 * disappears with the values of the channels its conditions read, without the table being built
 * again. A condition may depend on state no channel announces as well, such as the access rights
 * of the user: the table {@link #recheck() asks the conditions again} whenever it is updated.
 * </p>
 */
public final class ColumnExistence {

	/**
	 * The conditions of a column that does not always exist.
	 *
	 * @param exists
	 *        See {@link ColumnSetup#exists()}.
	 * @param released
	 *        See {@link ColumnSetup#released()}.
	 */
	private record Conditions(ChannelCondition exists, ChannelCondition released) {
		// Pure data.
	}

	/** The conditions of the columns that do not always exist, by column name. */
	private final Map<String, Conditions> _conditions;

	/** The columns that do not exist, as last determined. */
	private Set<String> _absentColumns;

	/** The columns withheld from the user, as last determined. */
	private Set<String> _withheldColumns;

	/** Takes up a change of {@link #_absentColumns}, {@code null} while nobody follows them. */
	private Consumer<Set<String>> _consumer;

	private ColumnExistence(Map<String, Conditions> conditions) {
		_conditions = conditions;
		determine();
	}

	/**
	 * The existence of the given columns, determined for the current state.
	 */
	public static ColumnExistence of(List<ColumnSetup> setups) {
		Map<String, Conditions> conditions = new LinkedHashMap<>();
		for (ColumnSetup setup : setups) {
			ChannelCondition exists = nonNull(setup.exists());
			ChannelCondition released = nonNull(setup.released());
			if (exists != ChannelCondition.ALWAYS || released != ChannelCondition.ALWAYS) {
				conditions.put(setup.name(), new Conditions(exists, released));
			}
		}
		return new ColumnExistence(conditions);
	}

	private static ChannelCondition nonNull(ChannelCondition condition) {
		return condition == null ? ChannelCondition.ALWAYS : condition;
	}

	/**
	 * Whether every column always exists, so that there is nothing to follow.
	 */
	public boolean isEmpty() {
		return _conditions.isEmpty();
	}

	/**
	 * The names of the columns that do not exist, as determined when this existence was created or
	 * last {@link #recheck() checked again}.
	 */
	public Set<String> absentColumns() {
		return _absentColumns;
	}

	/**
	 * The names of the columns the access rights of the user withhold, as determined when this
	 * existence was created or last {@link #recheck() checked again}.
	 *
	 * <p>
	 * Each of them is also {@link #absentColumns() absent}. In contrast to a column that is absent
	 * only, nothing the table offers may refer to such a column: a declared filter with a criterion
	 * on it would filter by values the user may not read.
	 * </p>
	 */
	public Set<String> withheldColumns() {
		return _withheldColumns;
	}

	/**
	 * Follows the conditions: hands the {@link #absentColumns() absent columns} to the given
	 * consumer whenever they change - when one of the channels the conditions read takes a new
	 * value, or on a {@link #recheck()}.
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
		_consumer = absentColumns;
		List<ChannelCondition> all = new ArrayList<>();
		for (Conditions conditions : _conditions.values()) {
			all.add(conditions.exists());
			all.add(conditions.released());
		}
		Registration channels = ChannelCondition.observe(all, this::recheck);
		return Registration.onDispose(() -> {
			channels.dispose();
			_consumer = null;
		});
	}

	/**
	 * Asks the conditions again, and hands the absent columns to the consumer
	 * {@link #observe(Consumer) following them} if they changed.
	 *
	 * <p>
	 * To be called whenever the table is updated: a condition may depend on state no channel
	 * announces, such as the access rights of the user, see
	 * {@link ChannelCondition#computed(java.util.function.BooleanSupplier)}. Whoever offers
	 * something referring to the columns takes up the {@link #withheldColumns() withheld columns}
	 * afterwards.
	 * </p>
	 */
	public void recheck() {
		Set<String> absentBefore = _absentColumns;
		determine();
		if (_consumer != null && !_absentColumns.equals(absentBefore)) {
			_consumer.accept(_absentColumns);
		}
	}

	private void determine() {
		Set<String> absent = new LinkedHashSet<>();
		Set<String> withheld = new LinkedHashSet<>();
		for (Map.Entry<String, Conditions> entry : _conditions.entrySet()) {
			Conditions conditions = entry.getValue();
			if (!conditions.released().holds()) {
				withheld.add(entry.getKey());
				absent.add(entry.getKey());
			} else if (!conditions.exists().holds()) {
				absent.add(entry.getKey());
			}
		}
		_absentColumns = absent;
		_withheldColumns = withheld;
	}

}
