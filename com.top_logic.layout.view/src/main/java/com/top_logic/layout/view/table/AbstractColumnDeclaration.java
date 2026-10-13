/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;

/**
 * Base of the {@link ColumnDeclaration}s that describe a single column, with the properties every
 * such column has: its label, its filter, its width, whether it can be edited, the order it sorts
 * the table in, the order of its values, what it aggregates over a group of rows, and the display
 * annotations it states over the ones of its values' attribute or type.
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
			extends ColumnDeclaration.Config<I>, AnnotatedConfig<TLAttributeAnnotation> {

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

		/** Configuration name for {@link #getComparator()}. */
		String COMPARATOR = "comparator";

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

		/**
		 * The order of this column's values: a function comparing two cell values, by which the
		 * column sorts the rows and orders the groups it forms.
		 *
		 * <p>
		 * The function receives two values of the column and yields a negative number, zero, or a
		 * positive number, as the first value comes before, together with, or after the second one
		 * - the contract of the comparator of TL-Script's <code>sort</code> function. An order by
		 * a key of the values is written
		 * <code>comparator(p -&gt; $p.get(`m:Page#position`))</code>. The function is never passed
		 * an empty value; rows without a value are ordered apart from it, a group without a value
		 * comes last. Unset (the default) keeps the order derived from the type of the column's
		 * values.
		 * </p>
		 */
		@Name(COMPARATOR)
		Expr getComparator();

	}

	private final ResKey _label;

	private final ColumnBinding _binding;

	private final boolean _readonly;

	private final SortDirection _sort;

	private final int _width;

	private final Function<List<Object>, Object> _aggregate;

	private final Comparator<Object> _comparator;

	private final boolean _hiddenByDefault;

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
		_aggregate = aggregate(config.getAggregate());
		_comparator = comparator(config.getComparator());
		_hiddenByDefault = false;
		_annotations = config.getAnnotations().isEmpty() ? null : config;
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
		_hiddenByDefault = hiddenByDefault;
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
		return new ColumnSetup(name, _label != null ? _label : label, annotatedType, value, scope.context(), _binding,
			_width, _readonly ? null : editing, _aggregate, _comparator, _hiddenByDefault);
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

	/**
	 * The order the given comparison function establishes, {@code null} for a column keeping its
	 * type-derived order.
	 *
	 * <p>
	 * The expression is compiled once here and applied to every pair of values compared.
	 * </p>
	 */
	private static Comparator<Object> comparator(Expr expr) {
		if (expr == null) {
			return null;
		}
		QueryExecutor executor = QueryExecutor.compile(expr);
		return order((value1, value2) -> executor.execute(value1, value2));
	}

	/**
	 * The order a TL-Script comparison function establishes, in the contract of the comparator of
	 * TL-Script's <code>sort</code> function: the number it yields, taken as an integer, decides,
	 * and no result counts as equal.
	 *
	 * @param comparison
	 *        Applies the comparison function to two values.
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
