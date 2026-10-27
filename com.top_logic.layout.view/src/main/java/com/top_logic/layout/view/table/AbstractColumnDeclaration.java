/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.CellClassProviderByExpression;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.table.ColumnAlignment;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.util.Resources;

/**
 * Base of the {@link ColumnDeclaration}s that describe a single column, with the properties every
 * such column has: its label, its filter, its width, whether it can be edited, whether it sorts at
 * all, the order it sorts the table in, the order of its values, what it aggregates over a group of
 * rows, how its cells display their values, and the display annotations it states over the ones of
 * its values' attribute or type.
 *
 * <p>
 * A subclass says what the column is named and what its cells hold; everything the table does with
 * those values is described here and reaches the column through the descriptor this class builds.
 * </p>
 */
public abstract class AbstractColumnDeclaration implements ColumnDeclaration {

	/**
	 * Configuration of an {@link AbstractColumnDeclaration}.
	 *
	 * @param <I>
	 *        The declaration this configuration describes.
	 */
	public interface Config<I extends AbstractColumnDeclaration>
			extends ColumnDeclaration.Config<I>, ColumnExportConfig, AnnotatedConfig<TLAttributeAnnotation>,
			ValueAsConfig {

		/**
		 * Name of the variable holding the member rows of a group in the {@link #getAggregate()
		 * aggregate}.
		 */
		String MEMBERS_VARIABLE = "members";

		/**
		 * Name of the variable holding the first of the values the {@link #getComparator()
		 * comparator} compares.
		 */
		String LEFT_VARIABLE = "left";

		/**
		 * Name of the variable holding the second of the values the {@link #getComparator()
		 * comparator} compares.
		 */
		String RIGHT_VARIABLE = "right";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getFilter()}. */
		String FILTER = "filter";

		/** Configuration name for {@link #getReadonly()}. */
		String READONLY = "readonly";

		/** Configuration name for {@link #getSort()}. */
		String SORT = "sort";

		/** Configuration name for {@link #getWidth()}. */
		String WIDTH = "width";

		/** Configuration name for {@link #getAggregate()}. */
		String AGGREGATE = "aggregate";

		/** Configuration name for {@link #getVisible()}. */
		String VISIBLE = "visible";

		/** Configuration name for {@link #getComparator()}. */
		String COMPARATOR = "comparator";

		/** Configuration name for {@link #getTooltip()}. */
		String TOOLTIP = "tooltip";

		/** Configuration name for {@link #getAlign()}. */
		String ALIGN = "align";

		/** Configuration name for {@link #getCellClass()}. */
		String CELL_CLASS = "cell-class";

		/** Configuration name for {@link #getDisplay()}. */
		String DISPLAY = "display";

		/** Configuration name for {@link #getSortable()}. */
		String SORTABLE = "sortable";

		/**
		 * The column header label, or unset to keep the label the column derives - the label of the
		 * attribute it shows, and the column's name where no attribute says it.
		 */
		@Name(LABEL)
		ResKey getLabel();

		/**
		 * An application-defined filter for this column, overriding the type-derived default. The
		 * filter matches against the cell's display text.
		 */
		@Name(FILTER)
		PolymorphicConfiguration<? extends ColumnFilter<?>> getFilter();

		/**
		 * Whether this column stays read-only while the rows of the table are edited.
		 */
		@Name(READONLY)
		boolean getReadonly();

		/**
		 * The direction this column is sorted in when the table is first shown, or unset to leave
		 * it unsorted.
		 *
		 * <p>
		 * Sorting by several columns is expressed by setting this on more than one of them; the
		 * declaration order decides which one sorts first. The default only applies until the user
		 * sorts the table themselves, from then on their own order is remembered.
		 * </p>
		 *
		 * <p>
		 * The rows are sorted in the order of the {@link #getComparator() comparator}. A column that
		 * is not {@link #getSortable() sortable} has no direction.
		 * </p>
		 */
		@Name(SORT)
		@Nullable
		@NullDefault
		SortDirection getSort();

		/**
		 * The column's default display width in pixels.
		 *
		 * <p>
		 * This is the width the user sees until they resize the column themselves; from then on
		 * their own width is remembered. {@code 0} - the default - keeps the width the column's
		 * type derives.
		 * </p>
		 */
		@Name(WIDTH)
		@Constraint(NonNegative.class)
		int getWidth();

		/**
		 * What this column shows for a group of rows: an expression over the member rows of the
		 * group, whose result is displayed in the group's header row.
		 *
		 * <p>
		 * The expression sees the list of the rows in the group as the variable
		 * {@value #MEMBERS_VARIABLE} and yields one value of the kind the column holds - the sum of
		 * a number column, the earliest of a date column - which is then displayed exactly as a cell
		 * of that column is. It sees the values of the inputs of the column as well, of the table
		 * for a column without inputs of its own. Unset (the default) leaves the column's group cell
		 * empty.
		 * </p>
		 */
		@Name(AGGREGATE)
		@Supplies(MEMBERS_VARIABLE)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getAggregate();

		/**
		 * Whether the column is shown from the start.
		 *
		 * <p>
		 * {@code false} offers the column in the column selection only: the table starts out
		 * without it, and the user shows it by selecting it there. A user's own arrangement of the
		 * table takes precedence over this setting.
		 * </p>
		 */
		@Name(VISIBLE)
		@BooleanDefault(true)
		boolean getVisible();

		/**
		 * The order of this column's values: an expression comparing two cell values, by which the
		 * column sorts the rows and orders the groups it forms.
		 *
		 * <p>
		 * The expression sees the two values of the column as the variables
		 * {@value #LEFT_VARIABLE} and {@value #RIGHT_VARIABLE} and yields a negative number, zero,
		 * or a positive number, as the first value comes before, together with, or after the second
		 * one - the result of TL-Script's <code>compare</code> function. An order by a key of the
		 * values is written
		 * <code>compare($left.get(`m:Page#position`), $right.get(`m:Page#position`))</code>. It
		 * sees the values of the inputs of the column as well, of the table for a column without
		 * inputs of its own. The values are never empty; rows without a value are ordered apart
		 * from them, a group without a value comes last. Unset (the default) keeps the order derived
		 * from the type of the column's values.
		 * </p>
		 *
		 * <p>
		 * The comparator decides the order the {@link #getSort() sort direction} applies to. A
		 * column that is not {@link #getSortable() sortable} has no comparator.
		 * </p>
		 */
		@Name(COMPARATOR)
		@Supplies(LEFT_VARIABLE)
		@Supplies(RIGHT_VARIABLE)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getComparator();

		/**
		 * The tooltip of a cell of the column: an expression computing what the user reads when
		 * pointing at the cell.
		 *
		 * <p>
		 * The expression sees the row as the variable {@value RowAsConfig#DEFAULT_ROW_AS}, or by the
		 * name the table gives it with {@value RowAsConfig#ROW_AS}, and the value of the cell as the
		 * variable {@value ValueAsConfig#DEFAULT_VALUE_AS}, or by the name given with
		 * {@value ValueAsConfig#VALUE_AS}. In the header row of a group formed by the column, it is
		 * evaluated for the value of the group, without a row. It sees the values of the inputs of
		 * the column as well, of the table for a column without inputs of its own. It yields a text
		 * or an internationalized text, written as <code>#('Text'@en, 'Text'@de)</code>; nothing at
		 * all leaves the cell without a tooltip. Unset (the default), a cell offers its own text
		 * where it does not fit.
		 * </p>
		 */
		@Name(TOOLTIP)
		@Supplies(value = RowAsConfig.DEFAULT_ROW_AS, renamedBy = RowAsConfig.ROW_AS)
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getTooltip();

		/**
		 * The CSS class of a cell of the column: an expression computing the class from the row and
		 * the value of the cell.
		 *
		 * <p>
		 * The expression sees the row as the variable {@value RowAsConfig#DEFAULT_ROW_AS}, or by the
		 * name the table gives it with {@value RowAsConfig#ROW_AS}, and the value of the cell as the
		 * variable {@value ValueAsConfig#DEFAULT_VALUE_AS}, or by the name given with
		 * {@value ValueAsConfig#VALUE_AS}. It sees the values of the inputs of the column as well,
		 * of the table for a column without inputs of its own.
		 * </p>
		 *
		 * <p>
		 * The result is put on the cell: nothing or an empty string gives the cell no class, a list
		 * gives it all of its members as classes, any other value its text. The standard classes
		 * tl-status-error, tl-status-warning, tl-status-success and tl-status-info tint the cell in
		 * the color of the corresponding status. Any other class is styled by the application's own
		 * stylesheet.
		 * </p>
		 *
		 * <p>
		 * The class follows the cell: it is computed whenever the cell is built. A cell the user may
		 * not read and the header row of a group get no class. Unset (the default) leaves the cells
		 * without a class of their own.
		 * </p>
		 *
		 * <p>
		 * Example marking the overdue dates of a column:
		 * </p>
		 *
		 * <pre>
		 * $value != null &amp;&amp; $value &lt; today() ? "tl-status-error" : null
		 * </pre>
		 */
		@Name(CELL_CLASS)
		@Supplies(value = RowAsConfig.DEFAULT_ROW_AS, renamedBy = RowAsConfig.ROW_AS)
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getCellClass();

		/**
		 * How a cell of the column displays its value, in place of the display the type of the
		 * values derives.
		 *
		 * <p>
		 * The display is given the value of the cell and creates the control showing it - the same
		 * displays a <code>&lt;tree&gt;</code> shows its nodes with. It shows a value only: an empty
		 * cell, a cell the user may not read and a cell being edited keep the display of the type.
		 * The header row of a group formed by the column and the result of its
		 * {@link #getAggregate() aggregate} show their value through it as well. A display that
		 * computes a label for the value is also what the search of the table finds the cell by.
		 * Unset (the default), the cells show their values as the type of the values derives.
		 * </p>
		 *
		 * <p>
		 * Example showing a device by its icon and a label of its own, computed with
		 * {@link com.top_logic.layout.view.element.ResourceDisplayByExpression}:
		 * </p>
		 *
		 * <pre>
		 * &lt;computed-column name="device" value="$row"&gt;
		 *     &lt;display class="com.top_logic.layout.view.element.ResourceDisplayByExpression"
		 *         label="$value.get(`my:Device#name`) + ' (' + $value.get(`my:Device#address`) + ')'"
		 *     /&gt;
		 * &lt;/computed-column&gt;
		 * </pre>
		 */
		@Name(DISPLAY)
		PolymorphicConfiguration<ReactControlProvider> getDisplay();

		/**
		 * Whether the table can be sorted by this column.
		 *
		 * <p>
		 * {@code false} offers no sorting by the column, and a table grouped by it keeps its groups
		 * in the order of the rows the table is given, whatever the table is sorted by; a sort
		 * orders the rows within each group. Such a column has neither a {@link #getSort() sort direction} nor a
		 * {@link #getComparator() comparator}.
		 * </p>
		 */
		@Name(SORTABLE)
		@BooleanDefault(true)
		boolean getSortable();

		/**
		 * The side of its cells the column puts its content on.
		 *
		 * <p>
		 * Unset (the default), a column of numbers ends at the end of its cells, so that their
		 * digits line up, and every other column starts at the beginning. Set it where that is not
		 * what the column's values call for - a column computed by a script that yields a number,
		 * or a number that is an identifier and reads like text.
		 * </p>
		 */
		@Name(ALIGN)
		@Nullable
		@NullDefault
		ColumnAlignment getAlign();

	}

	private final ResKey _label;

	private final ColumnBinding _binding;

	private final boolean _readonly;

	private final SortDirection _sort;

	private final int _width;

	private final QueryExecutor _aggregate;

	private final QueryExecutor _comparator;

	private final QueryExecutor _tooltip;

	private final ColumnAlignment _align;
	private final QueryExecutor _cellClass;

	/** The configured {@link Config#getDisplay() display}, {@code null} for the type-derived one. */
	private final ReactControlProvider _display;

	/**
	 * The inputs whose values the {@link #_aggregate}, the {@link #_comparator}, the
	 * {@link #_tooltip} and the {@link #_cellClass} see, empty for a declaration setting none of
	 * them.
	 */
	private final List<InputRef> _scriptInputs;

	private final boolean _hiddenByDefault;

	private final boolean _sortable;

	private final ColumnExport _export;

	/** The display annotations the declaration states, {@code null} where it states none. */
	private final AnnotationLookup _annotations;

	/**
	 * Creates an {@link AbstractColumnDeclaration} from configuration.
	 *
	 * @param context
	 *        The context instantiating a configured filter, and reporting its problems.
	 * @param config
	 *        What the declared column looks like and what it can do.
	 */
	protected AbstractColumnDeclaration(InstantiationContext context, Config<?> config) {
		_label = config.getLabel();
		_binding = binding(context, config.getFilter());
		_readonly = config.getReadonly();
		_sort = config.getSort();
		_width = config.getWidth();
		_aggregate = InputScript.compile(config, Config.AGGREGATE);
		_comparator = InputScript.compile(config, Config.COMPARATOR);
		_tooltip = InputScript.compile(config, Config.TOOLTIP);
		_align = config.getAlign();
		_cellClass = InputScript.compile(config, Config.CELL_CLASS);
		_display = context.getInstance(config.getDisplay());
		boolean scripted = _aggregate != null || _comparator != null || _tooltip != null || _cellClass != null;
		_scriptInputs =
			scripted ? InputScript.inputs(config, config.descriptor().getProperty(Config.AGGREGATE)) : List.of();
		_hiddenByDefault = !config.getVisible();
		_sortable = config.getSortable();
		_export = ColumnExport.of(context, config);
		_annotations = config.getAnnotations().isEmpty() ? null : config;

		if (!_sortable && (_sort != null || _comparator != null)) {
			context.error("A column that is not '" + Config.SORTABLE + "' has neither a '" + Config.SORT
				+ "' direction nor a '" + Config.COMPARATOR + "': column at " + config.location() + ".");
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The channels the declared filter reads, and the inputs of the column - of the table, for a
	 * column without inputs of its own - where the column computes its
	 * {@link Config#getAggregate() aggregate}, its {@link Config#getComparator() order}, its
	 * {@link Config#getTooltip() tooltips} or its {@link Config#getCellClass() cell classes} with
	 * a script: the script sees their values, so the cells follow them.
	 * </p>
	 */
	@Override
	public List<InputRef> inputs() {
		return union(_scriptInputs, _binding.inputs());
	}

	/**
	 * The given lists of inputs joined, each input once, in the order of their first occurrence.
	 */
	@SafeVarargs
	protected static List<InputRef> union(List<InputRef>... inputs) {
		Set<InputRef> result = new LinkedHashSet<>();
		for (List<InputRef> list : inputs) {
			result.addAll(list);
		}
		return new ArrayList<>(result);
	}

	/**
	 * Creates the plain {@link AbstractColumnDeclaration} a table derives for a column no
	 * declaration of its own describes: the type-derived display, the label and width the column
	 * brings itself, no sorting, the type-derived order and no aggregate.
	 *
	 * @param readonly
	 *        Whether the column stays read-only while the rows of the table are edited.
	 * @param hiddenByDefault
	 *        Whether the column is displayed only once the user selects it in the column selection.
	 */
	protected AbstractColumnDeclaration(boolean readonly, boolean hiddenByDefault) {
		_label = null;
		_binding = ColumnBinding.TYPE_DERIVED;
		_readonly = readonly;
		_sort = null;
		_width = 0;
		_aggregate = null;
		_comparator = null;
		_tooltip = null;
		_align = null;
		_cellClass = null;
		_display = null;
		_scriptInputs = List.of();
		_hiddenByDefault = hiddenByDefault;
		_sortable = true;
		_export = ColumnExport.DEFAULT;
		_annotations = null;
	}

	/**
	 * The configured column header label, or {@code null} to keep the label the column derives.
	 */
	protected final ResKey label() {
		return _label;
	}

	@Override
	public List<SortColumn> defaultSort() {
		if (_sort == null) {
			return List.of();
		}
		boolean ascending = _sort == SortDirection.ASC;
		List<SortColumn> result = new ArrayList<>();
		for (String name : declaredNames()) {
			result.add(new SortColumn(name, ascending));
		}
		return result;
	}

	/**
	 * The descriptor of one of this declaration's columns: what the subclass says about the values,
	 * combined with what this declaration says about every column of its own.
	 *
	 * @param name
	 *        The column name.
	 * @param label
	 *        The header label the column derives, used when the declaration configures none.
	 * @param type
	 *        What the column's values are. The display annotations the declaration states take
	 *        precedence over the ones described there.
	 * @param value
	 *        Reads the cell value from a row.
	 * @param editing
	 *        How a cell of the column is edited, or {@code null} for a column offering no edit. A
	 *        column declared read-only is not edited, whatever its kind could offer.
	 * @param scope
	 *        What the column is resolved against.
	 */
	protected final ColumnSetup setup(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			CellEditing editing, ColumnResolution scope) {
		ColumnType annotatedType = _annotations == null ? type : type.annotatedBy(_annotations);
		List<ViewChannel> inputs =
			_scriptInputs.isEmpty() ? List.of() : ChannelInputs.resolve(scope.context(), _scriptInputs);
		return new ColumnSetup(name, _label != null ? _label : label, annotatedType, value, scope.context(), _binding,
			_width, _readonly ? null : editing, aggregate(_aggregate, inputs), comparator(_comparator, inputs),
			_hiddenByDefault, _export, tooltip(_tooltip, inputs), cellClass(_cellClass, inputs), _display,
			_sortable, _align);
	}

	/**
	 * The column integration for a declared filter: derived from the type of the column's values
	 * when none is declared, the filter's own integration when it provides one, and a value-text
	 * filter otherwise.
	 *
	 * <p>
	 * This single capability check is the only place filter kinds are distinguished.
	 * </p>
	 */
	private static ColumnBinding binding(InstantiationContext context,
			PolymorphicConfiguration<? extends ColumnFilter<?>> filterConfig) {
		if (filterConfig == null) {
			return ColumnBinding.TYPE_DERIVED;
		}
		ColumnFilter<?> filter = context.getInstance(filterConfig);
		if (filter == null) {
			return ColumnBinding.TYPE_DERIVED;
		}
		return filter instanceof ColumnBinding binding ? binding : ColumnBinding.forValueFilter(filter);
	}

	/**
	 * The function computing the column's group cell, {@code null} for a column that aggregates
	 * nothing.
	 *
	 * @param executor
	 *        The compiled {@link Config#getAggregate()}, applied to the member rows of every group.
	 * @param inputs
	 *        The channels of its inputs.
	 */
	private static Function<List<Object>, Object> aggregate(QueryExecutor executor, List<ViewChannel> inputs) {
		if (executor == null) {
			return null;
		}
		return members -> executor.execute(ChannelInputs.arguments(inputs, members));
	}

	/**
	 * The order the given comparison establishes, {@code null} for a column keeping its
	 * type-derived order.
	 *
	 * @param executor
	 *        The compiled {@link Config#getComparator()}, applied to every pair of values compared.
	 * @param inputs
	 *        The channels of its inputs.
	 */
	private static Comparator<Object> comparator(QueryExecutor executor, List<ViewChannel> inputs) {
		if (executor == null) {
			return null;
		}
		return order((value1, value2) -> executor.execute(ChannelInputs.arguments(inputs, value1, value2)));
	}

	/**
	 * The function computing the tooltip of a cell from its row and its value, {@code null} for a
	 * column whose cells have none.
	 *
	 * @param executor
	 *        The compiled {@link Config#getTooltip()}.
	 * @param inputs
	 *        The channels of its inputs.
	 */
	private static BiFunction<Object, Object, String> tooltip(QueryExecutor executor, List<ViewChannel> inputs) {
		if (executor == null) {
			return null;
		}
		return (row, value) -> tooltipText(executor.execute(ChannelInputs.arguments(inputs, row, value)));
	}

	/**
	 * The function computing the CSS class of a cell from its row and its value, {@code null} for a
	 * column whose cells have none. The function answers {@code null} for a cell without a class.
	 *
	 * @param executor
	 *        The compiled {@link Config#getCellClass()}.
	 * @param inputs
	 *        The channels of its inputs.
	 */
	private static BiFunction<Object, Object, String> cellClass(QueryExecutor executor, List<ViewChannel> inputs) {
		if (executor == null) {
			return null;
		}
		return (row, value) -> cssClass(executor.execute(ChannelInputs.arguments(inputs, row, value)));
	}

	/**
	 * The CSS class of a cell a script computed, {@code null} for none.
	 *
	 * @see CellClassProviderByExpression#toCssClass(Object)
	 */
	public static String cssClass(Object result) {
		return StringServices.nonEmpty(CellClassProviderByExpression.toCssClass(result));
	}

	/**
	 * The text of a tooltip a script computed: the text itself, an internationalized text in the
	 * language of the user, the label of any other value, {@code null} for nothing at all.
	 */
	public static String tooltipText(Object result) {
		if (result == null) {
			return null;
		}
		if (result instanceof ResKey key) {
			return Resources.getInstance().getString(key);
		}
		if (result instanceof CharSequence text) {
			return text.toString();
		}
		return MetaLabelProvider.INSTANCE.getLabel(result);
	}

	/**
	 * The order a TL-Script comparison establishes, in the contract of TL-Script's
	 * <code>compare</code> function: the number it yields, taken as an integer, decides, and no
	 * result counts as equal.
	 *
	 * @param comparison
	 *        Applies the comparison to two values.
	 */
	static Comparator<Object> order(BiFunction<Object, Object, Object> comparison) {
		return (value1, value2) -> {
			Object result = comparison.apply(value1, value2);
			if (result == null) {
				return 0;
			}
			return ((Number) result).intValue();
		};
	}

}
