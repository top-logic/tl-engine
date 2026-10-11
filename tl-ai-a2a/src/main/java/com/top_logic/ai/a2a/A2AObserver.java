/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a;

/**
 * Application hook: learns of the A2A exchanges of this application in both directions - what
 * remote clients ask its agents, and what its agents ask remote agents.
 *
 * <p>
 * Configured in {@code <observers>} on {@link A2AService}. Every {@link A2AExchange} carries the
 * context id and the task id, which are the same on both sides of a conversation, and the message id
 * of a message, which is the same in the sender's {@link A2AExchange.Kind#MESSAGE_SENT} and the
 * receiver's {@link A2AExchange.Kind#MESSAGE_RECEIVED}; its {@link A2AExchange#runId() run id} links
 * it to the local run a {@code RunObserver} reports.
 * </p>
 *
 * <p>
 * Called on an observer thread of its own, never on the thread of the request or the run, in the
 * order the exchanges happened. An observer that throws is logged and called again with the next
 * exchange; one that cannot keep up loses exchanges rather than slowing the conversation - see
 * {@code ObserverQueue}.
 * </p>
 *
 * <p>
 * <b>Security:</b> an observer sees the messages and artifacts of every caller's conversations and
 * runs with no rights check of its own. An observer that passes them on to anything outside the
 * application must authenticate and authorize that itself.
 * </p>
 */
@FunctionalInterface
public interface A2AObserver {

	/**
	 * One step of an A2A conversation.
	 */
	void onExchange(A2AExchange exchange);
}
