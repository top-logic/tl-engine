/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import java.util.List;

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
import com.top_logic.layout.view.command.WithResultActions;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScript;
import com.top_logic.layout.view.command.ActionScriptVariables;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.search.providers.WithTransaction;
import com.top_logic.tool.boundsec.CommandHandler.ConfirmConfig.VisibleIf;

/**
 * {@link ViewAction} evaluating the TL-Script given in its configuration.
 *
 * <p>
 * The script uses the arguments of the chain as variables, without declaring them: in a command with
 * the input channel {@code item}, <code>$item.get(`my.module:MyType#name`)</code> works on the
 * object the channel holds, while a global expression such as <code>all(`my.module:MyType`)</code>
 * uses none. The evaluation result is the result of the action, seen by its follow-up actions.
 * </p>
 *
 * <p>
 * With the transaction option set, the evaluation runs inside a committed transaction, so a script
 * creating, modifying or deleting persistent objects has a lasting effect; otherwise it runs without
 * an ambient transaction and any modification is discarded. The changes are committed with the
 * configured commit message, by default with a message naming the command and its main argument.
 * </p>
 *
 * @implNote The script is compiled on first use through
 *           {@link ActionScript#compile(Expr, ActionScope, java.util.List, String...)} and kept for
 *           further invocations.
 */
@InApp
public class EvaluateScriptAction extends AbstractScriptAction {

	/**
	 * Configuration for {@link EvaluateScriptAction}.
	 */
	@TagName("evaluate-script")
	public interface Config extends PolymorphicConfiguration<EvaluateScriptAction>, WithTransaction.Config,
			WithCommitMessage, WithResultActions {

		/** Configuration name for {@link #getScript()}. */
		String SCRIPT = "script";

		@Override
		@ClassDefault(EvaluateScriptAction.class)
		Class<? extends EvaluateScriptAction> getImplementationClass();

		/**
		 * The script to evaluate.
		 *
		 * <p>
		 * Uses the arguments of the chain as variables named after them, without declaring them.
		 * </p>
		 */
		@Name(SCRIPT)
		@Mandatory
		@ScriptContextVariables(ActionScriptVariables.class)
		Expr getScript();

		/**
		 * The message to annotate to the changes the script performs.
		 *
		 * <p>
		 * If not set, the message names the label of the command and the command's main argument.
		 * </p>
		 *
		 * <p>
		 * A message may contain the placeholder '{0}' that is replaced with the label of the
		 * command's main argument. Only used when the evaluation runs in a transaction.
		 * </p>
		 */
		@Override
		@DynamicMode(fun = VisibleIf.class, args = @Ref(TRANSACTION))
		ResKey1 getCommitMessage();
	}

	private final Expr _script;

	private final ActionScope _scope;

	private ActionScript _compiled;

	/**
	 * Creates a new {@link EvaluateScriptAction} from configuration.
	 */
	@CalledByReflection
	public EvaluateScriptAction(InstantiationContext context, Config config) {
		super(context, config, null);
		_script = config.getScript();
		_scope = ActionScope.of(context);
	}

	@Override
	public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
		ActionScript script = compiled();
		continuation.resume(evaluate(() -> script.execute(context, arguments), arguments.value()));
	}

	private ActionScript compiled() {
		if (_compiled == null) {
			_compiled = ActionScript.compile(_script, _scope, List.of());
		}
		return _compiled;
	}

}
