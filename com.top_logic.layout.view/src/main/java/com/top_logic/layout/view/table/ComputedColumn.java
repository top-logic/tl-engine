/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;

/**
 * The {@code <computed-column>} of a table: one column showing a value computed from the row.
 *
 * <p>
 * The value function is evaluated per row and yields what the cell holds. The declared type says
 * what kind of value that is, and with it how the column is displayed, sorted, filtered, searched
 * and how wide it starts out - so a computed column gets everything a column over a model attribute
 * gets. A column declaring no type shows its values by their display label.
 * </p>
 *
 * <p>
 * Such a column shows what the row does not hold: a total over the values of an attribute, a
 * difference between two dates, a value read through a chain of references. It is not edited - what
 * a computed value would mean when written back is nothing the computation says.
 * </p>
 */
@InApp
public class ComputedColumn extends AbstractColumnDeclaration {

	/** The tag a column over a computed value is declared with. */
	public static final String TAG_NAME = "computed-column";

	/**
	 * Configuration of a {@link ComputedColumn}.
	 */
	@TagName(TAG_NAME)
	public interface Config extends AbstractColumnDeclaration.Config<ComputedColumn> {

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for {@link #getMultiple()}. */
		String MULTIPLE = "multiple";

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		@Override
		@ClassDefault(ComputedColumn.class)
		Class<? extends ComputedColumn> getImplementationClass();

		/**
		 * The name of the column, under which the user's arrangement of it is remembered.
		 *
		 * <p>
		 * It is the name the rest of the table refers to the column by - a grouping, a filter
		 * criterion - and no two columns of a table may share it.
		 * </p>
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * The type of the computed values, deciding how the column displays, sorts and filters
		 * them.
		 *
		 * <p>
		 * Unset, nothing is known about the values and the column shows them by their display
		 * label.
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
		 * The function computing the cell value, receiving the row as its last argument.
		 *
		 * <p>
		 * The values of the declared inputs come first, in declaration order, so a column can show
		 * something that depends on what is displayed elsewhere.
		 * </p>
		 */
		@Name(VALUE)
		@Mandatory
		@NonNullable
		Expr getValue();

		/**
		 * References to channels whose values become the leading arguments of the value function,
		 * ahead of the row.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

	}

	private final String _name;

	private final TLModelPartRef _typeRef;

	private final boolean _multiple;

	private final QueryExecutor _value;

	private final List<ChannelRef> _inputs;

	/**
	 * Creates a {@link ComputedColumn} from configuration.
	 */
	@CalledByReflection
	public ComputedColumn(InstantiationContext context, Config config) {
		super(context, config);
		_name = config.getName();
		_typeRef = config.getType();
		_multiple = config.getMultiple();
		_value = QueryExecutor.compile(config.getValue());
		_inputs = config.getInputs();
	}

	@Override
	public List<String> declaredNames() {
		return List.of(_name);
	}

	@Override
	public List<ColumnSetup> resolve(ColumnResolution scope) {
		List<ViewChannel> inputs = new ArrayList<>(_inputs.size());
		for (ChannelRef ref : _inputs) {
			inputs.add(scope.context().resolveChannel(ref));
		}
		QueryExecutor value = _value;
		Function<Object, Object> cellValue = row -> value.execute(arguments(inputs, row));
		return List.of(setup(_name, ResKey.text(_name), columnType(scope), cellValue, scope));
	}

	/**
	 * What the column's values are: the declared type, resolved in the model the table's rows live
	 * in, and nothing for a column declaring no type.
	 */
	private ColumnType columnType(ColumnResolution scope) {
		if (_typeRef == null) {
			return ColumnType.UNRESOLVED;
		}
		TLType type = _typeRef.resolveType(scope.model());
		return ColumnType.of(type, _multiple);
	}

	/**
	 * The arguments of the value function: the current values of the declared inputs, in
	 * declaration order, and the row last.
	 *
	 * <p>
	 * The inputs are read here, every time a cell is computed, so a column over an input shows what
	 * that input holds now.
	 * </p>
	 */
	private static Object[] arguments(List<ViewChannel> inputs, Object row) {
		Object[] arguments = new Object[inputs.size() + 1];
		for (int n = 0; n < inputs.size(); n++) {
			arguments[n] = inputs.get(n).get();
		}
		arguments[inputs.size()] = row;
		return arguments;
	}

}
