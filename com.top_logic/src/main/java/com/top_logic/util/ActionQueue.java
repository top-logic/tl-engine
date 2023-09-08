/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.mig.html.layout.Action;

/**
 * The class {@link ActionQueue} enqueues {@link Action Actions} of a certain type and dispatches
 * the {@link Action} on demand
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ActionQueue<E> {

	/**
	 * Sends or enqueues the given {@link Action}: If currently no {@link Action} is processed, the
	 * given {@link Action} is processed directly. If currently an {@link Action} is processed, the
	 * {@link Action} is enqueued and processed if all previous {@link Action Actions} have been
	 * processed.
	 * 
	 * @param action
	 *        The {@link Action} to send. Must not be <code>null</code>.
	 */
	void enqueueAction(E action);

	/**
	 * Forces the queue to enqueue all {@link #enqueueAction(Object) enqueued} {@link Action
	 * Actions} until {@link #processActions()} is called, i.e. no {@link Action} is processed until
	 * {@link #processActions()} is called.
	 * 
	 * @see #processActions()
	 */
	void forceQueueing();

	/**
	 * Starts the processing of the enqueued {@link Action Actions} after {@link #forceQueueing()}.
	 * If {@link #forceQueueing()} is not called before the call to this method has no effect.
	 * 
	 * @see #forceQueueing()
	 */
	void processActions();

}
