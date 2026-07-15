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
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that wraps inner actions in a KB transaction.
 *
 * <p>
 * All inner actions execute within the same transaction. If any action throws, the transaction is
 * rolled back. The result of the last inner action is returned.
 * </p>
 *
 * <p>
 * Inner actions must complete synchronously: an action may not suspend (e.g. a
 * {@link ConfirmAction}) inside a transaction, since that would hold the transaction open across an
 * asynchronous wait. Such a configuration fails fast with a clear error - place the guard
 * <em>before</em> the {@code <with-transaction>} instead.
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
				RequireSyncContinuation continuation = new RequireSyncContinuation(action);
				action.execute(context, current, continuation);
				current = continuation.resumedValue();
			}
			tx.commit();
			return current;
		} finally {
			tx.rollback();
		}
	}

	/**
	 * {@link Continuation} for the non-interruptible transaction context: an inner action must
	 * {@link #resume(Object) resume} synchronously. Suspending or aborting inside a transaction is
	 * rejected so the transaction is never held open across an asynchronous wait.
	 */
	private static final class RequireSyncContinuation implements Continuation {

		private final ViewAction _action;

		private boolean _resumed;

		private Object _value;

		RequireSyncContinuation(ViewAction action) {
			_action = action;
		}

		@Override
		public void resume(Object value) {
			_resumed = true;
			_value = value;
		}

		@Override
		public void abort() {
			throw suspendNotAllowed();
		}

		@Override
		public void onAbort(Runnable compensation) {
			// Inside a transaction, the KB rollback is the compensation; nothing to register.
		}

		Object resumedValue() {
			if (!_resumed) {
				throw suspendNotAllowed();
			}
			return _value;
		}

		private TopLogicException suspendNotAllowed() {
			return new TopLogicException(
				I18NConstants.ERROR_SUSPEND_NOT_ALLOWED_IN_TRANSACTION__ACTION.fill(
					_action.getClass().getSimpleName()));
		}
	}
}
