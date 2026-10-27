/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.component.WithCommitMessage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that wraps inner actions in a KB transaction.
 *
 * <p>
 * All inner actions execute within the same transaction, with the arguments of the enclosing chain.
 * If any action throws, the transaction is rolled back. The result of the last inner action is the
 * result of this action.
 * </p>
 *
 * <p>
 * The changes are committed with the configured commit message, filled with the label of the
 * default argument of the chain. Without one, the message names the label of the command the action
 * runs in and that argument.
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
 * Inner actions must not change the state of the view either: a failing commit rolls back the
 * model changes, but not a channel written or a dialog closed. Such actions are placed into the
 * {@link Config#getWithResult()} of the transaction and run after a successful commit. They see
 * the result of the transaction in addition to the arguments of the chain and may suspend and abort
 * like any action of the chain.
 * </p>
 *
 * <p>
 * Example: Copy a transient object to persistent within a transaction and select the created
 * object once the transaction is committed:
 * </p>
 *
 * <pre>
 * &lt;with-transaction&gt;
 *   &lt;execute-script function="$obj.copy(transient: false)"/&gt;
 *   &lt;with-result&gt;
 *     &lt;write-channel name="selection"/&gt;
 *   &lt;/with-result&gt;
 * &lt;/with-transaction&gt;
 * </pre>
 */
@InApp
public class WithTransactionAction implements ViewAction {

	/**
	 * Configuration for {@link WithTransactionAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<WithTransactionAction>, WithCommitMessage,
			WithResultActions, ActionScopeDefinition {

		/** Access to the default methods of this interface for the configuration proxy. */
		Lookup LOOKUP = MethodHandles.lookup();

		/** The tag name of {@link WithTransactionAction} in an action list. */
		String TAG_NAME = "with-transaction";

		/** Configuration name for {@link #getActions()}. */
		String ACTIONS = "actions";

		@Override
		@ClassDefault(WithTransactionAction.class)
		Class<? extends WithTransactionAction> getImplementationClass();

		/**
		 * Inner actions to execute within the transaction.
		 *
		 * <p>
		 * These actions may only read and modify the model. An action changing the state of the
		 * view belongs to the actions run after the commit, see {@link #getWithResult()}.
		 * </p>
		 */
		@Name(ACTIONS)
		@DefaultContainer
		@Options(fun = AllInAppImplementations.class)
		@TreeProperty
		List<PolymorphicConfiguration<? extends ViewAction>> getActions();

		/**
		 * The actions to run once the transaction is committed, e.g. to select the created object
		 * and close the dialog.
		 *
		 * <p>
		 * They see the result of the last of the {@link #getActions()}. They do not run when one of
		 * the inner actions fails or the commit fails.
		 * </p>
		 */
		@Override
		WithResult getWithResult();

		/**
		 * The actions in the transaction run in a {@link ActionScope#transactional() transactional}
		 * scope, the ones {@link #getWithResult() after the commit} in the scope of the transaction
		 * itself.
		 */
		@Override
		default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
			if (ActionScopes.contains(getActions(), part)) {
				return outer.transactional();
			}
			return outer;
		}
	}

	private final List<ViewAction> _actions;

	private final ViewCommitMessage _commitMessage;

	/**
	 * Creates a new {@link WithTransactionAction}.
	 */
	@CalledByReflection
	public WithTransactionAction(InstantiationContext context, Config config) {
		_actions = ViewActions.instantiate(context, config.getActions(), ActionScope.of(context).transactional());
		_commitMessage = new ViewCommitMessage(context, config);
	}

	@Override
	public boolean appliesFormState() {
		return ViewActions.appliesFormState(_actions);
	}

	/**
	 * The rules the actions run in the transaction {@link ViewAction#getIntrinsicRule() bring of
	 * their own}, combined.
	 */
	@Override
	public ViewExecutabilityRule getIntrinsicRule() {
		return ViewActions.intrinsicRule(_actions);
	}

	/**
	 * Destructive as soon as one of the actions run in the transaction is.
	 */
	@Override
	public ButtonTone getTone() {
		return ViewActions.tone(_actions);
	}

	/**
	 * Runs the inner actions with the given value as their single argument named
	 * {@link GenericViewCommand#DEFAULT_ARGUMENT}.
	 */
	@Override
	public Object execute(ReactContext context, Object input) {
		return executeInTransaction(context, ActionArguments.of(GenericViewCommand.DEFAULT_ARGUMENT, input));
	}

	/**
	 * Runs the inner actions in a transaction and resumes with the result of the last one once the
	 * transaction is committed.
	 */
	@Override
	public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
		continuation.resume(executeInTransaction(context, arguments == null ? ActionArguments.NONE : arguments));
	}

	private Object executeInTransaction(ReactContext context, ActionArguments arguments) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction(_commitMessage.create(arguments.value()));
		try {
			Object result = arguments.value();
			for (ViewAction action : _actions) {
				RequireSyncContinuation continuation = new RequireSyncContinuation(action);
				action.execute(context, arguments, continuation);
				result = continuation.resumedValue();
			}
			tx.commit();
			return result;
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
