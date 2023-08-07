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
import com.top_logic.basic.func.Function4;
import com.top_logic.basic.func.IFunction4;
import com.top_logic.layout.ScriptFunction4;
import com.top_logic.layout.form.values.edit.editor.PlainEditor;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link Function4} which executes a configured {@link Expr}.
 * 
 * @see ScriptFunction4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("TL-Script function")
public class ScriptFunction4Impl<R, A1, A2, A3, A4> extends Function4<R, A1, A2, A3, A4>
		implements ConfiguredInstance<ScriptFunction4Impl.Config<R, A1, A2, A3, A4>> {

	/**
	 * Configuration of a {@link ScriptFunction4Impl}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<r1, a1, a2, a3, a4> extends WithExpression, ScriptFunction4<r1, a1, a2, a3, a4> {

		@ClassDefault(ScriptFunction4Impl.class)
		@Override
		Class<? extends IFunction4<r1, a1, a2, a3, a4>> getImplementationClass();

	}

	private final Config<R, A1, A2, A3, A4> _config;

	private QueryExecutor _query;

	/**
	 * Creates a {@link ScriptFunction4Impl} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ScriptFunction4Impl(InstantiationContext context, Config<R, A1, A2, A3, A4> config) {
		_config = config;
		_query = QueryExecutor.compile(config.getExpression());
	}

	@Override
	public Config<R, A1, A2, A3, A4> getConfig() {
		return _config;
	}

	@SuppressWarnings("unchecked")
	@Override
	public R apply(A1 arg1, A2 arg2, A3 arg3, A4 arg4) {
		return (R) _query.execute(arg1, arg2, arg3, arg4);
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

