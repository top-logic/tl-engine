/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
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
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;

/**
 * The {@code <dynamic-columns>} of a table: one column per element of a computed set of objects.
 *
 * <p>
 * A table of employees showing one column per month, a table of tasks showing one column per
 * milestone: which columns there are is decided by data, not by the configuration. Each of those
 * columns stands for one object - a month, a milestone - and shows what the row holds for it, which
 * the value function computes from the row and that object.
 * </p>
 *
 * <p>
 * The columns are named after the declaration and the object each of them stands for, so that the
 * user's arrangement of a column is remembered under a name that stays with it. They share what the
 * declaration says about all of them: the type of their values (or a function computing it per
 * column), their display width and whether they stay read-only while the rows are edited. Their
 * filter is the one their type derives; a filter of their own is not declared, as there is no
 * single column to declare it for.
 * </p>
 *
 * <p>
 * The columns are those the function yields when the table is built. A table whose rows follow an
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
	public interface Config extends ColumnDeclaration.Config<DynamicColumns>, Inputs {

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

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration name for {@link #getUpdate()}. */
		String UPDATE = "update";

		/** Configuration name for {@link #getCanUpdate()}. */
		String CAN_UPDATE = "can-update";

		/** Configuration name for {@link #getAggregate()}. */
		String AGGREGATE = "aggregate";

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
		 * computed per column instead. Unset, and with no function computing it either, nothing is
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
		 * The function computing the type of the values of one column, receiving the object that
		 * column stands for.
		 *
		 * <p>
		 * The values of the declared inputs come first, in declaration order. The result is a
		 * reference to a model type, e.g. <code>`tl.core:Integer`</code>. A declaration whose
		 * columns all show values of the same type names that type instead of computing it.
		 * </p>
		 */
		@Name(COLUMN_TYPE)
		@Nullable
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
		 * The function computing the objects the columns stand for, one column per object and in
		 * that order.
		 *
		 * <p>
		 * The values of the declared inputs come first, in declaration order, so that the columns
		 * follow what is displayed elsewhere. An object is anything the other functions of this
		 * declaration can work with - a model object, a date, a string.
		 * </p>
		 */
		@Name(COLUMNS)
		@Mandatory
		@NonNullable
		Expr getColumns();

		/**
		 * The function computing the name of one column, receiving the object that column stands
		 * for.
		 *
		 * <p>
		 * The values of the declared inputs come first, in declaration order. The result is a text
		 * that stays the same across sessions, as the user's arrangement of a column is remembered
		 * under its name. Unset, the display label of the object names its column.
		 * </p>
		 */
		@Name(COLUMN_NAME)
		@Nullable
		Expr getColumnName();

		/**
		 * The function computing the header label of one column, receiving the object that column
		 * stands for.
		 *
		 * <p>
		 * The values of the declared inputs come first, in declaration order. The result is an
		 * internationalized text, a string, or any object shown by its display label. Unset, the
		 * object itself labels its column.
		 * </p>
		 */
		@Name(COLUMN_LABEL)
		@Nullable
		Expr getColumnLabel();

		/**
		 * The function computing the cell value, receiving the row and the object the column stands
		 * for as its last two arguments.
		 *
		 * <p>
		 * The values of the declared inputs come first, in declaration order.
		 * </p>
		 */
		@Name(VALUE)
		@Mandatory
		@NonNullable
		Expr getValue();

		/**
		 * The function writing an edited cell value, receiving the row, the object the column
		 * stands for and the entered value as its last three arguments.
		 *
		 * <p>
		 * The values of the declared inputs come first, in declaration order. Unset, the columns
		 * are displayed but not edited.
		 * </p>
		 */
		@Name(UPDATE)
		@Nullable
		Expr getUpdate();

		/**
		 * Whether the cell of a row can be edited: a function of the row and the object the column
		 * stands for, yielding whether the edit is offered there.
		 *
		 * <p>
		 * The values of the declared inputs come first, in declaration order. Unset, every row of
		 * columns declaring an update is edited.
		 * </p>
		 */
		@Name(CAN_UPDATE)
		@Nullable
		Expr getCanUpdate();

		/**
		 * What a column shows for a group of rows: a function of the member rows of the group and
		 * the object the column stands for, whose result is displayed in the group's header row.
		 *
		 * <p>
		 * The values of the declared inputs come first, in declaration order. The result is one
		 * value of the kind the column holds, displayed exactly as a cell of that column is. Unset,
		 * the group cells stay empty.
		 * </p>
		 */
		@Name(AGGREGATE)
		@Nullable
		Expr getAggregate();

	}

	private final String _name;

	private final int _width;

	private final boolean _readonly;

	private final Function<ColumnResolution, DynamicColumnSet> _columns;

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
	 * @param columns
	 *        The columns to show, computed for what the table resolves its columns against.
	 */
	public DynamicColumns(String name, int width, boolean readonly,
			Function<ColumnResolution, DynamicColumnSet> columns) {
		_name = name;
		_width = width;
		_readonly = readonly;
		_columns = columns;
	}

	/**
	 * Creates a {@link DynamicColumns} from configuration.
	 */
	@CalledByReflection
	public DynamicColumns(InstantiationContext context, Config config) {
		this(config.getName(), config.getWidth(), config.getReadonly(), scripted(context, config));
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
				scope.context(), ColumnBinding.TYPE_DERIVED, _width, editing, set.aggregate(column), false));
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

		private final QueryExecutor _aggregate;

		private final ColumnFunctions _functions;

		private final TLModelPartRef _typeRef;

		private final boolean _multiple;

		private final String _name;

		/**
		 * Creates a {@link ScriptedColumns} from the declared expressions.
		 */
		ScriptedColumns(Config config) {
			_columns = QueryExecutor.compile(config.getColumns());
			_columnName = QueryExecutor.compileOptional(config.getColumnName());
			_columnLabel = QueryExecutor.compileOptional(config.getColumnLabel());
			_columnType = QueryExecutor.compileOptional(config.getColumnType());
			_aggregate = QueryExecutor.compileOptional(config.getAggregate());
			_functions = ColumnFunctions.compile(config.getValue(), config.getUpdate(), config.getCanUpdate(),
				config.getInputs());
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

		}

	}

}
