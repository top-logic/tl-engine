/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.servlet;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import com.top_logic.basic.Logger;

/**
 * Per-thread scope that holds back SSE delivery until the interaction that produces the events has
 * completed.
 *
 * <p>
 * An interaction both updates controls and decides which controls stay displayed, and it does so in
 * that order: a control is patched by the listener chain of the channel a command wrote, while the
 * container that stops displaying it is notified later in the same chain. An update addressed to a
 * control the client is about to unmount sends the browser looking for data the server no longer
 * serves. Within an open scope an {@link SSEUpdateQueue#enqueue(com.top_logic.layout.react.protocol.SSEEvent)
 * enqueued} event therefore only queues, and the queues that received events are
 * {@link SSEUpdateQueue#settle() settled} once the outermost interaction closes - at the point where
 * it is known which controls are still displayed.
 * </p>
 *
 * <p>
 * The scope collects queues rather than serving a single one: an interaction in one window reaches
 * the queue of another - closing a window patches the state of its opener - so every queue an
 * interaction touched is settled.
 * </p>
 *
 * <p>
 * Events enqueued outside a scope are written through immediately: an SSE (re)connection, the
 * synthesis of model events on the heartbeat and background activity all deliver state that no
 * interaction is about to revise.
 * </p>
 *
 * @see com.top_logic.layout.react.window.Interaction
 */
public final class DeliveryScope {

	private static final ThreadLocal<DeliveryScope> SCOPE = new ThreadLocal<>();

	private int _depth;

	private final Set<SSEUpdateQueue> _touchedQueues =
		Collections.newSetFromMap(new IdentityHashMap<SSEUpdateQueue, Boolean>());

	private DeliveryScope() {
		// Only created through enter().
	}

	/**
	 * The scope of the current thread, or {@code null} if no interaction is open.
	 */
	static DeliveryScope current() {
		return SCOPE.get();
	}

	/**
	 * Opens the delivery scope of the current thread, or enters the open one another level deep.
	 *
	 * <p>
	 * Interactions nest - a replayed script step acts through the same seam the request handler
	 * already entered - and delivery settles when the outermost one {@link #exit() exits}.
	 * </p>
	 *
	 * @see com.top_logic.layout.react.window.Interaction
	 */
	public static void enter() {
		DeliveryScope scope = SCOPE.get();
		if (scope == null) {
			scope = new DeliveryScope();
			SCOPE.set(scope);
		}
		scope._depth++;
	}

	/**
	 * Leaves one level of the current thread's delivery scope, settling every queue the interaction
	 * touched when the outermost level is left.
	 *
	 * @see #enter()
	 */
	public static void exit() {
		DeliveryScope scope = SCOPE.get();
		if (scope == null) {
			return;
		}
		scope._depth--;
		if (scope._depth > 0) {
			return;
		}
		// Drop the thread-local entry before settling, so that pooled threads retain no scope and
		// events a settling queue produces are written through instead of being held back forever.
		SCOPE.remove();
		scope.settleAll();
	}

	/**
	 * Notes that the given queue received an event within this scope, so that it is settled when the
	 * interaction completes.
	 */
	void collect(SSEUpdateQueue queue) {
		_touchedQueues.add(queue);
	}

	private void settleAll() {
		for (SSEUpdateQueue queue : _touchedQueues) {
			try {
				queue.settle();
			} catch (RuntimeException ex) {
				// A window that cannot be served must not keep the remaining ones from receiving
				// what the interaction produced for them.
				Logger.error("Failed to deliver the events of an interaction.", ex, DeliveryScope.class);
			}
		}
		_touchedQueues.clear();
	}

}
