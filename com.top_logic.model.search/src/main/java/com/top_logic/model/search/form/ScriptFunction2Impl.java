/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.form;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.func.Function2;
import com.top_logic.basic.func.IFunction2;
import com.top_logic.layout.ScriptFunction2;
import com.top_logic.layout.form.values.edit.editor.PlainEditor;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link Function2} which executes a configured {@link Expr}.
 * 
 * @see ScriptFunction2
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("TL-Script function")
public class ScriptFunction2Impl<R, A1, A2> extends Function2<R, A1, A2>
		implements ConfiguredInstance<ScriptFunction2Impl.Config<R, A1, A2>> {

	/**
	 * Configuration of a {@link ScriptFunction2Impl}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<r1, a1, a2>
			extends WithExpression, ScriptFunction2<r1, a1, a2> {

		@ClassDefault(ScriptFunction2Impl.class)
		@Override
		Class<? extends IFunction2<r1, a1, a2>> getImplementationClass();

	}

	private final Config<R, A1, A2> _config;

	private QueryExecutor _query;

	/**
	 * Creates a {@link ScriptFunction2Impl} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ScriptFunction2Impl(InstantiationContext context, Config<R, A1, A2> config) {
		_config = config;
		_query = QueryExecutor.compile(config.getExpression());
	}

	@Override
	public Config<R, A1, A2> getConfig() {
		return _config;
	}

	@SuppressWarnings("unchecked")
	@Override
	public R apply(A1 arg1, A2 arg2) {
		return (R) _query.execute(arg1, arg2);
	}

	/**
	 * {@link PlainEditor} for configuration of a {@link Config}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class Editor extends ScriptFunctionEditor {

		/** Singleton {@link Editor} instance. */
		public static final Editor INSTANCE = new Editor();

		@Override
		protected Class<? extends WithExpression> scriptFunctionType() {
			return Config.class;
		}

	}

}

