/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.provider.CollectionLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactValueListControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.table.Aggregator;
import com.top_logic.table.CellContent;
import com.top_logic.table.CellRenderer;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnAlignment;
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
 *        Reads the cell value from a row. For a cell the user may not read, this is the
 *        {@link ColumnProviderService#UNREADABLE} marker, see
 *        {@link ColumnProviderService#attributeValue(Object, String)}.
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
 *        comes last. It is never passed the {@link ColumnProviderService#UNREADABLE} marker
 *        either: a cell the user may not read sorts after every value, see
 *        {@link ColumnProviderService#unreadableLast(Comparator)}.
 * @param hiddenByDefault
 *        Whether the column is displayed only once the user selects it in the column selection.
 * @param export
 *        How the column takes part in an export of the table.
 * @param tooltip
 *        The tooltip of a cell, computed from its row and its value, or {@code null} for a column
 *        whose cells have none. In the header row of a group, the row is {@code null} and the value
 *        is the value the group is formed by.
 * @param cellClass
 *        The CSS class of a data cell, computed from its row and its value, or {@code null} for a
 *        column whose cells have none. It is never passed the
 *        {@link ColumnProviderService#UNREADABLE} marker: a cell the user may not read has no class.
 *        An empty class counts as none.
 * @param display
 *        How a cell displays its value, in place of the display its {@link #type()} derives, or
 *        {@code null} for the type-derived display. It displays a value only: an empty cell, a cell
 *        the user may not read and a cell being edited keep the display of the column its
 *        {@link #binding()} built. A collection of values is displayed value by value. A display
 *        that is a {@link LabelProvider} gives the cells the text the free-text search finds them
 *        by, the labels of a collection's values joined as for any cell holding several values.
 * @param sortable
 *        Whether the column sorts at all. A column that does not sort offers no sorting and
 *        ignores its {@link #comparator()}; a table grouped by it keeps its groups in the order of
 *        the rows it is given, whatever it is sorted by, and a sort orders the rows within each
 *        group.
 * @param align
 *        The side of its cells the column puts its content on, or {@code null} for the one its
 *        values call for: the end for numbers, the start for everything else.
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
		ColumnExport export,
		BiFunction<Object, Object, String> tooltip,
		BiFunction<Object, Object, String> cellClass,
		ReactControlProvider display,
		boolean sortable,
		ColumnAlignment align) {

	/**
	 * Creates a {@link ColumnSetup} of a column whose content is aligned as its values call for.
	 *
	 * @see ColumnSetup The full descriptor.
	 */
	public ColumnSetup(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			ViewContext viewContext, ColumnBinding binding, int width, CellEditing editing,
			Function<List<Object>, Object> aggregate, Comparator<Object> comparator, boolean hiddenByDefault,
			ColumnExport export, BiFunction<Object, Object, String> tooltip,
			BiFunction<Object, Object, String> cellClass, ReactControlProvider display, boolean sortable) {
		this(name, label, type, value, viewContext, binding, width, editing, aggregate, comparator, hiddenByDefault,
			export, tooltip, cellClass, display, sortable, null);
	}

	/**
	 * Creates a {@link ColumnSetup} of a column that sorts.
	 *
	 * @see ColumnSetup The full descriptor.
	 */
	public ColumnSetup(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			ViewContext viewContext, ColumnBinding binding, int width, CellEditing editing,
			Function<List<Object>, Object> aggregate, Comparator<Object> comparator, boolean hiddenByDefault,
			ColumnExport export, BiFunction<Object, Object, String> tooltip,
			BiFunction<Object, Object, String> cellClass, ReactControlProvider display) {
		this(name, label, type, value, viewContext, binding, width, editing, aggregate, comparator, hiddenByDefault,
			export, tooltip, cellClass, display, true);
	}

	/**
	 * Creates a {@link ColumnSetup} of a column that sorts and whose cells display their values as
	 * the type of the values derives.
	 *
	 * @see ColumnSetup The full descriptor.
	 */
	public ColumnSetup(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			ViewContext viewContext, ColumnBinding binding, int width, CellEditing editing,
			Function<List<Object>, Object> aggregate, Comparator<Object> comparator, boolean hiddenByDefault,
			ColumnExport export, BiFunction<Object, Object, String> tooltip,
			BiFunction<Object, Object, String> cellClass) {
		this(name, label, type, value, viewContext, binding, width, editing, aggregate, comparator, hiddenByDefault,
			export, tooltip, cellClass, null);
	}

	/**
	 * Creates a {@link ColumnSetup} of a column whose cells have neither a CSS class of their own nor
	 * a display of their own.
	 *
	 * @see ColumnSetup The full descriptor.
	 */
	public ColumnSetup(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			ViewContext viewContext, ColumnBinding binding, int width, CellEditing editing,
			Function<List<Object>, Object> aggregate, Comparator<Object> comparator, boolean hiddenByDefault,
			ColumnExport export, BiFunction<Object, Object, String> tooltip, boolean sortable) {
		this(name, label, type, value, viewContext, binding, width, editing, aggregate, comparator, hiddenByDefault,
			export, tooltip, null, null, sortable);
	}

	/**
	 * Creates a {@link ColumnSetup} of a column that sorts and whose cells have no CSS class of
	 * their own.
	 *
	 * @see ColumnSetup The full descriptor.
	 */
	public ColumnSetup(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			ViewContext viewContext, ColumnBinding binding, int width, CellEditing editing,
			Function<List<Object>, Object> aggregate, Comparator<Object> comparator, boolean hiddenByDefault,
			ColumnExport export, BiFunction<Object, Object, String> tooltip) {
		this(name, label, type, value, viewContext, binding, width, editing, aggregate, comparator, hiddenByDefault,
			export, tooltip, null);
	}

	/**
	 * Creates a {@link ColumnSetup} of a column that sorts and whose cells have neither a tooltip
	 * nor a CSS class of their own.
	 *
	 * @see ColumnSetup The full descriptor.
	 */
	public ColumnSetup(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			ViewContext viewContext, ColumnBinding binding, int width, CellEditing editing,
			Function<List<Object>, Object> aggregate, Comparator<Object> comparator, boolean hiddenByDefault,
			ColumnExport export) {
		this(name, label, type, value, viewContext, binding, width, editing, aggregate, comparator, hiddenByDefault,
			export, null, null);
	}

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
	 * {@link #comparator()} when it declares one and not at all when it is not {@link #sortable()},
	 * displaying its values through the {@link #display()} when it declares one, with the
	 * {@link #tooltip()} on its cells and on the header rows of the groups it forms when it
	 * computes one, with the {@link #cellClass()} on its cells when it computes one, and left out
	 * of exports when its {@link #export()} says so. It puts its content on the side its
	 * {@link #alignment() alignment} names.
	 */
	public Column<Object, ?> buildColumn() {
		return decorate(binding().createColumn(this));
	}

	/**
	 * The side of its cells the column puts its content on: the one {@link #align() declared}, and
	 * where there is none, the end for a column of numbers and the start for every other.
	 */
	public ColumnAlignment alignment() {
		if (align != null) {
			return align;
		}
		return ColumnProviderService.isNumeric(type) ? ColumnAlignment.END : ColumnAlignment.START;
	}

	/**
	 * The given column with what this descriptor decides over the column its binding built.
	 */
	private <V> Column<Object, V> decorate(Column<Object, V> column) {
		Column<Object, V> result = column;
		ColumnAlignment alignment = alignment();
		if (alignment != result.alignment() && (align != null || alignment == ColumnAlignment.END)) {
			// A binding building a column of its own may align it itself; only a declaration or a
			// column of numbers overrules it.
			result = DelegatingColumn.withAlignment(result, alignment);
		}
		if (width > 0) {
			result = DelegatingColumn.withDefaultWidth(result, width);
		}
		if (aggregate != null) {
			Function<List<Object>, Object> function = aggregate;
			Aggregator<Object, V> aggregator = group -> content(function.apply(group.members()));
			result = new DelegatingColumn<>(result) {
				@Override
				public Optional<Aggregator<Object, V>> aggregate() {
					return Optional.of(aggregator);
				}
			};
		}
		if (!sortable) {
			result = DelegatingColumn.notSortable(result);
		} else if (comparator != null) {
			Sort<V> sort = sort(binding().order(ColumnProviderService.unreadableLast(comparator)));
			result = new DelegatingColumn<>(result) {
				@Override
				public Optional<Sort<V>> sort() {
					return Optional.of(sort);
				}
			};
		}
		if (display != null) {
			result = withDisplay(result, display);
		}
		if (tooltip != null) {
			result = withTooltip(result, tooltip);
		}
		if (cellClass != null) {
			result = withCellClass(result, cellClass);
		}
		if (!export.exported()) {
			result = DelegatingColumn.notExportable(result);
		}
		return result;
	}

	/**
	 * The content of a cell showing the given value, as the {@link #display()} shows it where it
	 * displays the value, as the {@link #type()} derives otherwise.
	 */
	private CellContent content(Object cellValue) {
		if (display != null && displays(cellValue)) {
			return displayed(display, cellValue);
		}
		return ColumnProviderService.displayContent(type, cellValue);
	}

	/**
	 * Whether a declared {@link #display()} displays the given cell value: any value but nothing, an
	 * empty collection and a value the user may not read.
	 */
	private static boolean displays(Object cellValue) {
		if (cellValue == null || ColumnProviderService.hasUnreadable(cellValue)) {
			return false;
		}
		return !(cellValue instanceof Collection<?> collection && collection.isEmpty());
	}

	/** The content of a cell the given display shows the given value in. */
	private static CellContent displayed(ReactControlProvider display, Object cellValue) {
		return new CellContent.Raw((CellControlFactory) context -> displayControl(context, display, cellValue));
	}

	/**
	 * The control the given display shows the given value with.
	 *
	 * <p>
	 * A collection of values is shown value by value, each by the control the display creates for
	 * it, arranged as the read-only {@link ReactValueListControl} arranges the values of any field
	 * holding several of them.
	 * </p>
	 */
	private static ReactControl displayControl(ReactContext context, ReactControlProvider display,
			Object cellValue) {
		if (!(cellValue instanceof Collection<?> values)) {
			return display.createControl(context, cellValue);
		}
		AbstractFieldModel model = new AbstractFieldModel(new ArrayList<>(values));
		model.setEditable(false);
		FieldSpec field = FieldSpec.of(Object.class, null).setMultiple(true);
		return new ReactValueListControl(context, model, field,
			(elementContext, elementField, element) -> display.createControl(elementContext, element.getValue()));
	}

	/**
	 * The text the free-text search finds a cell holding the given value by, as the given labels
	 * write it: a collection of values as the labels of its values, joined with
	 * {@link ColumnProviderService#VALUE_SEPARATOR} as for any cell holding several values.
	 */
	private static String labelText(LabelProvider labels, Object cellValue) {
		if (cellValue instanceof Collection<?>) {
			return new CollectionLabelProvider(labels, ColumnProviderService.VALUE_SEPARATOR).getLabel(cellValue);
		}
		return labels.getLabel(cellValue);
	}

	/**
	 * The given column displaying its values through the given display.
	 *
	 * <p>
	 * A data cell and the header row of a group show their value as the given display does, where
	 * it {@link #displays(Object) displays} the value, and as the given column does otherwise; a
	 * collection of values value by value. A display that is a {@link LabelProvider} is what the
	 * free-text search reads a cell by, so that the search finds what the cell shows.
	 * </p>
	 */
	private static <V> Column<Object, V> withDisplay(Column<Object, V> column, ReactControlProvider display) {
		return new DelegatingColumn<>(column) {
			@Override
			public CellContent renderCell(Object row) {
				V cellValue = super.value(row);
				if (displays(cellValue)) {
					return displayed(display, cellValue);
				}
				return super.renderCell(row);
			}

			@Override
			public CellRenderer<V> renderer() {
				CellRenderer<V> inner = super.renderer();
				return groupValue -> displays(groupValue) ? displayed(display, groupValue) : inner.render(groupValue);
			}

			@Override
			public String searchText(Object row) {
				if (display instanceof LabelProvider labels) {
					V cellValue = super.value(row);
					if (displays(cellValue)) {
						return labelText(labels, cellValue);
					}
				}
				return super.searchText(row);
			}
		};
	}

	/**
	 * The given column with the given tooltip on its cells.
	 *
	 * <p>
	 * A data cell gets the tooltip computed from its row and its value; a value rendered without a
	 * row - the value of a group in the header row of the group - the one computed from no row and
	 * that value. A cell the user may not read has no tooltip.
	 * </p>
	 */
	private static <V> Column<Object, V> withTooltip(Column<Object, V> column,
			BiFunction<Object, Object, String> tooltip) {
		return new DelegatingColumn<>(column) {
			@Override
			public CellContent renderCell(Object row) {
				V cellValue = super.value(row);
				CellContent content = super.renderCell(row);
				if (cellValue == ColumnProviderService.UNREADABLE) {
					return content;
				}
				return CellContent.withTooltip(content, tooltip.apply(row, cellValue));
			}

			@Override
			public CellRenderer<V> renderer() {
				CellRenderer<V> inner = super.renderer();
				return groupValue -> CellContent.withTooltip(inner.render(groupValue), tooltip.apply(null, groupValue));
			}
		};
	}

	/**
	 * The given column with the given CSS class on its data cells.
	 *
	 * <p>
	 * A data cell gets the class computed from its row and its value, in addition to the class the
	 * given column puts on it. A cell the user may not read gets no class of its own.
	 * </p>
	 */
	private static <V> Column<Object, V> withCellClass(Column<Object, V> column,
			BiFunction<Object, Object, String> cellClass) {
		return new DelegatingColumn<>(column) {
			@Override
			public String cssClass(Object row) {
				String inner = super.cssClass(row);
				V cellValue = super.value(row);
				if (cellValue == ColumnProviderService.UNREADABLE) {
					return inner;
				}
				String own = cellClass.apply(row, cellValue);
				if (StringServices.isEmpty(own)) {
					return inner;
				}
				return StringServices.isEmpty(inner) ? own : inner + " " + own;
			}
		};
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
	 * A value that is no collection is ordered as a collection holding only that value. An element
	 * holding the {@link ColumnProviderService#UNREADABLE} marker is ordered after every other
	 * element but an empty one, see {@link ColumnProviderService#unreadableLast(Comparator)}.
	 * </p>
	 *
	 * @param elementOrder
	 *        The ascending order of the elements, never passed {@code null} or the
	 *        {@link ColumnProviderService#UNREADABLE} marker.
	 */
	public static Comparator<Object> elementwise(Comparator<Object> elementOrder) {
		Comparator<Object> elements = Comparator.nullsLast(ColumnProviderService.unreadableLast(elementOrder));
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
