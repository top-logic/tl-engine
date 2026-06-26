/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import java.io.StringReader;
import java.util.Collections;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.parser.TokenMgrError;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that compiles and evaluates the TL-Script source passed as input and returns
 * the result.
 *
 * <p>
 * The script is parsed and executed without an argument (a global expression such as {@code 1 + 2}
 * or <code>all(`my.module:MyType`)</code>); the evaluation result becomes the action's output and is
 * typically written to a channel feeding a {@link ScriptResultTable}. Evaluation runs without an
 * ambient transaction, so a script that modifies persistent state has no committed effect.
 * </p>
 *
 * @implNote Compilation uses {@link SearchExpressionParser} and {@link QueryExecutor#compile(Expr)};
 *           parse and evaluation failures are surfaced as a {@link TopLogicException}.
 */
public class EvaluateScriptAction implements ViewAction {

	/**
	 * Configuration for {@link EvaluateScriptAction}.
	 *
	 * <p>
	 * Referenced by {@code class=} in the console view rather than claiming a global
	 * {@code @TagName}.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<EvaluateScriptAction> {

		@Override
		@ClassDefault(EvaluateScriptAction.class)
		Class<? extends EvaluateScriptAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link EvaluateScriptAction} from configuration.
	 */
	@CalledByReflection
	public EvaluateScriptAction(InstantiationContext context, Config config) {
		// No configuration options.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		String source = input == null ? "" : input.toString();
		if (source.isBlank()) {
			return Collections.emptyList();
		}

		Expr expr;
		try {
			expr = new SearchExpressionParser(new StringReader(source)).expr();
		} catch (ParseException | TokenMgrError ex) {
			throw new TopLogicException(I18NConstants.ERROR_SCRIPT_PARSE__MSG.fill(ex.getMessage()), ex);
		}

		try {
			return QueryExecutor.compile(expr).execute((Object) null);
		} catch (RuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_SCRIPT_EVALUATION__MSG.fill(ex.getMessage()), ex);
		}
	}
}
