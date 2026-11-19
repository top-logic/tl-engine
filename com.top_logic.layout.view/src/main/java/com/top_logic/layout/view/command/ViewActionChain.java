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
		run(context, actions, arguments, ButtonTone.DEFAULT, onComplete, null);
	}

	/**
	 * Runs {@code actions} for a caller that knows what the chain stands for, and that follows
	 * whether an action holds the chain.
	 *
	 * @param arguments
	 *        The arguments every action of the chain runs with; {@code null} for
	 *        {@link ActionArguments#NONE}.
	 * @param after
	 *        The tone of what follows the chain: {@link ButtonTone#DANGER} for the chain of a
	 *        command that is destructive by its {@link CommandCliques#tone(String) clique}, so that
	 *        every step of the chain sees a destructive {@link Continuation#tone() tone of what
	 *        follows it}.
	 * @param onComplete
	 *        Invoked when the chain settles normally, see
	 *        {@link #run(ReactContext, List, ActionArguments, Consumer)}.
	 * @param holds
	 *        Told whenever an action comes to hold the chain and whenever it hands the chain back,
	 *        see {@link HoldObserver}. May be {@code null}.
	 */
	public static void run(ReactContext context, List<ViewAction> actions, ActionArguments arguments,
			ButtonTone after, Consumer<Object> onComplete, HoldObserver holds) {
		ActionArguments args = nonNull(arguments);
		Activity activity = new Activity(holds);
		Deque<Runnable> compensations = new ArrayDeque<>();
		activity.run(() -> runFrom(context, actions, 0, args, args.value(), after, compensations, activity,
			value -> {
				activity.settle();
				settled(onComplete, value);
			},
			() -> {
				runCompensations(compensations);
				activity.settle();
				settled(onComplete, null);
			}));
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
		Activity activity = activityOf(continuation);
		activity.run(() -> runFrom(context, actions, 0, args, args.value(), continuation.tone(), compensations,
			activity,
			continuation::resume,
			() -> {
				runCompensations(compensations);
				continuation.abort();
			}));
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
		Activity activity = activityOf(continuation);
		ButtonTone afterAction =
			ViewActions.tone(followUps) == ButtonTone.DANGER ? ButtonTone.DANGER : continuation.tone();
		activity.run(() -> action.execute(context, input, new StepContinuation(activity, continuation, afterAction,
			result -> followUp(context, followUps, followUpArguments.apply(result), result, continuation))));
	}

	/**
	 * Runs the follow-up actions of the action that is executing right now as a nested chain over
	 * its result, and resumes the action with that result once they are done.
	 *
	 * <p>
	 * For an action that chooses its follow-up actions itself, e.g. by how the work it waited for
	 * ended. The follow-up actions behave exactly like the ones a
	 * {@link WithResultActions#getWithResult() with-result} runs: an abort inside them aborts the
	 * enclosing chain, the compensations registered inside them join the enclosing chain's unwind
	 * (see {@link #nest(ReactContext, List, ActionArguments, Continuation)}), and the chain goes on
	 * with the actions following the action once they are done.
	 * </p>
	 *
	 * @param followUps
	 *        The actions to run over the result.
	 * @param arguments
	 *        The arguments of the follow-up actions, usually the arguments of the action extended by
	 *        its result.
	 * @param result
	 *        The result the action resumes with after the follow-up actions.
	 * @param continuation
	 *        The continuation of the action.
	 */
	public static void followUp(ReactContext context, List<ViewAction> followUps, ActionArguments arguments,
			Object result, Continuation continuation) {
		nest(context, followUps, arguments,
			new StepContinuation(activityOf(continuation), continuation, continuation.tone(),
				ignored -> continuation.resume(result)));
	}

	/**
	 * The {@link Activity} of the chain the given continuation belongs to, so that a chain nested in
	 * it reports to the caller that started the outermost chain.
	 */
	private static Activity activityOf(Continuation continuation) {
		if (continuation instanceof ChainedContinuation chained) {
			return chained.activity();
		}
		return new Activity(null);
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
			Activity activity, Consumer<Object> onComplete, Runnable onAbort) {
		if (index >= actions.size()) {
			onComplete.accept(last);
			return;
		}
		ViewAction action = actions.get(index);
		ButtonTone rest = ViewActions.tone(actions.subList(index + 1, actions.size())) == ButtonTone.DANGER
			? ButtonTone.DANGER
			: after;
		ChainContinuation continuation = new ChainContinuation(activity, compensations, rest,
			result -> runFrom(context, actions, index + 1, arguments, result, after, compensations, activity,
				onComplete, onAbort),
			onAbort);
		try {
			action.execute(context, arguments, continuation);
		} catch (RuntimeException failure) {
			runCompensations(compensations);
			activity.settle();
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
	 * Carries the {@link Activity} of the chain it drives, so that a chain nested into it reports to
	 * the same caller as the outermost chain.
	 * </p>
	 */
	private static final class ChainContinuation implements ChainedContinuation {

		private final Activity _activity;

		private final Deque<Runnable> _compensations;

		private final ButtonTone _tone;

		private final Consumer<Object> _onResume;

		private final Runnable _onAbort;

		private boolean _spent;

		ChainContinuation(Activity activity, Deque<Runnable> compensations, ButtonTone tone,
				Consumer<Object> onResume, Runnable onAbort) {
			_activity = activity;
			_compensations = compensations;
			_tone = tone;
			_onResume = onResume;
			_onAbort = onAbort;
		}

		@Override
		public Activity activity() {
			return _activity;
		}

		@Override
		public void resume(Object value) {
			spend();
			_activity.run(() -> _onResume.accept(value));
		}

		@Override
		public void abort() {
			spend();
			_activity.run(_onAbort);
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
	 * A {@link Continuation} knowing the {@link Activity} of the chain it belongs to.
	 */
	private interface ChainedContinuation extends Continuation {

		/**
		 * The activity of the chain this continuation drives.
		 */
		Activity activity();
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
	private static final class StepContinuation implements ChainedContinuation {

		private final Activity _activity;

		private final Continuation _step;

		private final ButtonTone _tone;

		private final Consumer<Object> _onResume;

		private boolean _spent;

		StepContinuation(Activity activity, Continuation step, ButtonTone tone, Consumer<Object> onResume) {
			_activity = activity;
			_step = step;
			_tone = tone;
			_onResume = onResume;
		}

		@Override
		public Activity activity() {
			return _activity;
		}

		@Override
		public void resume(Object value) {
			spend();
			_activity.run(() -> _onResume.accept(value));
		}

		@Override
		public void abort() {
			spend();
			_activity.run(_step::abort);
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
	 * Observer of whether an action holds a chain.
	 *
	 * <p>
	 * An action <em>holds</em> the chain when it returns from executing without handing the chain
	 * back - without {@link Continuation#resume(Object) resuming} or {@link Continuation#abort()
	 * aborting} it, e.g. while a job runs in the background or a question waits for its answer - and
	 * nothing of the chain runs any more. It hands the chain back when it resumes or aborts it, or
	 * runs follow-up actions over its result (see
	 * {@link ViewActionChain#followUp(ReactContext, List, ActionArguments, Object, Continuation)}).
	 * The chain is handed back <em>before</em> what continues it runs: the actions that follow see
	 * the chain not held. Once the continuation returns while an action holds the chain again, the
	 * chain is reported held again.
	 * </p>
	 *
	 * <p>
	 * The reports alternate, starting with {@link #held()}, and are delivered one at a time, also
	 * when the chain is handed back by a thread other than the one that ran the action holding it.
	 * A chain that comes to an end - completed, aborted, or failed - is not held. A chain that runs
	 * through within the call starting it is never reported.
	 * </p>
	 */
	public interface HoldObserver {

		/**
		 * An action holds the chain.
		 */
		void held();

		/**
		 * The action holding the chain has handed it back.
		 */
		void handedBack();
	}

	/**
	 * Tracks whether an action holds a chain and whether the chain has come to an end.
	 *
	 * <p>
	 * Every entry into the chain - the call that starts it, the hand-back of a continuation, a
	 * nested chain - runs through {@link #run(Runnable)}. The chain is held when the last of these
	 * calls returns while the chain has not come to an end. A call that fails (an exception escapes
	 * it) ends the chain: nothing can continue it after its continuation has been used up.
	 * </p>
	 *
	 * <p>
	 * Transitions are recorded under the lock, and delivered to the {@link HoldObserver} outside of
	 * it by one thread at a time: a thread finding another one delivering leaves its transition to
	 * that thread, which delivers until the reported state matches the recorded one.
	 * </p>
	 */
	private static final class Activity {

		private final HoldObserver _observer;

		/** Number of calls into the chain that have not returned yet. */
		private int _active;

		/** Whether the chain has come to an end. */
		private boolean _settled;

		/** Whether the chain is held. */
		private boolean _held;

		/** Whether the {@link #_observer} was last told that the chain is held. */
		private boolean _reported;

		/** Whether a thread is delivering transitions to the {@link #_observer}. */
		private boolean _reporting;

		Activity(HoldObserver observer) {
			_observer = observer;
		}

		/**
		 * Runs a part of the chain: its start, the continuation after an action handed it back, or
		 * a nested chain.
		 */
		void run(Runnable body) {
			synchronized (this) {
				_active++;
				_held = false;
			}
			report();

			boolean returned = false;
			try {
				body.run();
				returned = true;
			} finally {
				synchronized (this) {
					_active--;
					if (!returned) {
						_settled = true;
					}
					_held = _active == 0 && !_settled;
				}
				report();
			}
		}

		/**
		 * Records that the chain has come to an end, whichever way.
		 */
		synchronized void settle() {
			_settled = true;
			_held = false;
		}

		private void report() {
			if (_observer == null) {
				return;
			}
			while (true) {
				boolean held;
				synchronized (this) {
					if (_reporting || _reported == _held) {
						return;
					}
					_reporting = true;
					held = _held;
					_reported = held;
				}
				try {
					if (held) {
						_observer.held();
					} else {
						_observer.handedBack();
					}
				} finally {
					synchronized (this) {
						_reporting = false;
					}
				}
			}
		}
	}
}
