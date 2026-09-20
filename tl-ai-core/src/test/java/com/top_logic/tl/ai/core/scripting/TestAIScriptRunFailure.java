/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.util.error.TopLogicException;

/**
 * What a TL-Script caller is told when the model never answered.
 *
 * <p>
 * The contract of Gitea #389: a failed run is a failure, not an empty string. The run itself is
 * exercised here with a provider that fails the way a real one does - an HTTP error before the
 * stream, and an error event inside it - so that what {@code aiChat} reports is what the loop
 * actually observed, down to the provider's own message.
 * </p>
 */
public class TestAIScriptRunFailure extends TestCase {

	private static final String MODEL = "test-model";

	private static final Instant T0 = Instant.parse("2026-09-19T12:00:00Z");

	private static final String MESSAGE = "API key is invalid.";

	/** Fails the request the way a rejected API key does: before a single event. */
	private static final LLMProvider REFUSES_THE_REQUEST = (request, handler) -> {
		throw new ProviderException(ProviderException.Kind.AUTHENTICATION, 401, MESSAGE);
	};

	/** Fails inside the stream, which is the other half of how a provider says no. */
	private static final LLMProvider FAILS_MID_STREAM = (request, handler) -> {
		handler.onEvent(new StreamEvent.TextDelta(0, "Let me look at the inv"));
		handler.onEvent(new StreamEvent.Error(MESSAGE, ProviderException.Kind.OVERLOADED));
	};

	/** Answers normally. */
	private static final LLMProvider ANSWERS = (request, handler) -> handler
		.onEvent(new StreamEvent.MessageComplete("msg", MODEL, "end_turn", TokenUsage.EMPTY,
			List.of(new TextContent("42"))));

	private static AgentResult run(LLMProvider provider) {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("How much?"))));

		ToolCatalog tools = new ToolCatalog();
		tools.freeze();
		AgentEventListener ignore = event -> {
			/* A TL-Script call has no streaming surface - which is the point. */
		};
		return new AgentLoop(new AgentParams(MODEL, "", 1, 1024, Map.of()), provider, tools)
			.run(conversation, ignore);
	}

	private static TopLogicException failureOf(AgentResult result) {
		try {
			AIScriptFunctions.checkRunSucceeded(result);
		} catch (TopLogicException ex) {
			return ex;
		}
		throw new AssertionError("Expected the run to be reported as a failure: " + result);
	}

	private static void assertMentions(TopLogicException failure, String expected) {
		ResKey key = failure.getErrorKey();
		List<Object> arguments = Arrays.asList(key.arguments());
		assertTrue("The failure must name " + expected + ", but says " + key + " " + arguments,
			arguments.contains(expected));
	}

	/** An HTTP error reaches the script as a failure carrying the provider's kind and message. */
	public void testRefusedRequestFails() {
		AgentResult result = run(REFUSES_THE_REQUEST);

		assertEquals(StopReason.ERROR, result.stopReason());
		assertEquals(ProviderException.Kind.AUTHENTICATION.name(), result.errorKind());
		assertEquals(MESSAGE, result.errorMessage());

		TopLogicException failure = failureOf(result);
		assertMentions(failure, ProviderException.Kind.AUTHENTICATION.name());
		assertMentions(failure, MESSAGE);
	}

	/** So does an error event inside the stream, after the model already said something. */
	public void testStreamErrorFails() {
		AgentResult result = run(FAILS_MID_STREAM);

		assertEquals(StopReason.ERROR, result.stopReason());
		assertEquals(ProviderException.Kind.OVERLOADED.name(), result.errorKind());

		assertMentions(failureOf(result), MESSAGE);
	}

	/** A run that answered is not a failure, and its answer is the text the model sent. */
	public void testAnsweredRunSucceeds() {
		AgentResult result = run(ANSWERS);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertNull(result.errorKind());
		assertNull(result.errorMessage());

		// The one case that must not throw.
		AIScriptFunctions.checkRunSucceeded(result);
	}

	/**
	 * A run that stopped for a reason the provider had no part in fails as well, named by that
	 * reason: the answer is missing either way, and an empty string cannot say which happened.
	 */
	public void testRunWithoutAnAnswerFails() {
		for (StopReason reason : List.of(StopReason.MAX_TURNS, StopReason.REJECTED,
			StopReason.ABORTED, StopReason.CONTEXT_OVERFLOW)) {
			TopLogicException failure =
				failureOf(new AgentResult(reason, 1, TokenUsage.EMPTY));
			assertMentions(failure, reason.name());
		}
	}

	/** No loop, no failure: a call that never ran the model has nothing to report. */
	public void testNoResultIsNoFailure() {
		AIScriptFunctions.checkRunSucceeded(null);
	}
}
