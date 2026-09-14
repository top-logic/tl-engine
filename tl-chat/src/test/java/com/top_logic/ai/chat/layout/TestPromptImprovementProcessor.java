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

	/** A lead-in line, a fence around the whole answer and outer quotes are all removed. */
	public void testWrapperIsStripped() {
		assertEquals("Summarize the invoice table.",
			PromptImprovementProcessor.stripWrapper("Here is the improved prompt:\n\nSummarize the invoice table."));
		assertEquals("Summarize the invoice table.",
			PromptImprovementProcessor.stripWrapper("```\nSummarize the invoice table.\n```"));
		assertEquals("Summarize the invoice table.",
			PromptImprovementProcessor.stripWrapper("```text\nSummarize the invoice table.\n```"));
		assertEquals("Summarize the invoice table.",
			PromptImprovementProcessor.stripWrapper("\"Summarize the invoice table.\""));
		assertEquals("Summarize the invoice table.",
			PromptImprovementProcessor.stripWrapper("\u201cSummarize the invoice table.\u201d"));
		assertEquals("Fasse die Belegtabelle zusammen.",
			PromptImprovementProcessor.stripWrapper(
				"Verbesserter Prompt:\n\n```\nFasse die Belegtabelle zusammen.\n```"));
	}

	/** Fences, colons and quotes that belong to the prompt itself survive. */
	public void testPromptContentIsNotMistakenForAWrapper() {
		String withCode = "Fix this snippet:\n```java\nint x = 1;\n```\nExplain the change.";
		assertEquals(withCode, PromptImprovementProcessor.stripWrapper(withCode));

		String heading = "Context:\nThe invoice table has three columns.";
		assertEquals(heading, PromptImprovementProcessor.stripWrapper(heading));

		String quoting = "Replace \"foo\" with \"bar\" in the template.";
		assertEquals(quoting, PromptImprovementProcessor.stripWrapper(quoting));

		String fencedOnly = "```java\nint x = 1;\n```\n\n```java\nint y = 2;\n```";
		assertEquals(fencedOnly, PromptImprovementProcessor.stripWrapper(fencedOnly));
	}

	/** A wrapper around nothing is not a result. */
	public void testWrapperOnlyAnswerIsRejected() {
		try {
			PromptImprovementProcessor.improve(_tracker, "req-wrap", "do it",
				name -> AGENT.equals(name), (name, draft, token) -> "\"\"");
			fail("Expected a failure for an answer that is only a wrapper.");
		} catch (IllegalStateException expected) {
			// Expected.
		}
	}

	/** A reply that talks to the user instead of rewriting the draft is refused. */
	public void testMetaAnswerFallsBackToTheDraft() {
		assertEquals("ok do it", improved("ok do it",
			"It looks like your message is a bit brief. Could you please share the draft you want improved?"));
		assertEquals("belege auswerten", improved("belege auswerten",
			"Bitte gib an, welche Belege ausgewertet werden sollen."));
		assertEquals("fix it", improved("fix it", "Here is the improved prompt: Fix the failing build."));
	}

	/** A question the draft did not ask is a request for clarification, not a prompt. */
	public void testUnaskedQuestionFallsBackToTheDraft() {
		assertEquals("ja", improved("ja", "Was genau soll gemacht werden?"));
		// A draft that asks a question may of course be rewritten as one.
		assertEquals("Explain why this line throws.",
			improved("why does this throw?", "Explain why this line throws."));
	}

	/** Three words do not become a briefing. */
	public void testShortDraftIsNotInflated() {
		String essay = "Investigate the failing build in detail, list every failing module, "
			+ "explain the root cause and propose a fix for each of them.";
		assertEquals("fix the build", improved("fix the build", essay));
		// A proportionate rewrite of the same draft is kept.
		assertEquals("Find out why the build fails and fix it.",
			improved("fix the build", "Find out why the build fails and fix it."));
	}

	/**
	 * A one-word draft is not turned into an instruction it never gave.
	 *
	 * <p>
	 * The review's finding: {@code "Belege"} came back as {@code "Werte die vorhandenen Belege aus
	 * und fasse die Ergebnisse zusammen."} - evaluating and summarizing is not what the word says.
	 * The allowance a rewrite gets now grows with the number of words in the draft, so one word buys
	 * a quarter of the ceiling rather than all of it.
	 * </p>
	 */
	public void testOneWordDraftKeepsItsOwnMeaning() {
		assertEquals("Belege",
			improved("Belege", "Werte die vorhandenen Belege aus und fasse die Ergebnisse zusammen."));
		// A rewrite that stays as small as the word it rewrites is still allowed through.
		assertEquals("Zeige alle Belege.", improved("Belege", "Zeige alle Belege."));
	}

	/** Two words buy more room than one, and still not a briefing. */
	public void testTheAllowanceGrowsWithTheDraft() {
		String sentence = "Liste alle offenen Rechnungen mit Betrag und Datum auf.";
		assertEquals(sentence, improved("offene rechnungen", sentence));
		assertEquals("offene rechnungen", improved("offene rechnungen",
			"Liste alle offenen Rechnungen mit Betrag, Datum und Lieferant auf und markiere die, "
				+ "deren Fälligkeit überschritten ist."));
	}

	/** A genuine rewrite of a long draft passes untouched. */
	public void testLongDraftIsNotGuarded() {
		String draft = "so i was thinking that maybe we could look into the invoice thing, "
			+ "the amounts are sometimes wrong, maybe the date parsing";
		String rewrite = "Investigate why invoice amounts are sometimes wrong; date parsing is a suspect.";
		assertEquals(rewrite, improved(draft, rewrite));
	}

	private String improved(String draft, String answer) {
		return PromptImprovementProcessor
			.improve(_tracker, "req-guard", draft, name -> AGENT.equals(name), (name, text, token) -> answer)
			.improvedText();
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
