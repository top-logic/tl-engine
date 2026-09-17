/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.basic.util.ResKey;

/**
 * The columns a {@link DynamicColumns} shows: the objects standing for them, and what each of those
 * objects says about its column.
 *
 * <p>
 * A table showing one column per milestone of a project stands each of its columns for a milestone.
 * The column is named and labelled after that object, its cells are computed from the row and it,
 * and an edit writes back to both - everything such a column does is a function of the object
 * standing for it, which is what this describes.
 * </p>
 */
public interface DynamicColumnSet {

	/**
	 * The objects standing for the columns: one column per object, in that order.
	 */
	List<?> columns();

	/**
	 * The name of the column the given object stands for, which follows the name of the declaration
	 * contributing it.
	 *
	 * <p>
	 * The user's arrangement of a column is remembered under its name, so the name of an object has
	 * to stay the same across sessions.
	 * </p>
	 *
	 * @param column
	 *        One of the objects standing for a column.
	 */
	String name(Object column);

	/**
	 * The header label of the column the given object stands for.
	 *
	 * @param column
	 *        One of the objects standing for a column.
	 */
	ResKey label(Object column);

	/**
	 * What the values of the column the given object stands for are, deciding how that column
	 * displays, sorts and filters them.
	 *
	 * @param column
	 *        One of the objects standing for a column.
	 */
	ColumnType type(Object column);

	/**
	 * Reads the cell value of the column the given object stands for from a row.
	 *
	 * @param column
	 *        One of the objects standing for a column.
	 */
	Function<Object, Object> value(Object column);

	/**
	 * Writes an edited cell value of the column the given object stands for, receiving the row and
	 * the entered value, or {@code null} for a column that is displayed but not edited.
	 *
	 * @param column
	 *        One of the objects standing for a column.
	 */
	default BiConsumer<Object, Object> update(Object column) {
		return null;
	}

	/**
	 * Which rows of the column the given object stands for offer the edit, or {@code null} where
	 * every row of it does.
	 *
	 * @param column
	 *        One of the objects standing for a column.
	 */
	default Predicate<Object> canUpdate(Object column) {
		return null;
	}

	/**
	 * What the column the given object stands for shows for a group of rows, computed from the
	 * group's member rows, or {@code null} for a column that leaves its group cell empty.
	 *
	 * @param column
	 *        One of the objects standing for a column.
	 */
	default Function<List<Object>, Object> aggregate(Object column) {
		return null;
	}

}
