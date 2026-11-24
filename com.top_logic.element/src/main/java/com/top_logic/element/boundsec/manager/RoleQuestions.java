/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.service.InteractionRevisionMemo;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Evaluation of the questions the computation of roles asks, which may depend on each other in
 * cycles.
 *
 * <p>
 * A question is identified by a key and answered by a computation that may ask further questions.
 * All computations only add to their result when the answers they use grow. The answer to a
 * question is the least fixed point: nothing is part of an answer that cannot be derived without
 * relying on itself.
 * </p>
 *
 * <p>
 * A question that is asked again while it is being answered is answered with the result assumed
 * for it so far, which is empty at first. When its computation is finished and the result differs
 * from the assumption, the computation is repeated with the result as new assumption, until both
 * agree. An answer that relies on an assumption about a question still being answered is
 * provisional: it is neither kept nor reused.
 * </p>
 *
 * <p>
 * Final answers are kept in a memo for the current interaction and the current revision of the
 * {@link KnowledgeBase}. Without such a memo, they are kept until the outermost question is
 * answered.
 * </p>
 */
public final class RoleQuestions {

	private static final int MAX_ITERATIONS = 1000;

	private static final InteractionRevisionMemo<Map<Object, Object>> MEMO =
		new InteractionRevisionMemo<>("roleQuestions", kb -> new HashMap<>());

	private static final ThreadLocal<RoleQuestions> ACTIVE = new ThreadLocal<>();

	private final Map<Object, Object> _answers;

	private final Map<Object, Frame> _open = new HashMap<>();

	private final ArrayDeque<Frame> _stack = new ArrayDeque<>();

	private RoleQuestions(Map<Object, Object> answers) {
		_answers = answers;
	}

	/**
	 * Answers the question with the given key.
	 *
	 * @param kb
	 *        The {@link KnowledgeBase} the question is asked about.
	 * @param useMemo
	 *        Whether the answer may be kept for the current interaction.
	 * @param key
	 *        Identifies the question. Must implement {@link Object#equals(Object)}.
	 * @param nothing
	 *        The empty answer, which is assumed for a question asked again while being answered.
	 * @param computation
	 *        Computes the answer. It may ask further questions. The result must not be modified
	 *        afterwards.
	 * @return The answer to the question.
	 */
	public static <T> T ask(KnowledgeBase kb, boolean useMemo, Object key, T nothing, Supplier<T> computation) {
		RoleQuestions active = ACTIVE.get();
		if (active != null) {
			return active.answer(key, nothing, computation);
		}
		Map<Object, Object> memo = useMemo ? MEMO.current(kb) : null;
		RoleQuestions questions = new RoleQuestions(memo == null ? new HashMap<>() : memo);
		ACTIVE.set(questions);
		try {
			return questions.answer(key, nothing, computation);
		} finally {
			ACTIVE.remove();
		}
	}

	private <T> T answer(Object key, T nothing, Supplier<T> computation) {
		@SuppressWarnings("unchecked")
		T known = (T) _answers.get(key);
		if (known != null) {
			return known;
		}
		Frame open = _open.get(key);
		if (open != null) {
			// The question depends on itself: Answer with the assumption, and record that the
			// questions being answered rely on it.
			open._used = true;
			for (Frame frame : _stack) {
				if (frame == open) {
					break;
				}
				frame._assumptions.add(key);
			}
			@SuppressWarnings("unchecked")
			T assumed = (T) open._assumed;
			return assumed;
		}

		Frame frame = new Frame(key, nothing);
		_open.put(key, frame);
		_stack.push(frame);
		T result;
		try {
			int iterations = 0;
			while (true) {
				frame._used = false;
				frame._assumptions.clear();
				result = computation.get();
				if (!frame._used || result.equals(frame._assumed)) {
					break;
				}
				if (++iterations > MAX_ITERATIONS) {
					Logger.error("Roles depending on each other could not be resolved: " + key, RoleQuestions.class);
					break;
				}
				frame._assumed = result;
			}
		} finally {
			_stack.pop();
			_open.remove(key);
		}
		if (frame._assumptions.isEmpty()) {
			_answers.put(key, result);
		}
		return result;
	}

	/**
	 * A question being answered.
	 */
	private static final class Frame {

		final Object _key;

		/** The answer assumed for the question, when it is asked while being answered. */
		Object _assumed;

		/** Whether {@link #_assumed} has been used in the current computation. */
		boolean _used;

		/**
		 * The other questions being answered, on whose assumptions the current computation relies.
		 */
		final Set<Object> _assumptions = new HashSet<>();

		Frame(Object key, Object assumed) {
			_key = key;
			_assumed = assumed;
		}

		@Override
		public String toString() {
			return String.valueOf(_key);
		}
	}

}
