/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;

/**
 * What a list of {@link ColumnDeclaration}s amounts to: the columns it contributes, their names,
 * and the order they sort the table in.
 *
 * <p>
 * A table shows what it declares and offers the rest of what its rows hold, so that a user can put
 * any attribute of the row type on display without the table having to enumerate them all. Both
 * halves are described the same way - by {@link ColumnDeclaration}s - and a column that is only
 * offered says so through its resolved {@link ColumnSetup#hiddenByDefault()}, wherever it comes
 * from.
 * </p>
 */
public class ColumnDeclarations {

	/**
	 * This class holds what a list of {@link ColumnDeclaration}s amounts to; it has no state of its
	 * own.
	 */
	private ColumnDeclarations() {
		// Static utilities only.
	}

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
	 * The names of the given columns that are displayed only once the user selects them, in the
	 * order they are offered in.
	 *
	 * <p>
	 * Whether a column starts out hidden is what its own setup says, so a column offered by a table
	 * and one offered from inside a declaration reach the display the same way.
	 * </p>
	 */
	public static Set<String> hiddenByDefault(List<ColumnSetup> setups) {
		Set<String> result = new LinkedHashSet<>();
		for (ColumnSetup setup : setups) {
			if (setup.hiddenByDefault()) {
				result.add(setup.name());
			}
		}
		return result;
	}

	/**
	 * The columns to show for objects of the given type where nothing else says which: those the
	 * type names as its main properties, and all of its non-hidden attributes when it names none it
	 * holds.
	 *
	 * @param type
	 *        The type of the objects the columns show, or {@code null} when it is unknown - there
	 *        is then nothing to derive columns from.
	 */
	public static List<ColumnDeclaration> mainColumns(TLStructuredType type) {
		if (type == null) {
			return List.of();
		}
		List<ColumnDeclaration> result = new ArrayList<>();
		for (String name : DisplayAnnotations.getMainProperties(type)) {
			TLStructuredTypePart part = type.getPart(name);
			if (part != null) {
				result.add(AttributeColumn.derived(part));
			}
		}
		if (!result.isEmpty()) {
			return result;
		}
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (DisplayAnnotations.isHidden(part)) {
				continue;
			}
			result.add(AttributeColumn.derived(part));
		}
		return result;
	}

	/**
	 * The columns to <em>offer</em> for objects of the given type in addition to the ones already
	 * covered: those of its attributes a form would display, too.
	 *
	 * <p>
	 * They start out hidden; what is shown is what someone chose to show, and the rest is a choice
	 * the user makes in the column selection.
	 * </p>
	 *
	 * @param covered
	 *        The names of the columns already accounted for, which are not offered a second time.
	 * @param type
	 *        The type of the objects the columns show, or {@code null} when it is unknown - nothing
	 *        is then offered.
	 */
	public static List<ColumnDeclaration> offeredColumns(Collection<String> covered, TLStructuredType type) {
		if (type == null) {
			return List.of();
		}
		Set<String> seen = new LinkedHashSet<>(covered);
		List<ColumnDeclaration> result = new ArrayList<>();
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (DisplayAnnotations.isHidden(part) || !seen.add(part.getName())) {
				continue;
			}
			result.add(AttributeColumn.offered(part));
		}
		return result;
	}

}
