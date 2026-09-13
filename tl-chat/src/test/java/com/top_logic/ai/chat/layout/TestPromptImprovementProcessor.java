/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.top_logic.ai.chat.layout.PromptImprovementProcessor.Failure;
import com.top_logic.ai.chat.layout.PromptImprovementProcessor.PromptImprovementException;
import com.top_logic.ai.chat.layout.PromptImprovementProcessor.Result;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Isolated prompt-improvement processor tests. The agent invocation is injected so these cases
 * never start a real model run.
 */
public class TestPromptImprovementProcessor extends TestCase {

	private static final String AGENT = "TL_PromptImprover";

	private TaskProgressTracker _tracker;

	@Override
	protected void setUp() {
		_tracker = new TaskProgressTracker();
		_tracker.setPromptImproverAgentName(AGENT);
	}

	/** A configured agent plus a successful rewrite leaves the chat transcript untouched. */
	public void testCompletedDoesNotTouchChatOrProcessingState() {
		int messageCount = _tracker.getChat().getMessages().size();

		Result result = PromptImprovementProcessor.improve(_tracker, "req-1", "  make this clearer  ",
			name -> AGENT.equals(name), (name, draft, token) -> {
				assertEquals(AGENT, name);
				assertEquals("make this clearer", draft);
				return "  Make this clearer and more specific.  ";
			});

		assertEquals("req-1", result.requestId());
		assertEquals(PromptImprovementProcessor.STATUS_COMPLETED, result.status());
		assertEquals("Make this clearer and more specific.", result.improvedText());
		assertEquals(messageCount, _tracker.getChat().getMessages().size());
		assertFalse(_tracker.hasActiveProcessingState());
		assertNull(_tracker.getPromptImprovementState());
	}

	/** Missing configuration is a disabled-feature failure. */
	public void testDisabledWhenNoAgentConfigured() {
		_tracker.setPromptImproverAgentName(null);
		assertFailure(Failure.DISABLED, "req-2", "improve this");
	}

	/** The configured agent name is resolved server-side; an unknown name is rejected. */
	public void testMissingConfiguredAgent() {
		PromptImprovementException ex = assertFailure(Failure.MISSING_AGENT, "req-3", "improve this",
			name -> false, (name, draft, token) -> "unused");
		assertTrue(ex.getMessage().contains(AGENT));
	}

	public void testBlankDraftRejected() {
		assertFailure(Failure.INVALID_REQUEST, "req-4", "   ");
	}

	public void testBlankRequestIdRejected() {
		assertFailure(Failure.INVALID_REQUEST, "  ", "improve this");
	}

	public void testOversizedDraftRejected() {
		assertFailure(Failure.INVALID_REQUEST, "req-5",
			"x".repeat(PromptImprovementProcessor.MAX_DRAFT_LENGTH + 1));
	}

	/** A matching cancel token stops the current run without creating chat state. */
	public void testCancelMatchingRequest() {
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch cancelled = new CountDownLatch(1);
		AtomicReference<Result> result = new AtomicReference<>();
		AtomicReference<Throwable> error = new AtomicReference<>();

		Thread worker = new Thread(() -> {
			try {
				result.set(PromptImprovementProcessor.improve(_tracker, "req-cancel", "draft",
					name -> true, (name, draft, token) -> {
						started.countDown();
						if (!await(cancelled)) {
							throw new IllegalStateException("cancel signal timed out");
						}
						if (token.isCancelled()) {
							throw new CancellationException();
						}
						return "should not apply";
					}));
			} catch (RuntimeException ex) {
				error.set(ex);
			}
		});
		worker.start();
		assertTrue("Improve run did not start.", await(started));
		assertTrue(_tracker.cancelPromptImprovement("req-cancel"));
		cancelled.countDown();
		join(worker);

		assertNull(error.get());
		assertEquals(PromptImprovementProcessor.STATUS_CANCELLED, result.get().status());
		assertEquals("", result.get().improvedText());
		assertFalse(_tracker.hasActiveProcessingState());
		assertTrue(_tracker.getChat().getMessages().isEmpty());
	}

	/** A newer request cancels the previous one and treats the old result as stale. */
	public void testNewerRequestMakesPreviousStale() {
		CountDownLatch firstStarted = new CountDownLatch(1);
		CountDownLatch secondDone = new CountDownLatch(1);
		AtomicReference<Result> first = new AtomicReference<>();
		AtomicReference<Result> second = new AtomicReference<>();

		Thread firstWorker = new Thread(() -> {
			first.set(PromptImprovementProcessor.improve(_tracker, "req-old", "first draft",
				name -> true, (name, draft, token) -> {
					firstStarted.countDown();
					if (!await(secondDone)) {
						throw new IllegalStateException("second run timed out");
					}
					if (token.isCancelled()) {
						throw new CancellationException();
					}
					return "old result";
				}));
		});
		firstWorker.start();
		assertTrue("First improve run did not start.", await(firstStarted));

		second.set(PromptImprovementProcessor.improve(_tracker, "req-new", "second draft",
			name -> true, (name, draft, token) -> "new result"));
		secondDone.countDown();
		join(firstWorker);

		assertEquals(PromptImprovementProcessor.STATUS_COMPLETED, second.get().status());
		assertEquals("new result", second.get().improvedText());
		assertEquals(PromptImprovementProcessor.STATUS_STALE, first.get().status());
		assertEquals("", first.get().improvedText());
		assertNull(_tracker.getPromptImprovementState());
	}

	/** Cancelling a request ID that is not current is a no-op. */
	public void testCancelUnknownRequestIsNoop() {
		CancellationToken token = new CancellationToken();
		_tracker.replacePromptImprovementState(
			new TaskProgressTracker.PromptImprovementState("other", token));
		assertFalse(_tracker.cancelPromptImprovement("missing"));
		assertFalse(token.isCancelled());
	}

	private PromptImprovementException assertFailure(Failure expected, String requestId, String draft) {
		return assertFailure(expected, requestId, draft, name -> true,
			(name, text, token) -> "unused");
	}

	private PromptImprovementException assertFailure(Failure expected, String requestId, String draft,
			java.util.function.Predicate<String> agentExists,
			PromptImprovementProcessor.AgentInvocation invocation) {
		try {
			PromptImprovementProcessor.improve(_tracker, requestId, draft, agentExists, invocation);
			fail("Expected " + expected);
			return null;
		} catch (PromptImprovementException ex) {
			assertEquals(expected, ex.getFailure());
			return ex;
		}
	}

	private static boolean await(CountDownLatch latch) {
		try {
			return latch.await(2, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	private static void join(Thread worker) {
		try {
			worker.join(2000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			fail("Interrupted while waiting for improve worker.");
		}
		assertFalse("Improve worker is still running.", worker.isAlive());
	}
}
