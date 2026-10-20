/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
 * That a choice question of {@code askUser} takes an answer in the user's own words, and that the
 * agent gets it marked as such (#679).
 */
public class TestAskUserOwnWords extends TestCase {

	private static final String OPTIONS = "[\"Monthly\", \"Quarterly\", \"Yearly\"]";

	private final AskUserTool _tool = new AskUserTool();

	/**
	 * The point of the ticket: options that do not fit no longer leave the user only "skip". What
	 * they wrote reaches the agent, and cannot be taken for one of the labels.
	 */
	public void testOwnWordsInsteadOfAnOption() throws Exception {
		String answer = askAndAnswer(Boolean.FALSE, "", "Every two weeks, on Fridays");

		assertEquals("The user picked none of the offered options and answered in their own words:\n"
			+ "Every two weeks, on Fridays", answer);
	}

	/** In a multi select the picked options and the own words arrive side by side. */
	public void testOwnWordsBesideThePickedOptions() throws Exception {
		String answer = askAndAnswer(Boolean.TRUE, "Monthly, Yearly", "and a summary in December");

		assertEquals("The user picked: Monthly, Yearly\n"
			+ "In addition, the user wrote in their own words:\n"
			+ "and a summary in December", answer);
	}

	/** A picked option alone is answered exactly as before. */
	public void testPickedOptionIsUnchanged() throws Exception {
		assertEquals("Quarterly", askAndAnswer(Boolean.FALSE, "Quarterly", null));
	}

	/**
	 * Asks a choice question on a thread of its own, as the agent does, and answers it from this one,
	 * as the chat servlet does.
	 */
	private String askAndAnswer(Boolean multiSelect, String picked, String ownWords) throws Exception {
		UserInteractionStore store = new UserInteractionStore();
		CompletableFuture<String> asking = new CompletableFuture<>();
		Thread agent = new Thread(() -> {
			try (UserInteractionStoreBinding.Binding binding = UserInteractionStoreBinding.bind(store)) {
				asking.complete(_tool.askUser("How often should the report run?", "Schedule", "choice",
					OPTIONS, multiSelect, ToolContext.unrestricted()));
			} catch (RuntimeException ex) {
				asking.completeExceptionally(ex);
			}
		}, "TestAskUserOwnWords-agent");
		agent.setDaemon(true);
		agent.start();
		try {
			UserInteractionRequest pending = awaitPending(store);
			store.resolve(pending.getId(), picked, ownWords);
			return asking.get(10, TimeUnit.SECONDS);
		} finally {
			agent.interrupt();
		}
	}

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
