/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.component.WithCommitMessage;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewCommitMessage;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.WithTransaction;
import com.top_logic.util.error.TopLogicException;

/**
 * Base class for {@link ViewAction}s evaluating a TL-Script, optionally in a committed transaction.
 *
 * <p>
 * A committed evaluation is annotated with the configured commit message, see
 * {@link ViewCommitMessage}.
 * </p>
 *
 * @see EvaluateScriptAction Evaluating a script given in the configuration.
 * @see EvaluateDynamicScriptAction Evaluating a script entered in the TL-Script console.
 */
abstract class AbstractScriptAction implements ViewAction, WithTransaction {

	private final boolean _inTransaction;

	private final ViewCommitMessage _commitMessage;

	/**
	 * Creates an {@link AbstractScriptAction}.
	 *
	 * @param context
	 *        The context the action is instantiated in.
	 * @param config
	 *        Decides whether the evaluation is committed, and with which message.
	 * @param defaultMessage
	 *        The commit message used when none is configured, filled with the label of the model
	 *        given to {@link #evaluate(QueryExecutor, Object, Object)}; {@code null} for the message
	 *        naming the command and that model.
	 */
	protected <C extends WithTransaction.Config & WithCommitMessage> AbstractScriptAction(InstantiationContext context,
			C config, ResKey1 defaultMessage) {
		_inTransaction = config.isInTransaction();
		_commitMessage = new ViewCommitMessage(context, config, defaultMessage);
	}

	/**
	 * Evaluates the given script with the given argument and returns its result.
	 *
	 * @param script
	 *        The compiled script.
	 * @param argument
	 *        The value passed to the script.
	 * @param model
	 *        The object the commit message names.
	 */
	protected final Object evaluate(QueryExecutor script, Object argument, Object model) {
		try (Transaction tx = beginTransaction(_inTransaction, _commitMessage.create(model))) {
			Object result = script.execute(argument);
			tx.commit();
			return result;
		} catch (RuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_SCRIPT_EVALUATION__MSG.fill(ex.getMessage()), ex);
		}
	}

}
