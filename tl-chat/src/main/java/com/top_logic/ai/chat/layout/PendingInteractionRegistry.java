/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;

/**
 * The runs currently parked on a human, so that any window of the responder's account can answer
 * them - not only the one that started the run.
 *
 * <p>
 * A run that asks for an approval, or asks the user a question, parks its thread on a future in the
 * {@link UserInteractionStore} of its own {@link ChatProcessor.AIProcessingState}, and that state
 * hangs on the {@link TaskProgressTracker} of one browser window. Without this register the future
 * is reachable only through that window: a second window, a reloaded page whose window is a new one,
 * or the same account signing in again could see the pending request - {@code GET /chat/interactions}
 * reads it from the persisted {@code InteractionRequest} rows and is account-scoped - but had no way
 * to answer it, and the run stayed parked until its timeout (issue #393).
 * </p>
 *
 * <p>
 * The register is process-wide and keyed by the run's task id, and every entry names the
 * <em>account</em> the run is waiting on. That account is the whole access rule:
 * {@link #find(Person, String)} answers only for the responder, so another account asking the same
 * request id is told exactly what it is told about a request that does not exist - nothing.
 * </p>
 *
 * <p>
 * Entries live for the duration of one run: {@link ChatProcessor} registers a run when it creates
 * its interaction store and drops it in the same {@code finally} that unbinds the store, so a
 * finished, failed or cancelled run leaves nothing behind. Nothing here keeps a window alive - the
 * run's own background task already holds its tracker while it is working.
 * </p>
 */
public final class PendingInteractionRegistry {

	/**
	 * One run that may be waiting for a human.
	 *
	 * @param taskId
	 *        The run's task id; the key this entry is registered under.
	 * @param responder
	 *        The account whose windows may answer this run's requests. Never <code>null</code>.
	 * @param tracker
	 *        The tracker of the window the run was started in. The run's session-approved tools
	 *        live here, so "approve for this session" has to be recorded against it - not against
	 *        the tracker of whichever window happens to answer.
	 * @param state
	 *        The run's processing state, holding the {@link UserInteractionStore} the answer
	 *        completes and the conversation log the decision is recorded in.
	 */
	public record PendingRun(String taskId, Person responder, TaskProgressTracker tracker,
			AIProcessingState state) {
		// Plain data.
	}

	private static final Map<String, PendingRun> RUNS = new ConcurrentHashMap<>();

	private PendingInteractionRegistry() {
		// Static registry.
	}

	/**
	 * Announces a run whose human-in-the-loop requests the given account may answer from any of its
	 * windows.
	 *
	 * <p>
	 * A no-op when the responder is unknown: a run nobody is named as the responder of must not
	 * become answerable by everybody. Such a run keeps the behaviour it had before this register
	 * existed - only the window it runs in can answer it.
	 * </p>
	 *
	 * @param taskId
	 *        The run's task id; must not be <code>null</code>.
	 * @param responder
	 *        The account the run is waiting on; may be <code>null</code>.
	 * @param tracker
	 *        The tracker of the window the run was started in.
	 * @param state
	 *        The run's processing state.
	 */
	public static void register(String taskId, Person responder, TaskProgressTracker tracker,
			AIProcessingState state) {
		if (taskId == null || responder == null || tracker == null || state == null) {
			if (taskId != null && responder == null) {
				Logger.info("Run " + taskId + " has no responder; its approvals and questions can be "
					+ "answered only in the window that started it.", PendingInteractionRegistry.class);
			}
			return;
		}
		RUNS.put(taskId, new PendingRun(taskId, responder, tracker, state));
	}

	/**
	 * Drops the run registered under the given task id; a no-op if there is none.
	 *
	 * @param taskId
	 *        The task id passed to {@link #register}; may be <code>null</code>.
	 */
	public static void unregister(String taskId) {
		if (taskId != null) {
			RUNS.remove(taskId);
		}
	}

	/**
	 * The run waiting for the given request, if it is waiting for this account.
	 *
	 * <p>
	 * Does not take the request: the caller resolves it through the returned run's
	 * {@link UserInteractionStore}, after recording what the user decided.
	 * </p>
	 *
	 * @param responder
	 *        The account that wants to answer; may be <code>null</code>, which finds nothing.
	 * @param requestId
	 *        The correlation id of the approval or question; may be <code>null</code>, which finds
	 *        nothing.
	 * @return The run parked on that request, or <code>null</code> when no run of this account is
	 *         waiting for it - including when the request belongs to somebody else.
	 */
	public static PendingRun find(Person responder, String requestId) {
		if (responder == null || requestId == null) {
			return null;
		}
		for (PendingRun run : RUNS.values()) {
			if (!responder.equals(run.responder())) {
				continue;
			}
			UserInteractionStore store = run.state().getUserInteractionStore();
			if (store != null && store.isPending(requestId)) {
				return run;
			}
		}
		return null;
	}

	/** How many runs are currently registered. For tests and diagnostics. */
	public static int size() {
		return RUNS.size();
	}
}
