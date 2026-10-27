/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.form;

import java.lang.reflect.Method;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.IFunction0;
import com.top_logic.basic.reflect.JavaTypeUtil;
import com.top_logic.layout.ScriptFunction0;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.ValueConverter;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link Function0} which executes a configured {@link Expr}.
 * 
 * @see ScriptFunction0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("TL-Script")
@Hidden
public class ScriptFunction0Impl<R> extends Function0<R> implements ConfiguredInstance<ScriptFunction0Impl.Config<R>> {

	/**
	 * Configuration of a {@link ScriptFunction0Impl}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<r1> extends WithExpression, WithImplementedType, ScriptFunction0<r1> {

		@ClassDefault(ScriptFunction0Impl.class)
		@Override
		Class<? extends IFunction0<r1>> getImplementationClass();

	}

	/**
	 * The method implemented by the script, see
	 * {@link ScriptImplementation#resultConverter(java.lang.reflect.Type, Method)}.
	 */
	private static final Method APPLY = JavaTypeUtil.getAbstractMethods(IFunction0.class).get(0);

	private final Config<R> _config;

	private QueryExecutor _query;

	private final ValueConverter _result;

	/**
	 * Creates a {@link ScriptFunction0Impl} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ScriptFunction0Impl(InstantiationContext context, Config<R> config) {
		_config = config;
		_query = QueryExecutor.compile(config.getExpression());
		_result = ScriptImplementation.resultConverter(config.getImplementedType(), APPLY);
	}

	@Override
	public Config<R> getConfig() {
		return _config;
	}

	@SuppressWarnings("unchecked")
	@Override
	public R apply() {
		return (R) ScriptImplementation.convertResult(_result, _query.execute());
	}

}

