/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * Memo of monotonic yes/no decisions that may depend on each other in cycles, such as the access
 * decisions on objects whose access parents lead back to them.
 *
 * <p>
 * A decision is computed once by a {@link Predicate} given to
 * {@link #decide(Object, Predicate, Runnable)}, which may ask for further decisions through this
 * memo. The decisions form a system of equations whose wanted solution is the least one: a
 * decision is <code>true</code> only when its computation finds a reason for it that does not rely
 * on the decision itself. The computations must be monotonic: a computation that receives
 * <code>true</code> for more of the decisions it asks for never answers <code>false</code> where it
 * answered <code>true</code> before (see the contract of
 * {@link AccessParentFunction#allows(TLObject, BoundCommandGroup, ParentCheck)}).
 * </p>
 *
 * <h2>Algorithm</h2>
 *
 * <p>
 * The decisions are computed depth first. The decisions under way form a stack, each one marked
 * with its depth on the stack. A decision asked for while it is under way closes a cycle: it is
 * answered <code>false</code> as an assumption, and the decision asking records that it depends on
 * the stack depth of the decision assumed (the lowest such depth is kept, as the "low link" in
 * Tarjan's algorithm for strongly connected components). The dependency is passed on to the
 * decision below on the stack when a decision completes.
 * </p>
 *
 * <p>
 * When a decision completes,
 * </p>
 * <ul>
 * <li>with <code>true</code>, it is final: it was computed assuming <code>false</code> for the
 * decisions under way, and by monotonicity it stays <code>true</code> whatever they turn out to be.
 * A <code>true</code> is never derived from an assumption of <code>true</code>. The provisional
 * denials computed during its computation are discarded, since they may rely on the assumption
 * that this decision is <code>false</code>.</li>
 * <li>with <code>false</code> and a dependency on a decision still under way below it, it is
 * provisional. It is stored as such together with that dependency, and asking for it again
 * answers <code>false</code> and records the same dependency, as if the cycle had been closed
 * again. This keeps the work polynomial: in a dense graph of decisions, a denial is not computed
 * again on every path reaching it.</li>
 * <li>with <code>false</code> and no dependency below it, it is final, and so are the provisional
 * denials computed during its computation: all assumptions they rely on have turned out
 * <code>false</code>.</li>
 * </ul>
 *
 * <p>
 * Each decision is thus computed at most once per completion of a decision it depends on, instead
 * of once per path through the graph of decisions.
 * </p>
 *
 * @param <K>
 *        The key identifying a decision.
 */
final class CyclicDecisions<K> {

	/**
	 * A decision under way.
	 */
	private static final class Frame {

		final int _depth;

		/**
		 * The number of {@link CyclicDecisions#_provisional provisional denials} when the
		 * computation started: those added later were computed during it.
		 */
		final int _provisionalStart;

		/**
		 * The lowest stack depth of a decision under way that the computation assumed
		 * <code>false</code>, {@link Integer#MAX_VALUE} for none.
		 */
		int _low = Integer.MAX_VALUE;

		Frame(int depth, int provisionalStart) {
			_depth = depth;
			_provisionalStart = provisionalStart;
		}

	}

	/**
	 * A denial relying on the assumption that a decision still under way is <code>false</code>.
	 */
	private static final class Provisional<K> {

		final K _key;

		/**
		 * The stack depth of the decision under way it relies on.
		 */
		int _low;

		Provisional(K key, int low) {
			_key = key;
			_low = low;
		}

	}

	/**
	 * For each key, a final decision as {@link Boolean}, a {@link Frame} while it is computed, or
	 * a {@link Provisional} denial.
	 */
	private final Map<K, Object> _states = new HashMap<>();

	private final List<Frame> _stack = new ArrayList<>();

	/**
	 * The provisional denials, in the order of their computation.
	 */
	private final List<Provisional<K>> _provisional = new ArrayList<>();

	/**
	 * The subjects of the cycles reported during the current top-level decision, see
	 * {@link #reportOnce(Object)}.
	 */
	private final Set<Object> _reported = new HashSet<>();

	/**
	 * Decides for the given key, answering from the memo where possible.
	 *
	 * @param key
	 *        The decision asked for.
	 * @param computation
	 *        Computes the decision for the key, possibly asking for further decisions through this
	 *        memo.
	 * @param onCycle
	 *        Called when the decision is asked for while it is under way. In a dense graph of
	 *        decisions, this happens once per path closing a cycle; a report should be limited
	 *        with {@link #reportOnce(Object)}.
	 * @return The decision, <code>false</code> for a decision under way, see the
	 *         {@link CyclicDecisions algorithm}.
	 */
	boolean decide(K key, Predicate<? super K> computation, Runnable onCycle) {
		Object state = _states.get(key);
		if (state instanceof Boolean decision) {
			return decision.booleanValue();
		}
		if (state instanceof Frame underWay) {
			onCycle.run();
			dependOn(underWay._depth);
			return false;
		}
		if (state instanceof Provisional<?> provisional) {
			dependOn(provisional._low);
			return false;
		}

		Frame frame = new Frame(_stack.size(), _provisional.size());
		_stack.add(frame);
		_states.put(key, frame);
		boolean completed = false;
		try {
			boolean result = computation.test(key);
			complete(key, frame, result);
			completed = true;
			return result;
		} finally {
			if (!completed) {
				// The computation failed: nothing computed meanwhile is stored.
				_stack.remove(_stack.size() - 1);
				discardProvisional(frame._provisionalStart);
				_states.remove(key);
			}
			if (_stack.isEmpty()) {
				_reported.clear();
			}
		}
	}

	/**
	 * Whether a cycle concerning the given subject is to be reported: the first time during the
	 * current top-level decision.
	 *
	 * <p>
	 * A {@link #decide(Object, Predicate, Runnable) cycle callback} reports through this method to
	 * report a subject at most once per top-level decision, instead of once per path closing a
	 * cycle through it.
	 * </p>
	 *
	 * @param subject
	 *        What the cycle is reported for, e.g. the object whose decision is under way.
	 * @return Whether the subject was not reported yet during the current top-level decision.
	 */
	boolean reportOnce(Object subject) {
		return _reported.add(subject);
	}

	private void complete(K key, Frame frame, boolean result) {
		_stack.remove(_stack.size() - 1);
		if (result) {
			discardProvisional(frame._provisionalStart);
			_states.put(key, Boolean.TRUE);
		} else if (frame._low < frame._depth) {
			// The denials computed meanwhile rely on this one, which relies on a decision below.
			for (int n = frame._provisionalStart, cnt = _provisional.size(); n < cnt; n++) {
				_provisional.get(n)._low = frame._low;
			}
			Provisional<K> provisional = new Provisional<>(key, frame._low);
			_provisional.add(provisional);
			_states.put(key, provisional);
			dependOn(frame._low);
		} else {
			for (int n = frame._provisionalStart, cnt = _provisional.size(); n < cnt; n++) {
				_states.put(_provisional.get(n)._key, Boolean.FALSE);
			}
			truncateProvisional(frame._provisionalStart);
			_states.put(key, Boolean.FALSE);
		}
	}

	/**
	 * Records that the decision on top of the stack relies on the decision under way at the given
	 * depth.
	 */
	private void dependOn(int depth) {
		if (_stack.isEmpty()) {
			return;
		}
		Frame top = _stack.get(_stack.size() - 1);
		top._low = Math.min(top._low, depth);
	}

	private void discardProvisional(int start) {
		for (int n = start, cnt = _provisional.size(); n < cnt; n++) {
			_states.remove(_provisional.get(n)._key);
		}
		truncateProvisional(start);
	}

	private void truncateProvisional(int start) {
		_provisional.subList(start, _provisional.size()).clear();
	}

}
