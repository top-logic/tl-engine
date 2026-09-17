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
 */
final class VetoCollector {

	private final List<StateHandler> _handlers = new ArrayList<>();

	private final Set<StateHandler> _seen = Collections.newSetFromMap(new IdentityHashMap<>());

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
