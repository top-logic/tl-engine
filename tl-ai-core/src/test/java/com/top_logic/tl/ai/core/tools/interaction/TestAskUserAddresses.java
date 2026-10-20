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
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;

/**
 * An address the user writes in answer to {@code askUser} is an address the run was given, as one in
 * the user's message is; an option the model offered and the user picked is not, because the model
 * wrote it.
 */
public class TestAskUserAddresses extends TestCase {

	private static final String MANUAL = "https://docs.example/manual";

	private final AskUserTool _tool = new AskUserTool();

	/** The answer to a text question is the user's own words. */
	public void testAnAddressInATextAnswerIsGiven() throws Exception {
		ReadingList list = askAndAnswer("text", null, "Read " + MANUAL + " first.", null);

		assertTrue(list.seenUrls().contains(MANUAL));
	}

	/** A picked option is the model's text, given back by a click. */
	public void testAPickedOptionIsNotGiven() throws Exception {
		ReadingList list = askAndAnswer("choice", "[\"" + MANUAL + "\", \"none\"]", MANUAL, null);

		assertFalse(list.seenUrls().contains(MANUAL));
	}

	/** What the user writes beside the options is the user's own words. */
	public void testAnAddressInTheOwnWordsIsGiven() throws Exception {
		ReadingList list = askAndAnswer("choice", "[\"Europe\", \"Asia\"]", "", "Use " + MANUAL);

		assertTrue(list.seenUrls().contains(MANUAL));
	}

	/**
	 * Asks a question on a thread of its own, as the agent does, with the run's reading list bound,
	 * and answers it from this one, as the chat servlet does.
	 */
	private ReadingList askAndAnswer(String type, String options, String response, String ownWords)
			throws Exception {
		ReadingList list = new ReadingList();
		UserInteractionStore store = new UserInteractionStore();
		CompletableFuture<String> asking = new CompletableFuture<>();
		Thread agent = new Thread(() -> {
			try (UserInteractionStoreBinding.Binding binding = UserInteractionStoreBinding.bind(store);
					ReadingListBinding.Binding reading = ReadingListBinding.bind(list)) {
				asking.complete(_tool.askUser("Which source should I read?", "Source", type, options,
					Boolean.FALSE, ToolContext.unrestricted()));
			} catch (RuntimeException ex) {
				asking.completeExceptionally(ex);
			}
		}, "TestAskUserAddresses-agent");
		agent.setDaemon(true);
		agent.start();
		try {
			UserInteractionRequest pending = awaitPending(store);
			store.resolve(pending.getId(), response, ownWords);
			asking.get(10, TimeUnit.SECONDS);
			return list;
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
