/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;

/**
 * The {@code <dynamic-columns>} of a table: one column per element of a computed set of objects.
 *
 * <p>
 * A table of employees showing one column per month, a table of tasks showing one column per
 * milestone: which columns there are is decided by data, not by the configuration. Each of those
 * columns stands for one object - a month, a milestone - and shows what the row holds for it, which
 * the value expression computes from the row and that object.
 * </p>
 *
 * <p>
 * The columns are named after the declaration and the object each of them stands for, so that the
 * user's arrangement of a column is remembered under a name that stays with it. They share what the
 * declaration says about all of them: the type of their values (or an expression computing it per
 * column), their display width and whether they stay read-only while the rows are edited. Their
 * filter is the one their type derives; a filter of their own is not declared, as there is no
 * single column to declare it for.
 * </p>
 *
 * <p>
 * The columns are those the expression yields when the table is built. A table whose rows follow an
 * input reads its rows again whenever that input changes; its columns are the ones it was built
 * with.
 * </p>
 *
 * @implNote The set of columns a declaration contributes is a {@link DynamicColumnSet}, computed
 *           for the scope the table resolves its columns against. The configured declaration
 *           computes it with TL-Script; the columns are built from it by
 *           {@link #columns(DynamicColumnSet, ColumnResolution)}, whatever produced it.
 */
@InApp
public class DynamicColumns implements ColumnDeclaration {

	/** The tag a set of computed columns is declared with. */
	public static final String TAG_NAME = "dynamic-columns";

	/** Separates the name of the declaration from the name of one of its columns. */
	private static final char NAME_SEPARATOR = '.';

	/**
	 * Configuration of a {@link DynamicColumns}.
	 */
	@TagName(TAG_NAME)
	public interface Config
			extends ColumnDeclaration.Config<DynamicColumns>, Inputs, ColumnExportConfig, ValueAsConfig {

		/** Configuration name for {@link #getColumnAs()}. */
		String COLUMN_AS = "column-as";

		/** Name of the variable holding the object a column stands for when no other name is given. */
		String DEFAULT_COLUMN_AS = "column";

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for {@link #getMultiple()}. */
		String MULTIPLE = "multiple";

		/** Configuration name for {@link #getColumnType()}. */
		String COLUMN_TYPE = "column-type";

		/** Configuration name for {@link #getWidth()}. */
		String WIDTH = "width";

		/** Configuration name for {@link #getReadonly()}. */
		String READONLY = "readonly";

		/** Configuration name for {@link #getColumns()}. */
		String COLUMNS = "columns";

		/** Configuration name for {@link #getColumnName()}. */
		String COLUMN_NAME = "column-name";

		/** Configuration name for {@link #getColumnLabel()}. */
		String COLUMN_LABEL = "column-label";

		/** Configuration name for {@link #getColumnVisible()}. */
		String COLUMN_VISIBLE = "column-visible";

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration name for {@link #getUpdate()}. */
		String UPDATE = "update";

		/** Configuration name for {@link #getCanUpdate()}. */
		String CAN_UPDATE = "can-update";

		/** Configuration name for {@link #getAggregate()}. */
		String AGGREGATE = "aggregate";

		/** Configuration name for {@link #getComparator()}. */
		String COMPARATOR = "comparator";

		/** Configuration name for {@link #getTooltip()}. */
		String TOOLTIP = AbstractColumnDeclaration.Config.TOOLTIP;

		/** Configuration name for {@link #getCellClass()}. */
		String CELL_CLASS = AbstractColumnDeclaration.Config.CELL_CLASS;

		/** Configuration name for {@link #getDisplay()}. */
		String DISPLAY = AbstractColumnDeclaration.Config.DISPLAY;

		@Override
		@ClassDefault(DynamicColumns.class)
		Class<? extends DynamicColumns> getImplementationClass();

		/**
		 * The name every column of this declaration is named after.
		 *
		 * <p>
		 * The name of a column is this name, a dot, and the name of the object the column stands
		 * for - so the columns of this declaration stay apart from the other columns of the table,
		 * whichever objects they are built for.
		 * </p>
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * The type of the computed values, deciding how the columns display, sort and filter them.
		 *
		 * <p>
		 * All columns of this declaration show values of this type. Where they do not, the type is
		 * computed per column instead. Unset, and with no expression computing it either, nothing is
		 * known about the values and the columns show them by their display label.
		 * </p>
		 */
		@Name(TYPE)
		@Nullable
		TLModelPartRef getType();

		/**
		 * Whether a cell holds a collection of values of the declared type rather than a single
		 * one.
		 */
		@Name(MULTIPLE)
		boolean getMultiple();

		/**
		 * The name of the variable holding the object a column stands for in the expressions of this
		 * declaration.
		 *
		 * <p>
		 * Unset, the variable is named {@value #DEFAULT_COLUMN_AS}. With
		 * <code>column-as="milestone"</code>, a value reads
		 * <code>$row.get(`my:Task#done`).contains($milestone)</code>. An input of the same name is a
		 * configuration error and is renamed with <code>as</code>.
		 * </p>
		 */
		@Name(COLUMN_AS)
		@Nullable
		String getColumnAs();

		/**
		 * The expression computing the type of the values of one column.
		 *
		 * <p>
		 * The expression sees the object the column stands for as the variable
		 * {@value #DEFAULT_COLUMN_AS}, or by the name given with {@value #COLUMN_AS}, and the values
		 * of the {@link #getInputs() inputs} as variables named after their channels or by their
		 * <code>as</code> names. The result is a reference to a model type, e.g.
		 * <code>`tl.core:Integer`</code>. A declaration whose columns all show values of the same
		 * type names that type instead of computing it.
		 * </p>
		 */
		@Name(COLUMN_TYPE)
		@Nullable
		@Supplies(value = DEFAULT_COLUMN_AS, renamedBy = COLUMN_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getColumnType();

		/**
		 * The default display width of every column of this declaration, in pixels.
		 *
		 * <p>
		 * This is the width the user sees until they resize a column themselves; from then on their
		 * own width is remembered. {@code 0} - the default - keeps the width the type of the values
		 * derives.
		 * </p>
		 */
		@Name(WIDTH)
		@Constraint(NonNegative.class)
		int getWidth();

		/**
		 * Whether the columns of this declaration stay read-only while the rows of the table are
		 * edited.
		 */
		@Name(READONLY)
		boolean getReadonly();

		/**
		 * The expression computing the objects the columns stand for, one column per object and in
		 * that order.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels or by their <code>as</code> names, so that the columns follow what is
		 * displayed elsewhere. An object is anything the other expressions of this declaration can
		 * work with - a model object, a date, a string.
		 * </p>
		 */
		@Name(COLUMNS)
		@Mandatory
		@NonNullable
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getColumns();

		/**
		 * The expression computing the name of one column.
		 *
		 * <p>
		 * The expression sees the variables of the {@link #getColumnType() column type}. The result
		 * is a text that stays the same across sessions, as the user's arrangement of a column is
		 * remembered under its name. Unset, the display label of the object names its column.
		 * </p>
		 */
		@Name(COLUMN_NAME)
		@Nullable
		@Supplies(value = DEFAULT_COLUMN_AS, renamedBy = COLUMN_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getColumnName();

		/**
		 * The expression computing the header label of one column.
		 *
		 * <p>
		 * The expression sees the variables of the {@link #getColumnType() column type}. The result
		 * is an internationalized text, a string, or any object shown by its display label. Unset,
		 * the object itself labels its column.
		 * </p>
		 */
		@Name(COLUMN_LABEL)
		@Nullable
		@Supplies(value = DEFAULT_COLUMN_AS, renamedBy = COLUMN_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getColumnLabel();

		/**
		 * Whether one column is shown from the start: an expression yielding a boolean.
		 *
		 * <p>
		 * The expression sees the variables of the {@link #getColumnType() column type}. A column
		 * for which the expression yields {@code false} is offered in the column selection only:
		 * the table starts out without it, and the user shows it by selecting it there. A user's own
		 * arrangement of the table takes precedence. Unset, every column is shown from the start.
		 * </p>
		 */
		@Name(COLUMN_VISIBLE)
		@Nullable
		@Supplies(value = DEFAULT_COLUMN_AS, renamedBy = COLUMN_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getColumnVisible();

		/**
		 * The expression computing the cell value.
		 *
		 * <p>
		 * The expression sees the row as the variable {@value RowAsConfig#DEFAULT_ROW_AS}, or by the
		 * name the table gives it with {@value RowAsConfig#ROW_AS}, and the variables of the
		 * {@link #getColumnType() column type}.
		 * </p>
		 */
		@Name(VALUE)
		@Mandatory
		@NonNullable
		@Supplies(value = RowAsConfig.DEFAULT_ROW_AS, renamedBy = RowAsConfig.ROW_AS)
		@Supplies(value = DEFAULT_COLUMN_AS, renamedBy = COLUMN_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getValue();

		/**
		 * The expression writing an edited cell value.
		 *
		 * <p>
		 * The expression sees the variables of the {@link #getValue() value}, and the entered value
		 * as the variable {@value ValueAsConfig#DEFAULT_VALUE_AS}, or by the name given with
		 * {@value ValueAsConfig#VALUE_AS}. Unset, the columns are displayed but not edited.
		 * </p>
		 */
		@Name(UPDATE)
		@Nullable
		@Supplies(value = RowAsConfig.DEFAULT_ROW_AS, renamedBy = RowAsConfig.ROW_AS)
		@Supplies(value = DEFAULT_COLUMN_AS, renamedBy = COLUMN_AS)
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getUpdate();

		/**
		 * Whether the cell of a row can be edited: an expression yielding whether the edit is
		 * offered there.
		 *
		 * <p>
		 * The expression sees the variables of the {@link #getValue() value}. Unset, every row of
		 * columns declaring an update is edited.
		 * </p>
		 */
		@Name(CAN_UPDATE)
		@Nullable
		@Supplies(value = RowAsConfig.DEFAULT_ROW_AS, renamedBy = RowAsConfig.ROW_AS)
		@Supplies(value = DEFAULT_COLUMN_AS, renamedBy = COLUMN_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getCanUpdate();

		/**
		 * What a column shows for a group of rows: an expression whose result is displayed in the
		 * group's header row.
		 *
		 * <p>
		 * The expression sees the list of the member rows of the group as the variable
		 * {@value AbstractColumnDeclaration.Config#MEMBERS_VARIABLE}, and the variables of the
		 * {@link #getColumnType() column type}. The result is one value of the kind the column
		 * holds, displayed exactly as a cell of that column is. Unset, the group cells stay empty.
		 * </p>
		 */
		@Name(AGGREGATE)
		@Nullable
		@Supplies(AbstractColumnDeclaration.Config.MEMBERS_VARIABLE)
		@Supplies(value = DEFAULT_COLUMN_AS, renamedBy = COLUMN_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getAggregate();

		/**
		 * The order of the values of every column: an expression comparing two cell values, by
		 * which a column sorts the rows and orders the groups it forms.
		 *
		 * <p>
		 * The expression sees the two values as the variables
		 * {@value AbstractColumnDeclaration.Config#LEFT_VARIABLE} and
		 * {@value AbstractColumnDeclaration.Config#RIGHT_VARIABLE}, and the values of the
		 * {@link #getInputs() inputs}. It yields a negative number, zero, or a positive number, as
		 * the first value comes before, together with, or after the second one - the result of
		 * TL-Script's <code>compare</code> function, so that an order by a key of the values is
		 * written <code>compare($left.get(`m:Type#position`), $right.get(`m:Type#position`))</code>.
		 * The values are never empty; rows without a value are ordered apart from them, a group
		 * without a value comes last. Unset, the columns keep the order derived from the type of
		 * their values.
		 * </p>
		 */
		@Name(COMPARATOR)
		@Nullable
		@Supplies(AbstractColumnDeclaration.Config.LEFT_VARIABLE)
		@Supplies(AbstractColumnDeclaration.Config.RIGHT_VARIABLE)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getComparator();

		/**
		 * The tooltip of a cell of every column: an expression computing what the user reads when
		 * pointing at the cell.
		 *
		 * <p>
		 * The expression sees the variables of the {@link #getValue() value} - the row and the
		 * object the column stands for - and the value of the cell as the variable
		 * {@value ValueAsConfig#DEFAULT_VALUE_AS}, or by the name given with
		 * {@value ValueAsConfig#VALUE_AS}. In the header row of a group formed by a column, it is
		 * evaluated for the value of the group, without a row. It yields a text or an
		 * internationalized text; nothing at all leaves the cell without a tooltip.
		 * </p>
		 */
		@Name(TOOLTIP)
		@Supplies(value = RowAsConfig.DEFAULT_ROW_AS, renamedBy = RowAsConfig.ROW_AS)
		@Supplies(value = DEFAULT_COLUMN_AS, renamedBy = COLUMN_AS)
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getTooltip();

		/**
		 * The CSS class of a cell of every column: an expression computing the class from the row,
		 * the object the column stands for and the value of the cell.
		 *
		 * <p>
		 * The expression sees the variables of the {@link #getValue() value} - the row and the
		 * object the column stands for - and the value of the cell as the variable
		 * {@value ValueAsConfig#DEFAULT_VALUE_AS}, or by the name given with
		 * {@value ValueAsConfig#VALUE_AS}. Its result is put on the cell as the one of a single
		 * column's {@link AbstractColumnDeclaration.Config#getCellClass() cell class}: nothing or an
		 * empty string gives the cell no class, a list all of its members, any other value its
		 * text. A cell the user may not read and the header row of a group get no class.
		 * </p>
		 */
		@Name(CELL_CLASS)
		@Supplies(value = RowAsConfig.DEFAULT_ROW_AS, renamedBy = RowAsConfig.ROW_AS)
		@Supplies(value = DEFAULT_COLUMN_AS, renamedBy = COLUMN_AS)
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getCellClass();

		/**
		 * How a cell of every column displays its value, in place of the display the type of the
		 * values derives.
		 *
		 * <p>
		 * The display is given the value of the cell, as the one of a single column's
		 * {@link AbstractColumnDeclaration.Config#getDisplay() display}: an empty cell, a cell the
		 * user may not read and a cell being edited keep the display of the type. Unset, the cells
		 * show their values as the type of the values derives.
		 * </p>
		 */
		@Name(DISPLAY)
		PolymorphicConfiguration<ReactControlProvider> getDisplay();

	}

	private final String _name;

	private final int _width;

	private final boolean _readonly;

	private final ColumnExport _export;

	private final Function<ColumnResolution, DynamicColumnSet> _columns;

	/** @see #inputs() */
	private final List<InputRef> _inputs;

	/** How the cells display their values, {@code null} for the display their type derives. */
	private final ReactControlProvider _display;

	/**
	 * Creates a {@link DynamicColumns} showing one column per object of the given set.
	 *
	 * @param name
	 *        The name every column is named after, ahead of the name of the object it stands for.
	 * @param width
	 *        The default display width of every column in pixels, {@code 0} to keep the width the
	 *        type of its values derives.
	 * @param readonly
	 *        Whether the columns stay read-only while the rows of the table are edited.
	 * @param export
	 *        How every column takes part in an export of the table.
	 * @param columns
	 *        The columns to show, computed for what the table resolves its columns against.
	 */
	public DynamicColumns(String name, int width, boolean readonly, ColumnExport export,
			Function<ColumnResolution, DynamicColumnSet> columns) {
		this(name, width, readonly, export, columns, List.of());
	}

	/**
	 * Creates a {@link DynamicColumns} showing one column per object of the given set, computed
	 * with the values of the given channels.
	 *
	 * @param name
	 *        The name every column is named after, ahead of the name of the object it stands for.
	 * @param width
	 *        The default display width of every column in pixels, {@code 0} to keep the width the
	 *        type of its values derives.
	 * @param readonly
	 *        Whether the columns stay read-only while the rows of the table are edited.
	 * @param export
	 *        How every column takes part in an export of the table.
	 * @param columns
	 *        The columns to show, computed for what the table resolves its columns against.
	 * @param inputs
	 *        The channels the given columns are computed with, see {@link #inputs()}.
	 */
	public DynamicColumns(String name, int width, boolean readonly, ColumnExport export,
			Function<ColumnResolution, DynamicColumnSet> columns, List<InputRef> inputs) {
		this(name, width, readonly, export, columns, inputs, null);
	}

	/**
	 * Creates a {@link DynamicColumns} showing one column per object of the given set, computed
	 * with the values of the given channels, whose cells display their values through the given
	 * display.
	 *
	 * @param name
	 *        The name every column is named after, ahead of the name of the object it stands for.
	 * @param width
	 *        The default display width of every column in pixels, {@code 0} to keep the width the
	 *        type of its values derives.
	 * @param readonly
	 *        Whether the columns stay read-only while the rows of the table are edited.
	 * @param export
	 *        How every column takes part in an export of the table.
	 * @param columns
	 *        The columns to show, computed for what the table resolves its columns against.
	 * @param inputs
	 *        The channels the given columns are computed with, see {@link #inputs()}.
	 * @param display
	 *        How the cells display their values, {@code null} for the display their type derives,
	 *        see {@link ColumnSetup#display()}.
	 */
	public DynamicColumns(String name, int width, boolean readonly, ColumnExport export,
			Function<ColumnResolution, DynamicColumnSet> columns, List<InputRef> inputs,
			ReactControlProvider display) {
		_name = name;
		_width = width;
		_readonly = readonly;
		_export = export;
		_columns = columns;
		_inputs = inputs;
		_display = display;
	}

	/**
	 * Creates a {@link DynamicColumns} from configuration.
	 */
	@CalledByReflection
	public DynamicColumns(InstantiationContext context, Config config) {
		this(config.getName(), config.getWidth(), config.getReadonly(), ColumnExport.of(context, config),
			scripted(context, config), config.getInputs(), context.getInstance(config.getDisplay()));
	}

	/**
	 * The columns the declared functions compute, reporting a declaration that says what its values
	 * are in more than one way - or that edits values of a type nothing says.
	 */
	private static Function<ColumnResolution, DynamicColumnSet> scripted(InstantiationContext context,
			Config config) {
		if (config.getType() != null && config.getColumnType() != null) {
			context.error("The type of the values is either named or computed, not both: columns '"
				+ config.getName() + "'.");
		}
		if (config.getUpdate() != null && config.getType() == null && config.getColumnType() == null) {
			context.error("Edited columns must declare the type of their values, so that it is known"
				+ " which control enters them: columns '" + config.getName() + "'.");
		}
		return new ScriptedColumns(config);
	}

	@Override
	public List<String> declaredNames() {
		return List.of();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The channels every function of the declaration reads, the one computing the set of columns
	 * included.
	 * </p>
	 */
	@Override
	public List<InputRef> inputs() {
		return _inputs;
	}

	@Override
	public List<ColumnSetup> resolve(ColumnResolution scope) {
		return columns(_columns.apply(scope), scope);
	}

	/**
	 * The columns standing for the objects of the given set: one column per object and in that
	 * order, each named after this declaration and the object it stands for, and each displayed as
	 * this declaration says.
	 *
	 * @param set
	 *        What the columns show.
	 * @param scope
	 *        What the columns are resolved against.
	 */
	public List<ColumnSetup> columns(DynamicColumnSet set, ColumnResolution scope) {
		List<ColumnSetup> result = new ArrayList<>();
		for (Object column : set.columns()) {
			if (column == null) {
				continue;
			}
			ColumnType type = set.type(column);
			Function<Object, Object> value = set.value(column);
			CellEditing editing = _readonly ? null
				: ValueCellEditing.forUpdate(type, value, set.update(column), set.canUpdate(column));

			result.add(new ColumnSetup(_name + NAME_SEPARATOR + set.name(column), set.label(column), type, value,
				scope.context(), ColumnBinding.TYPE_DERIVED, _width, editing, set.aggregate(column),
				set.comparator(column), !set.visible(column), _export, set.tooltip(column), set.cellClass(column),
				_display));
		}
		return result;
	}

	/**
	 * The columns a configured {@link DynamicColumns} shows, computed by its declared functions.
	 */
	private static class ScriptedColumns implements Function<ColumnResolution, DynamicColumnSet> {

		private final QueryExecutor _columns;

		private final QueryExecutor _columnName;

		private final QueryExecutor _columnLabel;

		private final QueryExecutor _columnType;

		private final QueryExecutor _columnVisible;

		private final QueryExecutor _aggregate;

		private final QueryExecutor _comparator;

		private final QueryExecutor _tooltip;

		private final QueryExecutor _cellClass;

		private final ColumnFunctions _functions;

		private final TLModelPartRef _typeRef;

		private final boolean _multiple;

		private final String _name;

		/**
		 * Creates a {@link ScriptedColumns} from the declared expressions.
		 */
		ScriptedColumns(Config config) {
			_columns = InputScript.compile(config, Config.COLUMNS);
			_columnName = InputScript.compile(config, Config.COLUMN_NAME);
			_columnLabel = InputScript.compile(config, Config.COLUMN_LABEL);
			_columnType = InputScript.compile(config, Config.COLUMN_TYPE);
			_columnVisible = InputScript.compile(config, Config.COLUMN_VISIBLE);
			_aggregate = InputScript.compile(config, Config.AGGREGATE);
			_comparator = InputScript.compile(config, Config.COMPARATOR);
			_tooltip = InputScript.compile(config, Config.TOOLTIP);
			_cellClass = InputScript.compile(config, Config.CELL_CLASS);
			_functions = ColumnFunctions.compile(config, Config.VALUE, Config.UPDATE, Config.CAN_UPDATE);
			_typeRef = config.getType();
			_multiple = config.getMultiple();
			_name = config.getName();
		}

		@Override
		public DynamicColumnSet apply(ColumnResolution scope) {
			return new Resolved(scope);
		}

		/**
		 * The columns for one scope: the objects the declared function yields there, and what the
		 * other declared functions say about each of them.
		 */
		private class Resolved implements DynamicColumnSet {

			private final ColumnFunctions.Resolved _cells;

			private final List<?> _objects;

			private final ColumnType _declaredType;

			/**
			 * Creates a {@link Resolved} for the given scope.
			 */
			Resolved(ColumnResolution scope) {
				_cells = _functions.resolve(scope);
				_objects = SearchExpression.asList(_columns.execute(_cells.inputs()));
				_declaredType = _typeRef == null ? ColumnType.UNRESOLVED
					: ColumnType.of(_typeRef.resolveType(scope.model()), _multiple);
			}

			@Override
			public List<?> columns() {
				return _objects;
			}

			@Override
			public String name(Object column) {
				if (_columnName == null) {
					return MetaLabelProvider.INSTANCE.getLabel(column);
				}
				return SearchExpression.asString(_columnName.execute(_cells.inputs(column)));
			}

			@Override
			public ResKey label(Object column) {
				Object label = _columnLabel == null ? column : _columnLabel.execute(_cells.inputs(column));
				if (label instanceof ResKey key) {
					return key;
				}
				return ResKey.text(MetaLabelProvider.INSTANCE.getLabel(label));
			}

			@Override
			public ColumnType type(Object column) {
				if (_columnType == null) {
					return _declaredType;
				}
				Object type = _columnType.execute(_cells.inputs(column));
				if (type == null) {
					return ColumnType.UNRESOLVED;
				}
				if (!(type instanceof TLType)) {
					throw new IllegalStateException("The columns of '" + _name + "' compute the type " + type
						+ ", which is not a model type.");
				}
				return ColumnType.of((TLType) type, _multiple);
			}

			@Override
			public boolean visible(Object column) {
				if (_columnVisible == null) {
					return true;
				}
				Object visible = _columnVisible.execute(_cells.inputs(column));
				return visible == null || SearchExpression.isTrue(visible);
			}

			@Override
			public Function<Object, Object> value(Object column) {
				return _cells.value(column);
			}

			@Override
			public BiConsumer<Object, Object> update(Object column) {
				return _cells.update(column);
			}

			@Override
			public Predicate<Object> canUpdate(Object column) {
				return _cells.canUpdate(column);
			}

			@Override
			public Function<List<Object>, Object> aggregate(Object column) {
				if (_aggregate == null) {
					return null;
				}
				QueryExecutor aggregate = _aggregate;
				return members -> aggregate.execute(_cells.inputs(members, column));
			}

			@Override
			public Comparator<Object> comparator(Object column) {
				if (_comparator == null) {
					return null;
				}
				QueryExecutor comparator = _comparator;
				return AbstractColumnDeclaration
					.order((value1, value2) -> comparator.execute(_cells.inputs(value1, value2)));
			}

			@Override
			public BiFunction<Object, Object, String> tooltip(Object column) {
				if (_tooltip == null) {
					return null;
				}
				QueryExecutor tooltip = _tooltip;
				return (row, value) -> AbstractColumnDeclaration
					.tooltipText(tooltip.execute(_cells.inputs(row, column, value)));
			}

			@Override
			public BiFunction<Object, Object, String> cellClass(Object column) {
				if (_cellClass == null) {
					return null;
				}
				QueryExecutor cellClass = _cellClass;
				return (row, value) -> AbstractColumnDeclaration
					.cssClass(cellClass.execute(_cells.inputs(row, column, value)));
			}

		}

	}

}
