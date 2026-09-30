/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.WithTransaction;
import com.top_logic.util.error.TopLogicException;

/**
 * Base class for {@link ViewAction}s evaluating a TL-Script, optionally in a committed transaction.
 *
 * @see EvaluateScriptAction Evaluating a script given in the configuration.
 * @see EvaluateDynamicScriptAction Evaluating a script entered in the TL-Script console.
 */
abstract class AbstractScriptAction implements ViewAction, WithTransaction {

	private final boolean _inTransaction;

	/**
	 * Creates an {@link AbstractScriptAction}.
	 *
	 * @param config
	 *        Decides whether the evaluation is committed.
	 */
	protected AbstractScriptAction(WithTransaction.Config config) {
		_inTransaction = config.isInTransaction();
	}

	/**
	 * Evaluates the given script with the given argument and returns its result.
	 *
	 * @param script
	 *        The compiled script.
	 * @param argument
	 *        The value passed to the script.
	 */
	protected final Object evaluate(QueryExecutor script, Object argument) {
		try (Transaction tx = beginTransaction(_inTransaction)) {
			Object result = script.execute(argument);
			tx.commit();
			return result;
		} catch (RuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_SCRIPT_EVALUATION__MSG.fill(ex.getMessage()), ex);
		}
	}

}
