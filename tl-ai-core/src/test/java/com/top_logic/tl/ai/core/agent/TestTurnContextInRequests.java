/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.memory.ByteBasedTokenEstimator;
import com.top_logic.tl.ai.core.memory.CompactionStrategy;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.memory.SummarizingStrategy;
import com.top_logic.tl.ai.core.memory.TruncatingStrategy;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
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
 * What a request carries when the host stated facts that change during the conversation.
 *
 * <p>
 * The contract of Gitea #438: the block reaches the model <em>with the turn</em>, the user's text
 * stays exactly what the user wrote, the system prompt does not move, and a compaction cannot lose
 * the newest block.
 * </p>
 */
public class TestTurnContextInRequests extends TestCase {

	private static final Instant T0 = Instant.parse("2026-09-19T12:00:00Z");

	private static final String MODEL = "test-model";

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	private static final String BLOCK =
		"<turn-context>\nTurn: 2\nTime: 14:32 (Europe/Berlin)\nOpen invoice: RE-2026-0042\n</turn-context>";

	/** Records every request a run sends and answers each with the same plain text. */
	private static final class Recorder implements LLMProvider {

		final List<ProviderRequest> requests = new ArrayList<>();

		private int _failFirstWith;

		Recorder() {
			this(0);
		}

		/** Answers the first {@code contextOverflows} calls with a context overflow. */
		Recorder(int contextOverflows) {
			_failFirstWith = contextOverflows;
		}

		@Override
		public void stream(ProviderRequest request, StreamHandler handler)
				throws ProviderException {
			requests.add(request);
			if (_failFirstWith > 0) {
				_failFirstWith--;
				throw new ProviderException(ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED, 400,
					"Too large for this test's window.");
			}
			handler.onEvent(new StreamEvent.MessageComplete("msg", MODEL, "end_turn", USAGE,
				List.of(new TextContent("Done."))));
		}
	}

	private static ToolCatalog noTools() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.freeze();
		return catalog;
	}

	private static AgentParams params(String turnContext) {
		return new AgentParams(MODEL, "You are a test assistant.",
			AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS, Map.of(),
			AgentParams.DEFAULT_CONTEXT_WINDOW_TOKENS, AgentParams.DEFAULT_MAX_RETRIES,
			AgentParams.DEFAULT_TIMEOUT_SECONDS, null, msg -> true,
			"<environment>\nUser: Ada Lovelace.\n</environment>", turnContext);
	}

	private static AgentEventListener ignore() {
		return e -> {
			/* Nothing to observe here. */
		};
	}

	private static List<String> textsOf(Message message) {
		List<String> texts = new ArrayList<>();
		for (ContentPart part : ((UserMessage) message).content()) {
			if (part instanceof TextContent text) {
				texts.add(text.text());
			}
		}
		return texts;
	}

	/** The block is its own content block of the turn, ahead of the user's untouched text. */
	public void testTheBlockTravelsBesideTheUsersText() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Is the payment term okay?"))));

		Recorder provider = new Recorder();
		new AgentLoop(params(BLOCK), provider, noTools()).run(conversation, ignore());

		assertEquals(1, provider.requests.size());
		List<Message> sent = provider.requests.get(0).messages();
		assertEquals(List.of(BLOCK, "Is the payment term okay?"), textsOf(sent.get(0)));
	}

	/** A run whose host stated nothing per turn sends what it sent before. */
	public void testNoBlockLeavesTheTurnAlone() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		Recorder provider = new Recorder();
		new AgentLoop(params(null), provider, noTools()).run(conversation, ignore());

		assertEquals(List.of("Hello"), textsOf(provider.requests.get(0).messages().get(0)));
	}

	/**
	 * The system prompt is byte-identical across the turns of one conversation.
	 *
	 * <p>
	 * This is why the block is where it is: the prompt sits in front of every request and the
	 * providers' prompt caches key on it, so a per-turn fact in it would miss the cache on every
	 * turn it changes.
	 * </p>
	 */
	public void testTheSystemPromptDoesNotMoveBetweenTurns() {
		Conversation conversation = new Conversation();
		Recorder provider = new Recorder();

		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("First question"))));
		new AgentLoop(params("<turn-context>\nTurn: 1\n</turn-context>"), provider, noTools())
			.run(conversation, ignore());

		conversation.add(new UserMessage("u2", T0, List.of(new TextContent("Second question"))));
		new AgentLoop(params("<turn-context>\nTurn: 2\n</turn-context>"), provider, noTools())
			.run(conversation, ignore());

		assertEquals(2, provider.requests.size());
		assertEquals("The system prompt must not change between the turns of one conversation.",
			provider.requests.get(0).systemPrompt(), provider.requests.get(1).systemPrompt());
		assertFalse("A per-turn fact must not reach the system prompt.",
			provider.requests.get(1).systemPrompt().contains("<turn-context>"));
	}

	/** The block goes onto the newest turn, and the turn before it keeps its own. */
	public void testEachTurnKeepsItsOwnBlock() {
		Conversation conversation = new Conversation();
		Recorder provider = new Recorder();
		String first = "<turn-context>\nTurn: 1\nOpen invoice: none\n</turn-context>";
		String second = "<turn-context>\nTurn: 2\nOpen invoice: RE-2026-0042\n</turn-context>";

		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("First question"))));
		new AgentLoop(params(first), provider, noTools()).run(conversation, ignore());

		conversation.add(new UserMessage("u2", T0, List.of(new TextContent("Second question"))));
		new AgentLoop(params(second), provider, noTools()).run(conversation, ignore());

		List<Message> sent = provider.requests.get(1).messages();
		assertEquals(List.of(first, "First question"), textsOf(sent.get(0)));
		assertEquals(List.of(second, "Second question"), textsOf(sent.get(2)));
	}

	/** Running the same turn twice does not send the block twice. */
	public void testTheBlockIsAddedOnlyOnce() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		Recorder provider = new Recorder();
		new AgentLoop(params(BLOCK), provider, noTools()).run(conversation, ignore());
		new AgentLoop(params(BLOCK), provider, noTools()).run(conversation, ignore());

		assertEquals(List.of(BLOCK, "Hello"),
			textsOf(provider.requests.get(1).messages().get(0)));
	}

	/**
	 * After a summarizing compaction the newest block still reaches the model.
	 *
	 * <p>
	 * The summarizer is driven against a budget that leaves no preserve zone at all, so the user
	 * message the block arrived on is summarized away - the one case in which the guarantee is not
	 * free.
	 * </p>
	 */
	public void testTheNewestBlockSurvivesASummarizingCompaction() {
		List<Message> retried = afterCompaction(new SummarizingStrategy(summarizer(), MODEL, 256, 1));

		assertTrue("The history must have been summarized away: " + retried,
			retried.stream().anyMatch(CompactionMarker.class::isInstance));
		assertTrue("The newest turn context must still reach the model after a compaction: "
			+ retried, carriesBlock(retried));
	}

	/**
	 * The same guarantee when the compaction leaves no user message at all.
	 *
	 * <p>
	 * {@link TruncatingStrategy} is what a failing summarizer falls back to, and it keeps whatever
	 * the tail of the conversation happens to be - which can be an assistant message alone. The
	 * block is then a message of its own, at the very end.
	 * </p>
	 */
	public void testTheNewestBlockSurvivesATruncatingCompaction() {
		List<Message> retried = afterCompaction(new TruncatingStrategy(1));

		assertFalse("This test is only about the case in which nothing the user said survives.",
			retried.subList(0, retried.size() - 1).stream().anyMatch(UserMessage.class::isInstance));
		assertTrue("The newest turn context must still reach the model after a compaction: "
			+ retried, carriesBlock(retried));
	}

	/** A summarizer that answers with one sentence, streamed the way a real one does. */
	private static LLMProvider summarizer() {
		return (request, handler) -> {
			handler.onEvent(new StreamEvent.TextDelta(0, "Earlier: the user asked about payment terms."));
			handler.onEvent(new StreamEvent.MessageComplete("sum", MODEL, "end_turn", USAGE,
				List.of(new TextContent("Earlier: the user asked about payment terms."))));
		};
	}

	/**
	 * Runs one turn whose first request overflows the context window, so the loop compacts with the
	 * given strategy and retries, and answers with the messages of that retry.
	 */
	private static List<Message> afterCompaction(CompactionStrategy strategy) {
		// A window far too small for the history below, so the preserve zone is squeezed down to
		// the floor and the turn the block arrived on is compacted away with the rest.
		ConversationMemory memory = new ConversationMemory(new Conversation(),
			new ByteBasedTokenEstimator(), strategy, 200, 0);
		for (int i = 0; i < 8; i++) {
			memory.add(new UserMessage("u" + i, T0, List.of(new TextContent("Question " + i
				+ " with enough words in it to push this conversation past the budget."))));
			memory.add(new AssistantMessage("a" + i, T0,
				List.of(new TextContent("Answer " + i
					+ " with enough words in it to push this conversation past the budget.")),
				USAGE, "end_turn"));
		}

		Recorder provider = new Recorder(1);
		new AgentLoop(params(BLOCK), provider, noTools(), memory).run(ToolContext.EMPTY, ignore());

		assertEquals("The loop must have compacted and retried.", 2, provider.requests.size());
		List<Message> retried = provider.requests.get(1).messages();
		assertTrue("The compaction must have replaced the history.",
			retried.size() < provider.requests.get(0).messages().size());
		return retried;
	}

	/** The run announces the block once, so the transcript can record it beside the user's text. */
	public void testTheBlockIsAnnouncedOnce() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		List<AgentEvent> events = new ArrayList<>();
		new AgentLoop(params(BLOCK), new Recorder(), noTools()).run(conversation, events::add);

		List<AgentEvent.TurnContextComposed> announced = events.stream()
			.filter(AgentEvent.TurnContextComposed.class::isInstance)
			.map(AgentEvent.TurnContextComposed.class::cast)
			.toList();
		assertEquals(1, announced.size());
		assertEquals(BLOCK, announced.get(0).turnContext());
	}

	/** A run without a block announces none. */
	public void testNoBlockIsAnnouncedWhenThereIsNone() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		List<AgentEvent> events = new ArrayList<>();
		new AgentLoop(params(null), new Recorder(), noTools()).run(conversation, events::add);

		assertFalse(events.stream().anyMatch(AgentEvent.TurnContextComposed.class::isInstance));
	}

	private static boolean carriesBlock(List<Message> messages) {
		for (Message message : messages) {
			if (!(message instanceof UserMessage user)) {
				continue;
			}
			for (ContentPart part : user.content()) {
				if (part instanceof TextContent text && BLOCK.equals(text.text())) {
					return true;
				}
			}
		}
		return false;
	}
}
