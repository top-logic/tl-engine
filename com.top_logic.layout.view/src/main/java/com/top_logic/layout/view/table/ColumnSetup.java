/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.table.Aggregator;
import com.top_logic.table.Column;
import com.top_logic.table.Sort;
import com.top_logic.table.impl.DelegatingColumn;
import com.top_logic.tool.export.ExcelCellRenderer;

/**
 * The resolved descriptor of one table column, passed to its {@link ColumnBinding} to build the
 * runtime column and contribute any per-session UI.
 *
 * <p>
 * What the column shows is described twice over: {@link #type()} says what kind of value a cell
 * holds, {@link #value()} reads that value from a row. Neither assumes a model attribute, so a
 * column over a computed value is described the same way as one over an attribute.
 * </p>
 *
 * @param name
 *        The column name, which for a column over a model attribute is the attribute name.
 * @param label
 *        The resolved column header label.
 * @param type
 *        What the column's values are, deciding its display, sort, filter and search.
 * @param value
 *        Reads the cell value from a row.
 * @param viewContext
 *        The per-session context, e.g. for resolving channel references.
 * @param binding
 *        The strategy turning this descriptor into a column (and optional UI).
 * @param width
 *        The configured default display width in pixels, or {@code 0} to keep the width the column
 *        brings itself.
 * @param editing
 *        How a cell of this column is edited, or {@code null} for a column that is displayed but
 *        not edited.
 * @param aggregate
 *        What the column shows for a group of rows, computed from the group's member rows, or
 *        {@code null} for a column that leaves its group cell empty.
 * @param comparator
 *        The ascending order of the column's values, by which it sorts the rows and orders the
 *        groups it forms, or {@code null} for the order its binding derives. It is never passed
 *        {@code null}; rows without a value are ordered apart from it, a group without a value
 *        comes last.
 * @param hiddenByDefault
 *        Whether the column is displayed only once the user selects it in the column selection.
 * @param export
 *        How the column takes part in an export of the table.
 */
public record ColumnSetup(
		String name,
		ResKey label,
		ColumnType type,
		Function<Object, Object> value,
		ViewContext viewContext,
		ColumnBinding binding,
		int width,
		CellEditing editing,
		Function<List<Object>, Object> aggregate,
		Comparator<Object> comparator,
		boolean hiddenByDefault,
		ColumnExport export) {

	/**
	 * Creates a {@link ColumnSetup} of a column that is exported by the renderer of its type.
	 *
	 * @see ColumnSetup The full descriptor.
	 */
	public ColumnSetup(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			ViewContext viewContext, ColumnBinding binding, int width, CellEditing editing,
			Function<List<Object>, Object> aggregate, Comparator<Object> comparator, boolean hiddenByDefault) {
		this(name, label, type, value, viewContext, binding, width, editing, aggregate, comparator, hiddenByDefault,
			ColumnExport.DEFAULT);
	}

	/**
	 * Creates a {@link ColumnSetup} of a column that is displayed from the start, is not edited,
	 * aggregates nothing and sorts in the order its binding derives.
	 *
	 * @see ColumnSetup The full descriptor.
	 */
	public ColumnSetup(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			ViewContext viewContext, ColumnBinding binding, int width) {
		this(name, label, type, value, viewContext, binding, width, null, null, null, false);
	}

	/**
	 * The runtime column for this descriptor: the column its {@link #binding()} builds, displayed
	 * in the {@link #width() configured width} when there is one, showing the
	 * {@link #aggregate()} in the header row of a group when it computes one, sorting by the
	 * {@link #comparator()} when it declares one, and left out of exports when its
	 * {@link #export()} says so.
	 */
	public Column<Object, ?> buildColumn() {
		return decorate(binding().createColumn(this));
	}

	/**
	 * The given column with what this descriptor decides over the column its binding built.
	 */
	private <V> Column<Object, V> decorate(Column<Object, V> column) {
		Column<Object, V> result = column;
		if (width > 0) {
			result = DelegatingColumn.withDefaultWidth(result, width);
		}
		if (aggregate != null) {
			ColumnType columnType = type;
			Function<List<Object>, Object> function = aggregate;
			Aggregator<Object, V> aggregator =
				group -> ColumnProviderService.displayContent(columnType, function.apply(group.members()));
			result = new DelegatingColumn<>(result) {
				@Override
				public Optional<Aggregator<Object, V>> aggregate() {
					return Optional.of(aggregator);
				}
			};
		}
		if (comparator != null) {
			Sort<V> sort = sort(binding().order(comparator));
			result = new DelegatingColumn<>(result) {
				@Override
				public Optional<Sort<V>> sort() {
					return Optional.of(sort);
				}
			};
		}
		if (!export.exported()) {
			result = DelegatingColumn.notExportable(result);
		}
		return result;
	}

	/**
	 * The renderer writing a value of this column into a cell of an Excel export, {@code null} for
	 * the default.
	 *
	 * @see ColumnExport#rendererFor(ColumnType)
	 */
	public ExcelCellRenderer exportRenderer() {
		return export.rendererFor(type);
	}

	/**
	 * The order of collections of values by the given order of their elements: element by element,
	 * the first differing element deciding, an empty element after every other one, and a
	 * collection that starts with all elements of a longer one before it.
	 *
	 * <p>
	 * A value that is no collection is ordered as a collection holding only that value.
	 * </p>
	 *
	 * @param elementOrder
	 *        The ascending order of the elements, never passed {@code null}.
	 */
	public static Comparator<Object> elementwise(Comparator<Object> elementOrder) {
		Comparator<Object> elements = Comparator.nullsLast(elementOrder);
		return (value1, value2) -> {
			Iterator<?> it1 = elements(value1).iterator();
			Iterator<?> it2 = elements(value2).iterator();
			while (it1.hasNext() && it2.hasNext()) {
				int result = elements.compare(it1.next(), it2.next());
				if (result != 0) {
					return result;
				}
			}
			return Boolean.compare(it1.hasNext(), it2.hasNext());
		};
	}

	/** The elements of a value ordered {@link #elementwise(Comparator) element by element}. */
	private static Collection<?> elements(Object value) {
		return value instanceof Collection<?> collection ? collection : List.of(value);
	}

	/**
	 * The sort capability of a column ordering its values by the given comparator.
	 *
	 * @implNote The cast is safe since the comparator is the one
	 *           {@link ColumnBinding#order(Comparator)} derives for the values of the very column
	 *           the binding built.
	 */
	@SuppressWarnings("unchecked")
	private static <V> Sort<V> sort(Comparator<?> order) {
		Comparator<V> valueOrder = (Comparator<V>) order;
		return () -> valueOrder;
	}

}
