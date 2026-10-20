/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonTone;

/**
 * Lightweight functional building block for composable view commands.
 *
 * <p>
 * Unlike {@link ViewCommand}, a {@link ViewAction} has no configuration for label, image,
 * executability, or other presentation concerns. It is purely functional: takes an input, produces
 * a result. Actions are chained in a {@link GenericViewCommand}; all actions of a chain run with
 * the same {@link ActionArguments arguments}, supplied by what triggered the chain. The result of
 * an action does not reach the actions following it, but the follow-up actions nested into it (see
 * {@link WithResultActions}).
 * </p>
 *
 * <p>
 * The chain always drives actions through
 * {@link #execute(ReactContext, ActionArguments, Continuation)}. A regular action processes a single
 * value - the {@link ActionArguments#value() default argument} - and only implements the
 * synchronous {@link #execute(ReactContext, Object)}; it is adapted automatically. An action that
 * needs to <em>suspend</em> the chain (e.g. to await a confirmation dialog) extends
 * {@link InterruptibleViewAction} and implements the {@link Continuation}-based form instead. An
 * action working on all arguments extends {@link ArgumentsViewAction}.
 * </p>
 */
public interface ViewAction {

	/**
	 * Executes this action synchronously.
	 *
	 * @param context
	 *        The React context.
	 * @param input
	 *        The value this action processes: the {@link ActionArguments#value() default argument} of
	 *        the chain, or the argument selected by {@link WithValueArgument}.
	 * @return The result of this action, seen by its follow-up actions.
	 */
	Object execute(ReactContext context, Object input);

	/**
	 * Executes this action within the chain, continuing via the given {@link Continuation}.
	 *
	 * <p>
	 * The default adapts the synchronous {@link #execute(ReactContext, Object)} - compute a value
	 * and {@link Continuation#resume(Object) resume} immediately. Actions that may suspend or abort
	 * the chain override this (see {@link InterruptibleViewAction}) and call
	 * {@link Continuation#resume(Object)} / {@link Continuation#abort()} themselves, possibly later.
	 * </p>
	 *
	 * @param context
	 *        The React context.
	 * @param input
	 *        The value this action processes, see {@link #execute(ReactContext, Object)}.
	 * @param continuation
	 *        Used to continue (or cancel) the chain.
	 */
	default void execute(ReactContext context, Object input, Continuation continuation) {
		continuation.resume(execute(context, input));
	}

	/**
	 * Executes this action with the arguments of its chain.
	 *
	 * <p>
	 * This is the method the chain calls. The default processes the
	 * {@link ActionArguments#value() default argument} with
	 * {@link #execute(ReactContext, Object, Continuation)}. Actions working on all arguments
	 * override it (see {@link ArgumentsViewAction}).
	 * </p>
	 *
	 * @param context
	 *        The React context.
	 * @param arguments
	 *        The arguments of the chain; {@code null} is taken as {@link ActionArguments#NONE}.
	 * @param continuation
	 *        Used to continue (or cancel) the chain, {@link Continuation#resume(Object) resumed} with
	 *        the result of this action.
	 */
	default void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
		execute(context, arguments == null ? null : arguments.value(), continuation);
	}

	/**
	 * Whether this action applies the values entered into the enclosing form.
	 *
	 * <p>
	 * A command containing such an action cannot succeed while the form displays validation
	 * errors, so it is disabled until they are fixed.
	 * </p>
	 *
	 * @see FormValid
	 */
	default boolean appliesFormState() {
		return false;
	}

	/**
	 * The executability this action brings of its own, decided by what the action knows about the
	 * operation it performs rather than by what the command using it configured.
	 *
	 * <p>
	 * An action performing a model operation that the current user may be refused - deleting its
	 * input, say - says so here, so that the command offering it is hidden or disabled before the
	 * operation would fail. A command running the action combines this rule with its
	 * {@link ViewCommand.Config#getExecutability() configured rules} (see
	 * {@link ViewCommand#getIntrinsicRule()}), and the rule takes part in the same way: it is
	 * {@link ContextDependentRule#bind(com.top_logic.layout.view.ViewContext) bound} to the context of
	 * the command and {@link ObservableRule observed} while its button is attached.
	 * </p>
	 *
	 * <p>
	 * The rule decides before the command runs. It therefore receives the <em>command's</em> input,
	 * not the value this action processes when the chain runs.
	 * </p>
	 *
	 * @return A rule of this action's own, {@link ViewExecutabilityRule#ALWAYS_EXECUTABLE} for an
	 *         action that leaves the decision to the command. A fresh instance per call, since a
	 *         bound rule belongs to the one command model it was built for.
	 *
	 * @see ViewActions#intrinsicRule(java.util.List)
	 */
	default ViewExecutabilityRule getIntrinsicRule() {
		return ViewExecutabilityRule.ALWAYS_EXECUTABLE;
	}

	/**
	 * The kind of action this is.
	 *
	 * <p>
	 * {@link ButtonTone#DANGER} for an action that destroys or discards what the user has - deleting
	 * its input, say. A command running the action takes this tone (see
	 * {@link ViewActions#tone(java.util.List)}), so that every surface offering the command - its
	 * button, a menu entry, the confirmation asking for it - shows it as destructive, without the
	 * author choosing a color.
	 * </p>
	 *
	 * @return {@link ButtonTone#DEFAULT} for an ordinary action.
	 */
	default ButtonTone getTone() {
		return ButtonTone.DEFAULT;
	}

	/**
	 * Whether this action changes the state of the view rather than (or in addition to) the model:
	 * it writes a channel, opens or closes a dialog, navigates, tells the user something, hands
	 * something to the client, or switches the session.
	 *
	 * <p>
	 * Such an action must not run inside the transaction of a {@link WithTransactionAction}: when
	 * the commit fails, the model changes are rolled back, but the view would keep what the action
	 * did - e.g. a channel holding an object that does not exist. Configuring it there is reported
	 * as an error; it belongs to the actions run after the commit (see
	 * {@link WithTransactionAction.Config#getAfterCommit()}).
	 * </p>
	 *
	 * @return {@code false} for an action that only reads or modifies the model, which is the
	 *         default.
	 *
	 * @see ActionScope#isTransactional()
	 */
	default boolean changesViewState() {
		return false;
	}
}
