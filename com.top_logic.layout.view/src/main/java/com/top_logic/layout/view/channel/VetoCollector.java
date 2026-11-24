/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.layout.view.channel.ViewChannel.VetoListener;
import com.top_logic.layout.view.form.StateHandler;

/**
 * Collects the answers of the {@link ViewChannel.VetoListener}s of a channel.
 *
 * <p>
 * Since the question is asked transitively over the channels written in reaction to a change, the
 * same {@link StateHandler} is reachable over several paths. It is reported once, at the position
 * where it is seen first, so that the user is asked about each form holding unsaved changes exactly
 * once.
 * </p>
 *
 * <p>
 * The channels asked form a graph that may contain cycles: a channel derived from a selection
 * channel is the input of the element writing that selection, so each forwards the question to the
 * other. A question started at a channel therefore asks every channel at most once:
 * {@link #ask(ViewChannel, Collection, Function)} records the channels asked so far, and a channel
 * reached again answers with nothing - its handlers are collected where it was asked first.
 * </p>
 */
final class VetoCollector {

	/**
	 * The channels asked so far in the question the current thread is answering, <code>null</code>
	 * outside of a question.
	 */
	private static final ThreadLocal<Set<ViewChannel>> ASKED = new ThreadLocal<>();

	private final List<StateHandler> _handlers = new ArrayList<>();

	private final Set<StateHandler> _seen = Collections.newSetFromMap(new IdentityHashMap<>());

	/**
	 * Asks the given veto listeners of a channel, as part of the question the current thread is
	 * answering, or as a new question.
	 *
	 * @param channel
	 *        The channel whose listeners are asked.
	 * @param listeners
	 *        The {@link VetoListener}s of the channel.
	 * @param question
	 *        What each listener is asked.
	 * @return The collected handlers, in the order of their first appearance; empty if the channel
	 *         was asked before in the same question.
	 */
	static List<StateHandler> ask(ViewChannel channel, Collection<VetoListener> listeners,
			Function<VetoListener, List<StateHandler>> question) {
		if (listeners.isEmpty()) {
			return List.of();
		}
		Set<ViewChannel> asked = ASKED.get();
		boolean outermost = asked == null;
		if (outermost) {
			asked = Collections.newSetFromMap(new IdentityHashMap<>());
			ASKED.set(asked);
		}
		try {
			if (!asked.add(channel)) {
				// Reached over a cycle: the handlers are collected where the channel was asked first.
				return List.of();
			}
			VetoCollector result = new VetoCollector();
			for (VetoListener listener : listeners) {
				result.addAll(question.apply(listener));
			}
			return result.toList();
		} finally {
			if (outermost) {
				ASKED.remove();
			}
		}
	}

	/**
	 * Adds the answer of a single {@link ViewChannel.VetoListener}, dropping handlers already
	 * collected.
	 *
	 * @param handlers
	 *        The handlers the listener objects with, may be empty.
	 */
	void addAll(Collection<? extends StateHandler> handlers) {
		for (StateHandler handler : handlers) {
			if (_seen.add(handler)) {
				_handlers.add(handler);
			}
		}
	}

	/**
	 * Whether no handler objects, so that the change can happen without asking.
	 */
	boolean isEmpty() {
		return _handlers.isEmpty();
	}

	/**
	 * The collected handlers, in the order of their first appearance.
	 */
	List<StateHandler> toList() {
		return _handlers;
	}

}
