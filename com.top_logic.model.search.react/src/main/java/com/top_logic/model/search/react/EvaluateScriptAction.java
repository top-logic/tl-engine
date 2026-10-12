/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.component.WithCommitMessage;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.WithTransaction;
import com.top_logic.tool.boundsec.CommandHandler.ConfirmConfig.VisibleIf;

/**
 * {@link ViewAction} evaluating the TL-Script given in its configuration.
 *
 * <p>
 * The script is passed the command's input as its argument, so a function such as
 * <code>x -&gt; $x.get(`my.module:MyType#name`)</code> works on the input, while a global expression
 * such as <code>all(`my.module:MyType`)</code> ignores it. The evaluation result becomes the
 * action's output and is written to the channel the command names.
 * </p>
 *
 * <p>
 * With the transaction option set, the evaluation runs inside a committed transaction, so a script
 * creating, modifying or deleting persistent objects has a lasting effect; otherwise it runs without
 * an ambient transaction and any modification is discarded. The changes are committed with the
 * configured commit message, by default with a message naming the command and its input.
 * </p>
 *
 * @implNote The script is compiled on first use through {@link QueryExecutor#compile(Expr)} and kept
 *           for further invocations.
 */
@InApp
public class EvaluateScriptAction extends AbstractScriptAction {

	/**
	 * Configuration for {@link EvaluateScriptAction}.
	 */
	@TagName("evaluate-script")
	public interface Config extends PolymorphicConfiguration<EvaluateScriptAction>, WithTransaction.Config,
			WithCommitMessage {

		/** Configuration name for {@link #getScript()}. */
		String SCRIPT = "script";

		@Override
		@ClassDefault(EvaluateScriptAction.class)
		Class<? extends EvaluateScriptAction> getImplementationClass();

		/**
		 * The script to evaluate, receiving the command's input as its argument.
		 */
		@Name(SCRIPT)
		@Mandatory
		Expr getScript();

		/**
		 * The message to annotate to the changes the script performs.
		 *
		 * <p>
		 * If not set, the message names the label of the command and the command's input.
		 * </p>
		 *
		 * <p>
		 * A message may contain the placeholder '{0}' that is replaced with the label of the
		 * command's input. Only used when the evaluation runs in a transaction.
		 * </p>
		 */
		@Override
		@DynamicMode(fun = VisibleIf.class, args = @Ref(TRANSACTION))
		ResKey1 getCommitMessage();
	}

	private final Expr _script;

	private QueryExecutor _compiled;

	/**
	 * Creates a new {@link EvaluateScriptAction} from configuration.
	 */
	@CalledByReflection
	public EvaluateScriptAction(InstantiationContext context, Config config) {
		super(context, config, null);
		_script = config.getScript();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		return evaluate(compiled(), input, input);
	}

	private QueryExecutor compiled() {
		if (_compiled == null) {
			_compiled = QueryExecutor.compile(_script);
		}
		return _compiled;
	}

}
