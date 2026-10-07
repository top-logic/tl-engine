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
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.component.WithCommitMessage;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.parser.TokenMgrError;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.WithTransaction;
import com.top_logic.tool.boundsec.CommandHandler.ConfirmConfig.VisibleIf;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} of the TL-Script console, compiling and evaluating the script source it
 * receives as input.
 *
 * <p>
 * The source is the text a developer entered into the console's editor. Since the action executes
 * the value it is given, it belongs to that console alone: it claims no tag and is not offered as a
 * building block, and the console view references it by {@code class=}. A view running a script of
 * its own uses {@link EvaluateScriptAction}, whose script is part of the view definition instead of
 * arriving as data.
 * </p>
 *
 * <p>
 * The changes are committed with the configured commit message, by default with a message naming
 * the executed script.
 * </p>
 *
 * @implNote The source is parsed per invocation by a {@link SearchExpressionParser}, since it
 *           changes with every entry; syntax errors are reported to the developer entering them.
 */
public class EvaluateDynamicScriptAction extends AbstractScriptAction {

	/**
	 * Configuration for {@link EvaluateDynamicScriptAction}.
	 */
	public interface Config extends PolymorphicConfiguration<EvaluateDynamicScriptAction>, WithTransaction.Config,
			WithCommitMessage {

		@Override
		@ClassDefault(EvaluateDynamicScriptAction.class)
		Class<? extends EvaluateDynamicScriptAction> getImplementationClass();

		/**
		 * The message to annotate to the changes the script performs.
		 *
		 * <p>
		 * If not set, the message names the executed script.
		 * </p>
		 *
		 * <p>
		 * A message may contain the placeholder '{0}' that is replaced with the source of the
		 * script. Only used when the evaluation runs in a transaction.
		 * </p>
		 */
		@Override
		@DynamicMode(fun = VisibleIf.class, args = @Ref(TRANSACTION))
		ResKey1 getCommitMessage();
	}

	/**
	 * Creates a new {@link EvaluateDynamicScriptAction} from configuration.
	 */
	@CalledByReflection
	public EvaluateDynamicScriptAction(InstantiationContext context, Config config) {
		super(context, config, com.top_logic.model.search.ui.I18NConstants.EXECUTED_CUSTOM_SCRIPT__SCRIPT);
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		String source = input == null ? "" : input.toString();
		if (source.isBlank()) {
			return Collections.emptyList();
		}
		return evaluate(QueryExecutor.compile(parse(source)), null, source);
	}

	private Expr parse(String source) {
		try {
			return new SearchExpressionParser(new StringReader(source)).expr();
		} catch (ParseException | TokenMgrError ex) {
			throw new TopLogicException(I18NConstants.ERROR_SCRIPT_PARSE__MSG.fill(ex.getMessage()), ex);
		}
	}

}
