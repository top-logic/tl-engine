/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.tools.ToolCatalog;

/**
 * Tests that a rate-limited model is waited for long enough to outlast another request of the same
 * key, instead of losing the turn after three short retries (#678).
 *
 * @see AgentLoop#retryWait(int, Duration, long, long)
 */
public class TestRateLimitWait extends TestCase {

	/** The agent loop executes inside a TopLogic interaction, as in {@code TestAgentLoop}. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestRateLimitWait.class)));
	}

	private static final String MODEL = "test-model";

	/** What the provider asks for: short, so the test does not sleep for real. */
	private static final Duration RETRY_AFTER = Duration.ofMillis(10);

	private static final long WINDOW = Duration.ofMinutes(3).toMillis();

	/** Refuses the given number of calls as rate limited, then answers. */
	private static final class BusyProvider implements LLMProvider {

		private int _refusals;

		private int _calls;

		BusyProvider(int refusals) {
			_refusals = refusals;
		}

		int calls() {
			return _calls;
		}

		@Override
		public void stream(ProviderRequest request, StreamHandler handler) throws ProviderException {
			_calls++;
			if (_refusals-- > 0) {
				throw new ProviderException(ProviderException.Kind.RATE_LIMITED, 429,
					"Only one concurrent request per API key.", RETRY_AFTER, null);
			}
			List<ContentPart> content = List.of(new TextContent("answer"));
			handler.onEvent(new StreamEvent.TextDelta(0, "answer"));
			handler.onEvent(new StreamEvent.MessageComplete("msg", MODEL, "stop", TokenUsage.EMPTY, content));
		}
	}

	private static List<AgentEvent> run(BusyProvider provider, List<AgentEvent> events) {
		ToolCatalog catalog = new ToolCatalog();
		catalog.freeze();
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Option B, please."))));
		new AgentLoop(new AgentParams(MODEL, "You are a test assistant.", 5,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), provider, catalog).run(conversation, events::add);
		return events;
	}

	private static List<Long> waits(List<AgentEvent> events) {
		return events.stream()
			.filter(AgentEvent.RetryScheduled.class::isInstance)
			.map(event -> Long.valueOf(((AgentEvent.RetryScheduled) event).waitMs()))
			.toList();
	}

	private static StopReason stopReason(List<AgentEvent> events) {
		return ((AgentEvent.RunCompleted) events.get(events.size() - 1)).reason();
	}

	/**
	 * The bug: a model held by another request for longer than three retries took the turn with
	 * it. A run that has no other model to go to keeps asking.
	 */
	public void testRunOutlastsAModelThatIsBusyForLong() {
		BusyProvider provider = new BusyProvider(6);

		List<AgentEvent> events = run(provider, new ArrayList<>());

		assertEquals(StopReason.COMPLETE, stopReason(events));
		assertEquals("Six refusals and the answer.", 7, provider.calls());
	}

	/** The pauses grow, starting at what the provider asked for. */
	public void testPausesDoubleFromRetryAfter() {
		List<AgentEvent> events = run(new BusyProvider(5), new ArrayList<>());

		assertEquals(List.of(10L, 20L, 40L, 80L, 160L), waits(events));
	}

	/** Without a Retry-After the backoff starts at one second and doubles up to its ceiling. */
	public void testBackoffWithoutRetryAfter() {
		assertEquals(1_000, AgentLoop.retryWait(0, null, 0, WINDOW));
		assertEquals(2_000, AgentLoop.retryWait(1, null, 0, WINDOW));
		assertEquals(16_000, AgentLoop.retryWait(4, null, 0, WINDOW));
		assertEquals(AgentTuning.MAX_BACKOFF_MS, AgentLoop.retryWait(5, null, 0, WINDOW));
		assertEquals(AgentTuning.MAX_BACKOFF_MS, AgentLoop.retryWait(40, null, 0, WINDOW));
	}

	/** Asking earlier than the provider said only earns another refusal. */
	public void testNeverSoonerThanRetryAfter() {
		assertEquals(60_000, AgentLoop.retryWait(0, Duration.ofSeconds(60), 0, WINDOW));
	}

	/** The total wait is bounded: the last pause is cut to what is left, then the run gives up. */
	public void testTheWindowBoundsTheTotalWait() {
		assertEquals(1_000, AgentLoop.retryWait(8, null, WINDOW - 1_000, WINDOW));
		assertEquals(0, AgentLoop.retryWait(8, null, WINDOW, WINDOW));
		assertEquals(0, AgentLoop.retryWait(0, null, 0, 0));
	}
}
