/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;

/**
 * That {@code askUser} fails at once in a run that has nobody to ask (issue #384).
 *
 * <p>
 * A scheduled run has no browser window. Before this, {@code askUser} in such a run created a
 * request nobody would ever see and parked the agent thread on it for the full interaction timeout -
 * 30 minutes by default - after which the model was told its question had "timed out", which is a
 * different thing from "there is nobody here" and invites it to ask again.
 * </p>
 *
 * <p>
 * The refusal is keyed on {@link ToolContext#attended()}, which the entry point sets, never on what
 * the prompt says: an application that has to write "do not call askUser" into every scheduled
 * prompt is relying on the model to enforce its own limits.
 * </p>
 */
public class TestAskUserUnattended extends TestCase {

	private final AskUserTool _tool = new AskUserTool();

	/** An unattended run is refused, and told what to do instead. */
	public void testUnattendedRunIsRefused() {
		String answer = ask(ToolContext.unrestricted().unattended());

		assertEquals(AskUserTool.NOBODY_TO_ASK, answer);
		assertTrue("The model has to be able to tell this from a rejection or a timeout.",
			answer.startsWith("error:"));
		assertTrue("Retrying must be named as pointless, or the run spends its turns on it.",
			answer.contains("Do not call askUser again"));
	}

	/**
	 * Nothing is registered for an answer that cannot come.
	 *
	 * <p>
	 * The refusal is the first statement of the tool, before the request is created and before it
	 * is recorded, so an unattended run leaves no pending question behind - neither in the store
	 * nor in {@code InteractionRequest} rows an operator would have to look at.
	 * </p>
	 */
	public void testUnattendedRunRegistersNoQuestion() {
		UserInteractionStore store = new UserInteractionStore();
		try (UserInteractionStoreBinding.Binding binding = UserInteractionStoreBinding.bind(store)) {
			ask(ToolContext.unrestricted().unattended());

			assertTrue("A question nobody can answer must not be left pending.",
				store.getPendingRequests().isEmpty());
		}
	}

	/**
	 * An attended run still parks on its question.
	 *
	 * <p>
	 * The other half of the claim: this changes nothing for a chat run, whose question is registered
	 * and waited for exactly as before.
	 * </p>
	 */
	public void testAttendedRunStillAsks() throws Exception {
		UserInteractionStore store = new UserInteractionStore();
		CompletableFuture<String> asking = new CompletableFuture<>();
		// The store is bound to the asking thread, as AgentEnvironment binds it around a run, so the
		// question has to be asked on a thread of its own - this one answers it.
		Thread agent = new Thread(() -> {
			try (UserInteractionStoreBinding.Binding binding = UserInteractionStoreBinding.bind(store)) {
				asking.complete(ask(ToolContext.unrestricted()));
			} catch (RuntimeException ex) {
				asking.completeExceptionally(ex);
			}
		}, "TestAskUserUnattended-agent");
		agent.setDaemon(true);
		agent.start();
		try {
			UserInteractionRequest pending = awaitPending(store);
			assertFalse("The tool must still be parked on the answer.", asking.isDone());

			store.resolve(pending.getId(), "the answer");
			assertEquals("the answer", asking.get(10, TimeUnit.SECONDS));
		} finally {
			agent.interrupt();
		}
	}

	/**
	 * A question nobody answers in time ends the wait, and the model is told to go on with an
	 * assumption it names, not to stop or undo (Openium/Pietro#744: a chat turn waited 30 minutes,
	 * then the agent deleted the document it had made).
	 */
	public void testAQuestionNobodyAnswersLetsTheRunGoOn() throws Exception {
		UserInteractionStore store = new UserInteractionStore();
		AskUserTool tool = new AskUserTool(() -> Duration.ofMillis(200));
		String answer;
		try (UserInteractionStoreBinding.Binding binding = UserInteractionStoreBinding.bind(store)) {
			answer = tool.askUser("Which layout do you want?", "Choose", "text", null, Boolean.FALSE,
				ToolContext.unrestricted());
		}

		assertTrue(answer, answer.startsWith("Nobody answered within 1 minute"));
		assertTrue(answer, answer.contains("Go on with the task"));
		assertTrue(answer, answer.contains("say which assumption you took"));
		assertTrue(answer, answer.contains("Never delete or replace work"));
		assertTrue("The question must not stay pending.", store.getPendingRequests().isEmpty());
	}

	private String ask(ToolContext context) {
		return _tool.askUser("Which invoice did you mean?", "Clarify", "text", null, Boolean.FALSE,
			context);
	}

	/** The question the other thread registered, or <code>null</code> if it never did. */
	private static UserInteractionRequest awaitPending(UserInteractionStore store)
			throws InterruptedException, TimeoutException {
		long deadline = System.currentTimeMillis() + Duration.ofSeconds(10).toMillis();
		while (System.currentTimeMillis() < deadline) {
			for (UserInteractionRequest pending : store.getPendingRequests()) {
				return pending;
			}
			Thread.sleep(20);
		}
		throw new TimeoutException("No question was registered within 10 s.");
	}
}
