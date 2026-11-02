/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.servlet;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
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
 * Events enqueued outside a scope are written through immediately: an SSE (re)connection and
 * background activity deliver state that no interaction is about to revise.
 * </p>
 *
 * <p>
 * The end of the outermost interaction is also the point at which the display has settled, so a
 * check that only holds for the completed display - whether every content found the place it is
 * shown at, say - is registered with {@link #afterInteraction(Runnable)} instead of being made
 * while the trees are still changing. Such work runs before the queues are settled, so that what it
 * changes is delivered together with the interaction.
 * </p>
 *
 * @see com.top_logic.layout.react.window.Interaction
 */
public final class DeliveryScope {

	private static final ThreadLocal<DeliveryScope> SCOPE = new ThreadLocal<>();

	private int _depth;

	private final Set<SSEUpdateQueue> _touchedQueues =
		Collections.newSetFromMap(new IdentityHashMap<SSEUpdateQueue, Boolean>());

	/**
	 * The work {@link #afterInteraction(Runnable) registered} to run when the outermost level is
	 * left, in registration order.
	 */
	private final Deque<Runnable> _afterInteraction = new ArrayDeque<>();

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
	 * Whether an interaction is open on the current thread.
	 *
	 * <p>
	 * Outside an interaction, the control trees have no point at which a change to them is complete:
	 * a session that ends, for instance, disposes them one control after another.
	 * </p>
	 */
	public static boolean isOpen() {
		return SCOPE.get() != null;
	}

	/**
	 * Runs the given work when the interaction of the current thread has completed.
	 *
	 * <p>
	 * Within an open scope, the work is queued and runs once, when the outermost level is
	 * {@link #exit() left} - after everything the interaction does to the control trees and before
	 * the queues are settled, so that the state changes the work makes reach the client together
	 * with the interaction. The work runs while the scope is still open: an interaction it enters
	 * itself nests into the completing one, and work it registers in turn runs as well, after the
	 * work registered before it. A failing work item is logged and keeps neither the remaining work
	 * nor the delivery from happening.
	 * </p>
	 *
	 * <p>
	 * Outside a scope there is no interaction to wait for, and the work runs at once.
	 * </p>
	 *
	 * @param work
	 *        The work to run when the interaction has completed.
	 */
	public static void afterInteraction(Runnable work) {
		DeliveryScope scope = SCOPE.get();
		if (scope == null) {
			work.run();
			return;
		}
		scope._afterInteraction.add(work);
	}

	/**
	 * Leaves one level of the current thread's delivery scope.
	 *
	 * <p>
	 * When the outermost level is left, the work registered with {@link #afterInteraction(Runnable)}
	 * runs, and then every queue the interaction touched is settled.
	 * </p>
	 *
	 * @see #enter()
	 */
	public static void exit() {
		DeliveryScope scope = SCOPE.get();
		if (scope == null) {
			return;
		}
		if (scope._depth > 1) {
			scope._depth--;
			return;
		}
		try {
			// The outermost level stays entered while the work runs, so that the events it produces
			// are held back for the settling below and an interaction it enters nests.
			scope.runAfterInteraction();
		} finally {
			scope._depth = 0;
			// Drop the thread-local entry before settling, so that pooled threads retain no scope and
			// events a settling queue produces are written through instead of being held back
			// forever.
			SCOPE.remove();
			scope.settleAll();
		}
	}

	/**
	 * Notes that the given queue received an event within this scope, so that it is settled when the
	 * interaction completes.
	 */
	void collect(SSEUpdateQueue queue) {
		_touchedQueues.add(queue);
	}

	private void runAfterInteraction() {
		Runnable work;
		while ((work = _afterInteraction.poll()) != null) {
			try {
				work.run();
			} catch (RuntimeException ex) {
				// One failing check must not keep the remaining ones or the delivery of the
				// interaction from happening.
				Logger.error("Failed to complete an interaction.", ex, DeliveryScope.class);
			}
		}
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
