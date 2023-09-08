/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.Accessor;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Configurable {@link Accessor} using TL-Script expressions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class AccessorByExpression<C extends AccessorByExpression.Config<?>> extends AbstractConfiguredInstance<C>
		implements Accessor<Object> {

	private QueryExecutor _getter;

	private QueryExecutor _setter;

	/**
	 * Configuration options for {@link AccessorByExpression}.
	 */
	public interface Config<I extends AccessorByExpression<?>> extends PolymorphicConfiguration<I> {

		/**
		 * The getter function retrieving the column value.
		 * 
		 * <p>
		 * The expression is expected to be a two-argument function receiving the row object as
		 * first argument and the column name as second argument.
		 * </p>
		 */
		@Mandatory
		Expr getGetter();

		/**
		 * Optional setter function.
		 * 
		 * <p>
		 * The expression is expected to be a three-argument function
		 * <code>row -> value -> column -> {...}</code> receiving the row object as first argument,
		 * the new value to set as second argument and the column name as third argument.
		 * </p>
		 * 
		 * @see Accessor#setValue(Object, String, Object) Note: The argument order of the TL-Script
		 *      function does not match the argument order of the signature of
		 *      {@link Accessor#setValue(Object, String, Object)} to allow using only a two-argument
		 *      function, if the column name does not care.
		 */
		Expr getSetter();
	}

	/**
	 * Creates a {@link AccessorByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AccessorByExpression(InstantiationContext context, C config) {
		super(context, config);
		
		_getter = QueryExecutor.compile(config.getGetter());
		_setter = QueryExecutor.compileOptional(config.getSetter());
	}

	@Override
	public Object getValue(Object object, String property) {
		return _getter.execute(object, property);
	}

	@Override
	public void setValue(Object object, String property, Object value) {
		if (_setter == null) {
			throw new UnsupportedOperationException("Column '" + property + "' can not be updated, no setter defined.");
		}
		_setter.execute(object, value, property);
	}

}
