/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import junit.framework.TestCase;

import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;

/**
 * Regression test for the "second run's askUser hangs" bug: a completed run's dead processing
 * state (with a {@code null} {@link UserInteractionStore}) must not shadow the live run of a
 * follow-up message in {@link TaskProgressTracker#getProcessingStateForSession(String)}.
 *
 * <p>
 * When run 1 finishes, {@code ChatProcessor} nulls its interaction store but the completed state
 * is not evicted until the next {@code GET /chat/updates} poll — which the client stops after
 * {@code run_completed}. During run 2 the tracker therefore holds two states for the same session.
 * Before the fix, the lookup returned the first match in map iteration order, which could be the
 * dead state → its {@code null} store made the SSE poll return early → run 2's question was never
 * emitted, so the agent parked until the interaction timeout.
 * </p>
 */
public class TestProcessingStateLookup extends TestCase {

	private static final String SESSION_ID = "session-A";

	/**
	 * The task ids are chosen so that, in the tracker's {@code ConcurrentHashMap}, the completed
	 * state is iterated <em>before</em> the processing state (buckets 3 vs. 8 in the default
	 * 16-bucket table). This makes the pre-fix "return first match" behavior deterministically
	 * pick the dead state, so this test genuinely fails before the fix and passes after.
	 */
	private static final String COMPLETED_TASK_ID = "completed-task";

	private static final String PROCESSING_TASK_ID = "processing-task";

	/**
	 * The live/processing state must be preferred over a same-session completed state, and it must
	 * carry the non-null interaction store the SSE poll needs.
	 */
	public void testProcessingStatePreferredOverCompleted() {
		TaskProgressTracker tracker = new TaskProgressTracker();

		AIProcessingState completed = new AIProcessingState();
		completed.setSessionId(SESSION_ID);
		completed.startProcessing();
		completed.completeSuccessfully();
		completed.setUserInteractionStore(null);
		tracker.addProcessingState(COMPLETED_TASK_ID, completed);

		AIProcessingState processing = new AIProcessingState();
		processing.setSessionId(SESSION_ID);
		processing.startProcessing();
		UserInteractionStore store = new UserInteractionStore();
		store.createRequest("Which option?", "Choose", "text", null, false);
		processing.setUserInteractionStore(store);
		tracker.addProcessingState(PROCESSING_TASK_ID, processing);

		AIProcessingState found = tracker.getProcessingStateForSession(SESSION_ID);

		assertSame("Lookup must return the live (processing) state, not the dead one.",
			processing, found);
		assertNotNull("The returned state must carry the interaction store with the pending question.",
			found.getUserInteractionStore());
		assertTrue("The pending ask-user question must be visible to the SSE poll.",
			found.getUserInteractionStore().hasPending());
	}

	/**
	 * When only a completed state exists, it is still returned - the completion-delivery path (a
	 * late {@code GET /chat/updates} that must still find the finished run) must not regress.
	 */
	public void testCompletedStateReturnedWhenNoProcessing() {
		TaskProgressTracker tracker = new TaskProgressTracker();

		AIProcessingState completed = new AIProcessingState();
		completed.setSessionId(SESSION_ID);
		completed.startProcessing();
		completed.completeSuccessfully();
		tracker.addProcessingState(COMPLETED_TASK_ID, completed);

		assertSame("A lone completed state must still be found.",
			completed, tracker.getProcessingStateForSession(SESSION_ID));
	}

	/** Eviction removes a completed same-session state but leaves an in-flight one untouched. */
	public void testEvictCompletedKeepsProcessing() {
		TaskProgressTracker tracker = new TaskProgressTracker();

		AIProcessingState completed = new AIProcessingState();
		completed.setSessionId(SESSION_ID);
		completed.startProcessing();
		completed.completeSuccessfully();
		tracker.addProcessingState(COMPLETED_TASK_ID, completed);

		AIProcessingState processing = new AIProcessingState();
		processing.setSessionId(SESSION_ID);
		processing.startProcessing();
		tracker.addProcessingState(PROCESSING_TASK_ID, processing);

		tracker.evictCompletedStatesForSession(SESSION_ID);

		assertNull("The completed state must be evicted.",
			tracker.getProcessingState(COMPLETED_TASK_ID));
		assertSame("The in-flight state must survive eviction.",
			processing, tracker.getProcessingState(PROCESSING_TASK_ID));
	}

	/** Eviction must not touch a completed state belonging to a different session. */
	public void testEvictLeavesOtherSessionsAlone() {
		TaskProgressTracker tracker = new TaskProgressTracker();

		AIProcessingState other = new AIProcessingState();
		other.setSessionId("session-B");
		other.startProcessing();
		other.completeSuccessfully();
		tracker.addProcessingState(COMPLETED_TASK_ID, other);

		tracker.evictCompletedStatesForSession(SESSION_ID);

		assertSame("A completed state of another session must not be evicted.",
			other, tracker.getProcessingState(COMPLETED_TASK_ID));
	}
}
