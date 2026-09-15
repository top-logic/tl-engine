/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;

/**
 * Base of the {@link ColumnDeclaration}s that describe a single column, with the properties every
 * such column has: its label, its filter, its width, whether it can be edited, the order it sorts
 * the table in, and what it aggregates over a group of rows.
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
	public interface Config<I extends AbstractColumnDeclaration> extends ColumnDeclaration.Config<I> {

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
		 * What this column shows for a group of rows: a function over the member rows of the group,
		 * whose result is displayed in the group's header row.
		 *
		 * <p>
		 * The function receives the list of the rows in the group and yields one value of the kind
		 * the column holds - the sum of a number column, the earliest of a date column - which is
		 * then displayed exactly as a cell of that column is. Unset (the default) leaves the
		 * column's group cell empty.
		 * </p>
		 */
		@Name(AGGREGATE)
		Expr getAggregate();

	}

	private final ResKey _label;

	private final ColumnBinding _binding;

	private final boolean _readonly;

	private final SortDirection _sort;

	private final int _width;

	private final Function<List<Object>, Object> _aggregate;

	private final boolean _hiddenByDefault;

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
		_aggregate = aggregate(config.getAggregate());
		_hiddenByDefault = false;
	}

	/**
	 * Creates the plain {@link AbstractColumnDeclaration} a table derives for a column no
	 * declaration of its own describes: the type-derived display, the label and width the column
	 * brings itself, no sorting and no aggregate.
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
		_hiddenByDefault = hiddenByDefault;
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
	 *        What the column's values are.
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
		return new ColumnSetup(name, _label != null ? _label : label, type, value, scope.context(), _binding,
			_width, _readonly ? null : editing, _aggregate, _hiddenByDefault);
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
	 * <p>
	 * The expression is compiled once here and applied to the member rows of every group.
	 * </p>
	 */
	private static Function<List<Object>, Object> aggregate(Expr expr) {
		if (expr == null) {
			return null;
		}
		QueryExecutor executor = QueryExecutor.compile(expr);
		return members -> executor.execute(members);
	}

}
