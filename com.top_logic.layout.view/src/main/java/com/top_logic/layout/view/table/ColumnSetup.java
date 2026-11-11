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
import java.util.function.Consumer;
import java.util.function.Function;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.provider.CollectionLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactValueListControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelCondition;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.table.Aggregator;
import com.top_logic.table.CellContent;
import com.top_logic.table.CellExistence;
import com.top_logic.table.CellRenderer;
import com.top_logic.table.Column;
import com.top_logic.table.Sort;
import com.top_logic.table.impl.DelegatingColumn;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.util.Resources;

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
 *        {@code null}, a value {@link ColumnProviderService#isNoValue(Object) showing nothing}
 *        or the {@link ColumnProviderService#UNREADABLE} marker: in both sort directions, a cell
 *        the user may not read comes after every value, and a cell showing nothing after that,
 *        see {@link ColumnProviderService#cellSort(java.util.function.Supplier)}.
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
 * @param cellRole
 *        The color role of a data cell, computed from its row and its value, or {@code null} for a
 *        column whose cells have none. It is never passed the
 *        {@link ColumnProviderService#UNREADABLE} marker: a cell the user may not read has no role.
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
 * @param exists
 *        Whether the column currently exists for the table, {@link ChannelCondition#ALWAYS} for a
 *        column that always does. While the condition does not hold, the table neither displays
 *        the column nor offers it in its column selection, see
 *        {@link ColumnExistence}. Never {@code null}.
 * @param cellExists
 *        Whether the column has a cell for a row, {@code null} for a column that has one for every
 *        row: a column over an attribute has none for a row whose type does not hold the
 *        attribute. A row without a cell shows nothing in the column - neither a value, nor an
 *        input, nor a tooltip or a class -, and the built column announces the test as its
 *        {@link Column#existence()}.
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
		BiFunction<Object, Object, ValueColor> cellRole,
		ReactControlProvider display,
		boolean sortable,
		ChannelCondition exists,
		CellExistence<Object> cellExists) {

	/**
	 * Creates a {@link ColumnSetup} of a column that has a cell for every row.
	 *
	 * @see ColumnSetup The full descriptor.
	 */
	public ColumnSetup(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			ViewContext viewContext, ColumnBinding binding, int width, CellEditing editing,
			Function<List<Object>, Object> aggregate, Comparator<Object> comparator, boolean hiddenByDefault,
			ColumnExport export, BiFunction<Object, Object, String> tooltip,
			BiFunction<Object, Object, String> cellClass, ReactControlProvider display, boolean sortable,
			ChannelCondition exists) {
		this(name, label, type, value, viewContext, binding, width, editing, aggregate, comparator, hiddenByDefault,
			export, tooltip, cellClass, display, sortable, exists, null);
	}

	/**
	 * Creates a {@link ColumnSetup} of a column that always exists and whose cells have no color
	 * role of their own.
	 *
	 * @see ColumnSetup The full descriptor.
	 */
	public ColumnSetup(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			ViewContext viewContext, ColumnBinding binding, int width, CellEditing editing,
			Function<List<Object>, Object> aggregate, Comparator<Object> comparator, boolean hiddenByDefault,
			ColumnExport export, BiFunction<Object, Object, String> tooltip,
			BiFunction<Object, Object, String> cellClass, ReactControlProvider display, boolean sortable) {
		this(name, label, type, value, viewContext, binding, width, editing, aggregate, comparator, hiddenByDefault,
			export, tooltip, cellClass, null, display, sortable, ChannelCondition.ALWAYS);
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
	 * This descriptor for a column that exists only where the given condition holds in addition
	 * to its own {@link #exists()} condition.
	 */
	public ColumnSetup existingIf(ChannelCondition condition) {
		if (condition == ChannelCondition.ALWAYS) {
			return this;
		}
		return new ColumnSetup(name, label, type, value, viewContext, binding, width, editing, aggregate, comparator,
			hiddenByDefault, export, tooltip, cellClass, cellRole, display, sortable, exists.and(condition), cellExists);
	}

	/**
	 * The runtime column for this descriptor: the column its {@link #binding()} builds, displayed
	 * in the {@link #width() configured width} when there is one, showing the
	 * {@link #aggregate()} in the header row of a group when it computes one, sorting by the
	 * {@link #comparator()} when it declares one and not at all when it is not {@link #sortable()},
	 * displaying its values through the {@link #display()} when it declares one, with the
	 * {@link #tooltip()} on its cells and on the header rows of the groups it forms when it
	 * computes one, with the {@link #cellClass()} and the {@link #cellRole()} on its cells when it
	 * computes them, and left out of exports when its {@link #export()} says so.
	 */
	public Column<Object, ?> buildColumn() {
		return decorate(binding().createColumn(this));
	}

	/**
	 * The runtime column for this descriptor in a table whose cells are edited in place: as
	 * {@link #buildColumn()}, but a cell of a row the {@link #editing()} offers the edit on shows
	 * the control the value is entered with, and writes an entered value at once.
	 *
	 * <p>
	 * Only a column writing a computed value back through an update function is edited in place
	 * ({@link ValueCellEditing}): an attribute column edits the object itself, which is done in a
	 * form, where the edit is buffered until it is saved. A cell of a row the edit is not offered
	 * on shows a value it has with the same control, read-only, and an empty cell as the column
	 * displays it. The cell keeps the tooltip, the cell class and the cell role the column computes.
	 * </p>
	 *
	 * @param written
	 *        Called with the row after a value of its cell was written, e.g. to build the cells
	 *        whose values may follow the written one again.
	 */
	public Column<Object, ?> buildInPlaceColumn(Consumer<Object> written) {
		Column<Object, ?> column = buildColumn();
		if (!(editing instanceof ValueCellEditing valueEditing)) {
			return column;
		}
		return inPlace(column, valueEditing, written);
	}

	private <V> Column<Object, V> inPlace(Column<Object, V> column, ValueCellEditing editing,
			Consumer<Object> written) {
		return new DelegatingColumn<>(column) {
			@Override
			public CellContent renderCell(Object row) {
				CellContent content = super.renderCell(row);
				CellControlFactory factory;
				if (editing.canEdit(row)) {
					factory = context -> createEditControl(context, row,
						editing.createInPlaceModel(row, () -> written.accept(row)));
				} else if (editing.value(row) != null) {
					// A value the row does not offer to edit is shown by the same control, disabled.
					factory = context -> editing.createControl(context, row, editing.createReadOnlyModel(row));
				} else {
					return content;
				}
				return CellContent.withTooltip(new CellContent.Raw(factory), content.tooltip());
			}
		};
	}

	/**
	 * Creates the control an edited cell of this column is entered with: the control the
	 * {@link #editing()} creates for the given row and model, in a
	 * {@link ReactFormFieldChromeControl#setCompact(boolean) compact} field chrome.
	 *
	 * <p>
	 * The chrome keeps the size of the control, so that the row and the column keep theirs, and
	 * shows the error or the warnings of the given model as a marker at the corner of the cell. It
	 * is named by the {@link #label()} of the column, addressed by its {@link #name()}, follows the
	 * model itself, whatever control enters the value, and is shown only while the field is
	 * {@link BoundFieldModel#isVisible() visible}.
	 * </p>
	 *
	 * @param context
	 *        The React context the control is created in.
	 * @param row
	 *        The row whose cell is edited.
	 * @param model
	 *        The field model of that cell.
	 */
	public ReactControl createEditControl(ReactContext context, Object row, BoundFieldModel model) {
		ReactControl input = editing.createControl(context, row, model);
		ReactFormFieldChromeControl chrome = new ReactFormFieldChromeControl(context,
			label == null ? null : Resources.getInstance().getString(label), input);
		chrome.setAgentName(name);
		chrome.setCompact(true);
		chrome.setFieldModel(model);
		Runnable followVisibility = () -> chrome.setVisible(model.isVisible());
		followVisibility.run();
		model.addVisibilityListener(followVisibility);
		chrome.addCleanupAction(() -> model.removeVisibilityListener(followVisibility));
		return chrome;
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
			Sort<V> sort = sort(binding().sort(comparator));
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
		if (cellRole != null) {
			result = withCellRole(result, cellRole);
		}
		if (!export.exported()) {
			result = DelegatingColumn.notExportable(result);
		}
		if (cellExists != null) {
			result = withCellExistence(result, cellExists);
		}
		return result;
	}

	/**
	 * The given column showing nothing for a row it has no cell for: no content, no search text, no
	 * class and no exported value.
	 */
	private static <V> Column<Object, V> withCellExistence(Column<Object, V> column, CellExistence<Object> exists) {
		return new DelegatingColumn<>(column) {
			@Override
			public CellContent renderCell(Object row) {
				return exists.cellExists(row) ? super.renderCell(row) : CellContent.empty();
			}

			@Override
			public String searchText(Object row) {
				return exists.cellExists(row) ? super.searchText(row) : "";
			}

			@Override
			public String cssClass(Object row) {
				return exists.cellExists(row) ? super.cssClass(row) : null;
			}

			@Override
			public Object exportValue(Object row) {
				return exists.cellExists(row) ? super.exportValue(row) : null;
			}

			@Override
			public Optional<CellExistence<Object>> existence() {
				return Optional.of(exists);
			}
		};
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
	 * The given column with the given color role on its data cells.
	 *
	 * <p>
	 * A data cell gets the role computed from its row and its value, the role the given column
	 * gives it where it computes none. A cell the user may not read gets no role of its own.
	 * </p>
	 */
	private static <V> Column<Object, V> withCellRole(Column<Object, V> column,
			BiFunction<Object, Object, ValueColor> cellRole) {
		return new DelegatingColumn<>(column) {
			@Override
			public ValueColor color(Object row) {
				V cellValue = super.value(row);
				if (cellValue == ColumnProviderService.UNREADABLE) {
					return super.color(row);
				}
				ValueColor own = cellRole.apply(row, cellValue);
				return own != null ? own : super.color(row);
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
	 * element but an empty one, see {@link ColumnProviderService#cellOrder(Comparator)}. A
	 * collection without elements is no value the order is applied to: it shows nothing, and the
	 * column's sort places it after every value.
	 * </p>
	 *
	 * @param elementOrder
	 *        The ascending order of the elements, never passed {@code null} or the
	 *        {@link ColumnProviderService#UNREADABLE} marker.
	 */
	public static Comparator<Object> elementwise(Comparator<Object> elementOrder) {
		Comparator<Object> elements = ColumnProviderService.cellOrder(elementOrder);
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
	 * The given sort capability as one of a column of the given value type.
	 *
	 * @implNote The cast is safe since the sort is the one {@link ColumnBinding#sort(Comparator)}
	 *           derives for the values of the very column the binding built.
	 */
	@SuppressWarnings("unchecked")
	private static <V> Sort<V> sort(Sort<?> sort) {
		return (Sort<V>) sort;
	}

}
