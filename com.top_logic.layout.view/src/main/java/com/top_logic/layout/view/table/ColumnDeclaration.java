/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.table.SortColumn;

/**
 * One entry of the {@code <columns>} of a table: what columns it contributes, and what they show.
 *
 * <p>
 * A declaration is the unit a table's columns are configured in. It contributes any number of
 * columns - one for a declaration naming a single column, several for one standing for a set of
 * them - so what a table shows is described by what its declarations yield, not by an enumeration
 * of columns. Every kind of column is a declaration of its own, and a table is built from them
 * without distinguishing the kinds.
 * </p>
 *
 * <p>
 * Two moments matter. A table needs the names of its columns before it has any rows: its identity,
 * the order it is sorted in and which columns it offers in addition are all derived from them.
 * What a column shows, on the other hand, is only known once the type of the rows is at hand.
 * {@link #declaredNames()} answers the first question, {@link #resolve(ColumnResolution)} the
 * second.
 * </p>
 */
public interface ColumnDeclaration {

	/**
	 * Configuration of a {@link ColumnDeclaration}, the entry form of a {@code <columns>} list.
	 *
	 * @param <I>
	 *        The declaration this configuration describes.
	 */
	interface Config<I extends ColumnDeclaration> extends PolymorphicConfiguration<I> {
		// The common base of every column declaration; the properties are declared by the kinds.
	}

	/**
	 * The names of the columns this declaration contributes, as far as they are known without rows.
	 *
	 * <p>
	 * Empty for a declaration whose columns only the data at hand decides. Such a declaration takes
	 * no part in what is derived from the column names - the table's identity, its initial sort
	 * order, and which further columns it offers.
	 * </p>
	 */
	List<String> declaredNames();

	/**
	 * The order this declaration's columns sort the table in until the user sorts it themselves.
	 *
	 * <p>
	 * Empty for a declaration whose columns start out unsorted, which is the default.
	 * </p>
	 */
	default List<SortColumn> defaultSort() {
		return List.of();
	}

	/**
	 * The columns this declaration contributes, in display order.
	 *
	 * @param scope
	 *        What the columns are resolved against: the type of the rows, and the session.
	 */
	List<ColumnSetup> resolve(ColumnResolution scope);

}
