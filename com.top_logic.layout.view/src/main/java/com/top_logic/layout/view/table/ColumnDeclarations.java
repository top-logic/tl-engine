/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;

/**
 * The columns of a table: the ones it shows, and the ones it only offers in its column selection.
 *
 * <p>
 * A table shows what it declares and offers the rest of what its rows hold, so that a user can put
 * any attribute of the row type on display without the table having to enumerate them all. Both
 * halves are described the same way - by {@link ColumnDeclaration}s - and differ only in whether
 * the table starts out showing them.
 * </p>
 *
 * @param displayed
 *        The declarations of the columns the table shows, in display order.
 * @param offered
 *        The declarations of the columns the table offers without showing them, in the order they
 *        are offered in.
 */
public record ColumnDeclarations(List<ColumnDeclaration> displayed, List<ColumnDeclaration> offered) {

	/**
	 * The instantiated declarations of a {@code <columns>} configuration, in declaration order.
	 *
	 * <p>
	 * A declaration that cannot be instantiated is reported and left out, and so is a column whose
	 * name another declaration already uses - a table refers to its columns by name, so two columns
	 * sharing one could not be told apart.
	 * </p>
	 *
	 * @param context
	 *        Instantiates the declarations and takes their problems.
	 * @param config
	 *        The declared columns, or {@code null} for a table declaring none.
	 */
	public static List<ColumnDeclaration> instantiate(InstantiationContext context, ColumnsConfig config) {
		if (config == null) {
			return List.of();
		}
		List<ColumnDeclaration> result = new ArrayList<>(config.getColumns().size());
		Set<String> names = new LinkedHashSet<>();
		for (PolymorphicConfiguration<? extends ColumnDeclaration> entry : config.getColumns()) {
			ColumnDeclaration declaration = context.getInstance(entry);
			if (declaration == null) {
				continue;
			}
			boolean unique = true;
			for (String name : declaration.declaredNames()) {
				if (!names.add(name)) {
					context.error("A table declares the column '" + name + "' more than once.");
					unique = false;
				}
			}
			if (unique) {
				result.add(declaration);
			}
		}
		return result;
	}

	/**
	 * The names of the columns the given declarations contribute, as far as they are known without
	 * rows, in declaration order.
	 */
	public static List<String> declaredNames(List<ColumnDeclaration> declarations) {
		List<String> result = new ArrayList<>();
		for (ColumnDeclaration declaration : declarations) {
			result.addAll(declaration.declaredNames());
		}
		return result;
	}

	/**
	 * The order the given declarations sort the table in until the user sorts it themselves,
	 * {@link SortSpec#NONE} when none of them declares a direction.
	 */
	public static SortSpec defaultSort(List<ColumnDeclaration> declarations) {
		List<SortColumn> sortColumns = new ArrayList<>();
		for (ColumnDeclaration declaration : declarations) {
			sortColumns.addAll(declaration.defaultSort());
		}
		return sortColumns.isEmpty() ? SortSpec.NONE : new SortSpec(sortColumns);
	}

	/**
	 * The columns the given declarations contribute, in display order.
	 */
	public static List<ColumnSetup> resolve(List<ColumnDeclaration> declarations, ColumnResolution scope) {
		List<ColumnSetup> result = new ArrayList<>();
		for (ColumnDeclaration declaration : declarations) {
			result.addAll(declaration.resolve(scope));
		}
		return result;
	}

	/**
	 * Every declaration of the table, the displayed ones first.
	 */
	public List<ColumnDeclaration> all() {
		List<ColumnDeclaration> result = new ArrayList<>(displayed.size() + offered.size());
		result.addAll(displayed);
		result.addAll(offered);
		return result;
	}

	/**
	 * The columns of the table, the displayed ones first.
	 */
	public List<ColumnSetup> resolve(ColumnResolution scope) {
		return resolve(all(), scope);
	}

	/**
	 * The order the table is displayed in until the user sorts it themselves, taken from the
	 * columns it shows.
	 */
	public SortSpec defaultSort() {
		return defaultSort(displayed);
	}

	/**
	 * The names of the columns the table does not show until the user selects them.
	 */
	public Set<String> hiddenByDefault() {
		return new LinkedHashSet<>(declaredNames(offered));
	}

}
