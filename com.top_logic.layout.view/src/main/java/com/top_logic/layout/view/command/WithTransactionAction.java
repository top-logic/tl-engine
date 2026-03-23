/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.ReactContext;

/**
 * {@link ViewAction} that wraps inner actions in a KB transaction.
 *
 * <p>
 * All inner actions execute within the same transaction. If any action throws, the transaction is
 * rolled back. The result of the last inner action is returned.
 * </p>
 *
 * <p>
 * Example: Copy a transient object to persistent within a transaction:
 * </p>
 *
 * <pre>
 * &lt;with-transaction&gt;
 *   &lt;execute-script function="obj -&gt; $obj.copy(transient: false)"/&gt;
 * &lt;/with-transaction&gt;
 * </pre>
 */
public class WithTransactionAction implements ViewAction {

	/**
	 * Configuration for {@link WithTransactionAction}.
	 */
	@TagName("with-transaction")
	public interface Config extends PolymorphicConfiguration<WithTransactionAction> {

		@Override
		@ClassDefault(WithTransactionAction.class)
		Class<? extends WithTransactionAction> getImplementationClass();

		/**
		 * Inner actions to execute within the transaction.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<ViewAction>> getActions();
	}

	private final List<ViewAction> _actions;

	/**
	 * Creates a new {@link WithTransactionAction}.
	 */
	@CalledByReflection
	public WithTransactionAction(InstantiationContext context, Config config) {
		_actions = config.getActions().stream()
			.map(c -> context.getInstance(c))
			.toList();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		try {
			Object current = input;
			for (ViewAction action : _actions) {
				current = action.execute(context, current);
			}
			tx.commit();
			return current;
		} finally {
			tx.rollback();
		}
	}
}
