/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonTone;

/**
 * Runs a chain of {@link ViewAction}s with the {@link ActionArguments arguments} supplied by what
 * triggered the chain.
 *
 * <p>
 * Every action of the chain runs with the same arguments: the result an action
 * {@link Continuation#resume(Object) resumes} with does not reach the actions following it, but only
 * its follow-up actions (see {@link WithResultActions}), which run with the arguments extended by
 * that result.
 * </p>
 *
 * <p>
 * The chain is driven by {@link Continuation}: a synchronous action resumes inline (the chain
 * completes within the call), while an interruptible action may resume later (e.g. after a dialog)
 * or abort (skipping the remaining actions and running, newest first, the compensations registered
 * via {@link Continuation#onAbort(Runnable)}). A thrown {@link RuntimeException} runs the same
 * compensation unwind before propagating.
 * </p>
 *
 * <p>
 * An action that is itself composed of actions - a branch such as {@link IfAction} or
 * {@link SwitchAction} - runs the actions it contains as a nested chain of the chain it is part of,
 * see {@link #nest(ReactContext, List, ActionArguments, Continuation)}.
 * </p>
 *
 * <p>
 * The <em>completion value</em> of a chain is the result of its last action, or the
 * {@link ActionArguments#value() default argument} for a chain without actions. It is what a nested
 * chain hands to the action it is nested in, and what the caller of
 * {@link #run(ReactContext, List, ActionArguments, Consumer)} receives.
 * </p>
 */
public class ViewActionChain {

	/**
	 * Runs {@code actions} with the given arguments.
	 *
	 * @param arguments
	 *        The arguments every action of the chain runs with; {@code null} for
	 *        {@link ActionArguments#NONE}.
	 * @param onComplete
	 *        Invoked exactly once when the chain settles normally - with the completion value of the
	 *        chain (see {@link ViewActionChain}) when the chain ran past its last action, or with
	 *        {@code null} when it was aborted. It is <em>not</em> invoked when an action throws (the
	 *        exception propagates after compensations run). May be {@code null}.
	 */
	public static void run(ReactContext context, List<ViewAction> actions, ActionArguments arguments,
			Consumer<Object> onComplete) {
		run(context, actions, arguments, onComplete, null);
	}

	/**
	 * Runs {@code actions} with the given arguments, telling the caller when the chain has come to
	 * an end whichever way it ended.
	 *
	 * @param arguments
	 *        The arguments every action of the chain runs with; {@code null} for
	 *        {@link ActionArguments#NONE}.
	 * @param onComplete
	 *        Invoked when the chain settles normally, see
	 *        {@link #run(ReactContext, List, ActionArguments, Consumer)}.
	 * @param onSettled
	 *        Invoked exactly once as soon as the chain cannot continue any more: it completed, it
	 *        was aborted, or an action threw. Invoked before the call returns for a chain that
	 *        settles within it, and from whatever interaction hands the chain back otherwise - a
	 *        caller that has to tell the two apart marks the call and inspects the mark after it
	 *        returned. May be {@code null}.
	 */
	public static void run(ReactContext context, List<ViewAction> actions, ActionArguments arguments,
			Consumer<Object> onComplete, Runnable onSettled) {
		ActionArguments args = nonNull(arguments);
		Settlement settlement = new Settlement(onSettled);
		Deque<Runnable> compensations = new ArrayDeque<>();
		runFrom(context, actions, 0, args, args.value(), ButtonTone.DEFAULT, compensations, settlement,
			value -> {
				settlement.settle();
				settled(onComplete, value);
			},
			() -> {
				runCompensations(compensations);
				settlement.settle();
				settled(onComplete, null);
			});
	}

	/**
	 * Runs {@code actions} as a nested chain of the action that is executing right now, handing the
	 * completion value of the nested chain to the enclosing chain.
	 *
	 * <p>
	 * The nested chain runs with the given arguments, and the action it is nested in resumes with
	 * the result of the nested chain's last action - with the
	 * {@link ActionArguments#value() default argument} when there is no action to run. An abort
	 * inside the nested chain aborts the enclosing chain, and the compensations registered inside
	 * the nested chain take their place in the enclosing chain's unwind: whenever the enclosing chain
	 * is aborted or fails, they run - newest first - before the compensations of the actions that
	 * precede the nesting one. A step of the nested chain sees, as {@link Continuation#tone() the
	 * tone of what follows it}, what follows it in the nested chain together with what follows the
	 * nesting action.
	 * </p>
	 *
	 * @param arguments
	 *        The arguments every action of the nested chain runs with.
	 * @param continuation
	 *        The continuation of the action that contains the nested chain.
	 */
	public static void nest(ReactContext context, List<ViewAction> actions, ActionArguments arguments,
			Continuation continuation) {
		ActionArguments args = nonNull(arguments);
		Deque<Runnable> compensations = new ArrayDeque<>();
		continuation.onAbort(() -> runCompensations(compensations));
		runFrom(context, actions, 0, args, args.value(), continuation.tone(), compensations,
			settlementOf(continuation),
			continuation::resume,
			() -> {
				runCompensations(compensations);
				continuation.abort();
			});
	}

	/**
	 * Runs an action and, once it resumed, the given follow-up actions as a nested chain over its
	 * result.
	 *
	 * <p>
	 * The step resumes with the result of the action after the follow-up actions are done. An abort
	 * of the action or inside the follow-up actions aborts the enclosing chain; compensations
	 * registered by the action and inside the follow-up actions join the enclosing chain's unwind,
	 * see {@link #nest(ReactContext, List, ActionArguments, Continuation)}. The action sees, as the
	 * tone of what follows it, its follow-up actions together with what follows the step.
	 * </p>
	 *
	 * @param action
	 *        The action to run first.
	 * @param input
	 *        The arguments to run the action with.
	 * @param followUps
	 *        The actions to run over the action's result.
	 * @param followUpArguments
	 *        The arguments of the follow-up actions, computed from the result of the action.
	 * @param continuation
	 *        The continuation of the step.
	 */
	static void followUp(ReactContext context, ViewAction action, ActionArguments input,
			List<ViewAction> followUps, Function<Object, ActionArguments> followUpArguments,
			Continuation continuation) {
		Settlement settlement = settlementOf(continuation);
		ButtonTone afterAction =
			ViewActions.tone(followUps) == ButtonTone.DANGER ? ButtonTone.DANGER : continuation.tone();
		action.execute(context, input, new StepContinuation(settlement, continuation, afterAction,
			result -> nest(context, followUps, followUpArguments.apply(result),
				new StepContinuation(settlement, continuation, continuation.tone(),
					ignored -> continuation.resume(result)))));
	}

	/**
	 * The settlement of the chain the given continuation belongs to, so that a chain nested in it
	 * reports a failure to the caller that started the outermost chain.
	 */
	private static Settlement settlementOf(Continuation continuation) {
		if (continuation instanceof SettledContinuation chained) {
			return chained.settlement();
		}
		return new Settlement(null);
	}

	private static ActionArguments nonNull(ActionArguments arguments) {
		return arguments == null ? ActionArguments.NONE : arguments;
	}

	/**
	 * @param last
	 *        The result of the action before {@code index}, the completion value when no action
	 *        follows.
	 * @param after
	 *        The tone of what follows the chain being run: what follows the nesting action in the
	 *        enclosing chain, {@link ButtonTone#DEFAULT} for an outermost chain.
	 */
	private static void runFrom(ReactContext context, List<ViewAction> actions, int index,
			ActionArguments arguments, Object last, ButtonTone after, Deque<Runnable> compensations,
			Settlement settlement, Consumer<Object> onComplete, Runnable onAbort) {
		if (index >= actions.size()) {
			onComplete.accept(last);
			return;
		}
		ViewAction action = actions.get(index);
		ButtonTone rest = ViewActions.tone(actions.subList(index + 1, actions.size())) == ButtonTone.DANGER
			? ButtonTone.DANGER
			: after;
		ChainContinuation continuation = new ChainContinuation(settlement, compensations, rest,
			result -> runFrom(context, actions, index + 1, arguments, result, after, compensations, settlement,
				onComplete, onAbort),
			onAbort);
		try {
			action.execute(context, arguments, continuation);
		} catch (RuntimeException failure) {
			runCompensations(compensations);
			settlement.settle();
			throw failure;
		}
	}

	private static void settled(Consumer<Object> onComplete, Object value) {
		if (onComplete != null) {
			onComplete.accept(value);
		}
	}

	/**
	 * Runs (and removes) all registered compensations, newest first. Draining makes this safe to
	 * invoke from nested {@code catch} blocks during exception unwinding.
	 */
	private static void runCompensations(Deque<Runnable> compensations) {
		while (!compensations.isEmpty()) {
			compensations.pop().run();
		}
	}

	/**
	 * The {@link Continuation} of an action running in a chain.
	 *
	 * <p>
	 * Carries the {@link Settlement} of the chain it drives, so that a chain nested into it reaches
	 * the same settlement and the outermost caller hears of an end reached deep inside.
	 * </p>
	 */
	private static final class ChainContinuation implements SettledContinuation {

		private final Settlement _settlement;

		private final Deque<Runnable> _compensations;

		private final ButtonTone _tone;

		private final Consumer<Object> _onResume;

		private final Runnable _onAbort;

		private boolean _spent;

		ChainContinuation(Settlement settlement, Deque<Runnable> compensations, ButtonTone tone,
				Consumer<Object> onResume, Runnable onAbort) {
			_settlement = settlement;
			_compensations = compensations;
			_tone = tone;
			_onResume = onResume;
			_onAbort = onAbort;
		}

		@Override
		public Settlement settlement() {
			return _settlement;
		}

		@Override
		public void resume(Object value) {
			spend();
			_onResume.accept(value);
		}

		@Override
		public void abort() {
			spend();
			_onAbort.run();
		}

		@Override
		public void onAbort(Runnable compensation) {
			if (_spent) {
				throw new IllegalStateException("onAbort() after the action already continued.");
			}
			_compensations.push(compensation);
		}

		@Override
		public ButtonTone tone() {
			return _tone;
		}

		private void spend() {
			if (_spent) {
				throw new IllegalStateException("Continuation already used (resume/abort called twice).");
			}
			_spent = true;
		}
	}

	/**
	 * A {@link Continuation} knowing the {@link Settlement} of the chain it belongs to.
	 */
	private interface SettledContinuation extends Continuation {

		/**
		 * The settlement of the chain this continuation drives.
		 */
		Settlement settlement();
	}

	/**
	 * The {@link Continuation} of a part of a {@link #followUp(ReactContext, ViewAction,
	 * ActionArguments, List, Function, Continuation) step with follow-up actions}: the action itself,
	 * or the follow-up actions.
	 *
	 * <p>
	 * Hands aborts and compensations directly to the continuation of the step, which is resumed only
	 * when the follow-up actions are done.
	 * </p>
	 */
	private static final class StepContinuation implements SettledContinuation {

		private final Settlement _settlement;

		private final Continuation _step;

		private final ButtonTone _tone;

		private final Consumer<Object> _onResume;

		private boolean _spent;

		StepContinuation(Settlement settlement, Continuation step, ButtonTone tone, Consumer<Object> onResume) {
			_settlement = settlement;
			_step = step;
			_tone = tone;
			_onResume = onResume;
		}

		@Override
		public Settlement settlement() {
			return _settlement;
		}

		@Override
		public void resume(Object value) {
			spend();
			_onResume.accept(value);
		}

		@Override
		public void abort() {
			spend();
			_step.abort();
		}

		@Override
		public void onAbort(Runnable compensation) {
			if (_spent) {
				throw new IllegalStateException("onAbort() after the action already continued.");
			}
			_step.onAbort(compensation);
		}

		@Override
		public ButtonTone tone() {
			return _tone;
		}

		private void spend() {
			if (_spent) {
				throw new IllegalStateException("Continuation already used (resume/abort called twice).");
			}
			_spent = true;
		}
	}

	/**
	 * Tells the caller of a chain, once, that the chain has come to an end.
	 *
	 * <p>
	 * Every outcome passes through here - completion, abort and the failure unwind, which runs at
	 * each level of the chain it propagates through - so the notification is guarded to happen only
	 * the first time. The guard holds across interactions, since the end may be reached by a thread
	 * other than the one that started the chain.
	 * </p>
	 */
	private static final class Settlement {

		private final Runnable _onSettled;

		private boolean _settled;

		Settlement(Runnable onSettled) {
			_onSettled = onSettled;
		}

		/**
		 * Reports the chain settled, unless that was already reported.
		 */
		void settle() {
			synchronized (this) {
				if (_settled) {
					return;
				}
				_settled = true;
			}
			if (_onSettled != null) {
				_onSettled.run();
			}
		}
	}
}
