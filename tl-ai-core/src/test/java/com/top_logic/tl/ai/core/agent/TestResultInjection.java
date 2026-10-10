/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResultDelivery;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.ConversationLogListenerBinding;
import com.top_logic.tl.ai.core.session.ConversationLogReplay;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Tests that a subagent finishing between two turns reaches the model in the next request.
 *
 * @see AgentLoop#injectSubagentResults(Conversation, com.top_logic.tl.ai.core.agent.ToolContext)
 */
public class TestResultInjection extends TestCase {

	/** The agent loop executes inside a TopLogic interaction, as in {@code TestAgentLoop}. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestResultInjection.class)));
	}

	private static final String MODEL = "test-model";

	private SubagentJobRegistry _registry;

	private SubagentRegistryBinding.Binding _binding;

	/** Every test runs with a thread-bound registry of its own. */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_registry = new SubagentJobRegistry(4);
		_binding = SubagentRegistryBinding.bind(_registry);
	}

	@Override
	protected void tearDown() throws Exception {
		_binding.close();
		_registry.shutdown();
		super.tearDown();
	}

	/** Registers a job that has already finished with the given final text. */
	private SubagentJob finished(String agentName, String finalText) {
		SubagentJob job = _registry.register(agentName, "do " + agentName, new CancellationToken());
		job.markTerminal(SubagentJobStatus.DONE,
			new SubagentResult(finalText, TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE));
		return job;
	}

	/**
	 * Records the {@link ProviderRequest} of every call and answers with the given texts.
	 */
	private static final class CapturingProvider implements LLMProvider {

		private final List<ProviderRequest> _requests = new ArrayList<>();

		private final List<String> _answers;

		CapturingProvider(String... answers) {
			_answers = List.of(answers);
		}

		List<ProviderRequest> requests() {
			return _requests;
		}

		@Override
		public void stream(ProviderRequest request, com.top_logic.tl.ai.core.provider.StreamHandler handler) {
			int index = _requests.size();
			_requests.add(request);
			String answer = index < _answers.size() ? _answers.get(index) : "done";
			List<ContentPart> content = List.of(new TextContent(answer));
			handler.onEvent(new StreamEvent.TextDelta(0, answer));
			handler.onEvent(new StreamEvent.MessageComplete(
				"msg-" + index, MODEL, "end_turn", TokenUsage.EMPTY, content));
		}
	}

	private static AgentParams config() {
		return new AgentParams(MODEL, "You are a test assistant.", 5,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	private static ToolCatalog emptyRegistry() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.freeze();
		return catalog;
	}

	/** All text the given request carries, concatenated. */
	private static String textOf(ProviderRequest request) {
		StringBuilder text = new StringBuilder();
		for (Message message : request.messages()) {
			if (message instanceof UserMessage user) {
				for (ContentPart part : user.content()) {
					if (part instanceof TextContent content) {
						text.append(content.text()).append('\n');
					}
				}
			}
		}
		return text.toString();
	}

	/**
	 * The bug of #359: the result of a subagent that finished between two turns has to be in
	 * the request the model gets next, not merely in the conversation object.
	 */
	public void testResultReachesTheNextProviderRequest() {
		finished("researcher", "The answer is 42.");

		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Any news?"))));

		CapturingProvider provider = new CapturingProvider("Thanks.");
		new AgentLoop(config(), provider, emptyRegistry())
			.run(conversation, (AgentEventListener) e -> {
				/* no listener */
			});

		assertEquals("One provider call expected.", 1, provider.requests().size());
		String sent = textOf(provider.requests().get(0));
		assertTrue("The subagent's result must be in the request the model receives, but was:\n"
			+ sent, sent.contains("The answer is 42."));
		assertTrue("The result must name the subagent that produced it.", sent.contains("researcher"));
	}

	/**
	 * The delivery must not read as something the user typed - it arrives in the user's role,
	 * so the text itself has to say who is speaking.
	 */
	public void testDeliveryIsMarkedAsNotTheUser() {
		finished("researcher", "The answer is 42.");

		Conversation conversation = new Conversation();
		AgentLoop.injectSubagentResults(conversation, null);

		assertEquals(1, conversation.size());
		Message injected = conversation.messages().get(0);
		assertTrue("A user message is the only shape every provider transmits.",
			injected instanceof UserMessage);
		assertTrue("The text must mark itself as a framework notice.",
			text(injected).startsWith(SubagentResultDelivery.NOTICE));
	}

	/**
	 * Two subagents finishing in the same gap arrive as one message, in the order the parent
	 * started them.
	 */
	public void testBatchIsOneMessageInStartOrder() {
		finished("first", "Result one.");
		finished("second", "Result two.");

		Conversation conversation = new Conversation();
		AgentLoop.injectSubagentResults(conversation, null);

		assertEquals("A batch is one delivery, not one message per subagent.",
			1, conversation.size());
		String delivered = text(conversation.messages().get(0));
		assertTrue(delivered.contains("Result one."));
		assertTrue(delivered.contains("Result two."));
		assertTrue("The subagent started first must be reported first.",
			delivered.indexOf("Result one.") < delivered.indexOf("Result two."));
	}

	/**
	 * Registration order is the counter's order, not the id's alphabet: past ten subagents the
	 * two differ.
	 */
	public void testOrderSurvivesPastTenSubagents() {
		for (int i = 1; i <= 11; i++) {
			finished("agent" + i, "Result " + i + ".");
		}

		Conversation conversation = new Conversation();
		AgentLoop.injectSubagentResults(conversation, null);

		String delivered = text(conversation.messages().get(0));
		assertTrue("sa-2 started before sa-11 and must be reported before it.",
			delivered.indexOf("Result 2.") < delivered.indexOf("Result 11."));
	}

	/** A result is delivered once; the turn after it must not repeat it. */
	public void testDeliveredOnlyOnce() {
		finished("researcher", "The answer is 42.");

		Conversation conversation = new Conversation();
		AgentLoop.injectSubagentResults(conversation, null);
		AgentLoop.injectSubagentResults(conversation, null);

		assertEquals("Second injection must find nothing left to deliver.", 1, conversation.size());
	}

	/** A turn with no finished subagent must look exactly as it did before. */
	public void testNothingInjectedWithoutResults() {
		_registry.register("running", "still working", new CancellationToken());

		Conversation conversation = new Conversation();
		AgentLoop.injectSubagentResults(conversation, null);

		assertEquals(0, conversation.size());
	}

	/**
	 * A subagent that finishes while no run is in progress keeps its result until the run that
	 * the user's next message starts.
	 */
	public void testResultArrivingBetweenRunsIsDeliveredWithTheNextMessage() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Start the research."))));

		// First run: the subagent is still working, so nothing is delivered.
		SubagentJob job = _registry.register("researcher", "research", new CancellationToken());
		CapturingProvider first = new CapturingProvider("Started it.");
		new AgentLoop(config(), first, emptyRegistry())
			.run(conversation, (AgentEventListener) e -> {
				/* no listener */
			});
		assertFalse("Nothing to deliver while the subagent runs.",
			textOf(first.requests().get(0)).contains("The answer is 42."));

		// Between the runs - nobody is listening.
		job.markTerminal(SubagentJobStatus.DONE,
			new SubagentResult("The answer is 42.", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE));

		// The user writes again: that run's first turn carries the result.
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Anything back?"))));
		CapturingProvider second = new CapturingProvider("Yes: 42.");
		new AgentLoop(config(), second, emptyRegistry())
			.run(conversation, (AgentEventListener) e -> {
				/* no listener */
			});

		assertTrue("The waiting result must ride along with the next user message.",
			textOf(second.requests().get(0)).contains("The answer is 42."));
	}

	/**
	 * The delivery is recorded on the turn, so the admin transcript shows it - as an assistant-side
	 * system part, never as a user part, because the chat draws user parts as the user's bubble.
	 */
	public void testDeliveryIsRecordedInTheTranscript() {
		finished("researcher", "The answer is 42.");

		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(
			new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), "Any news?")));

		Conversation conversation = new Conversation();
		try (ConversationLogListenerBinding.Binding binding =
			ConversationLogListenerBinding.bind(new ConversationLogListener(log))) {
			AgentLoop.injectSubagentResults(conversation, null);
		}

		List<Part> recorded = turn.assistantParts();
		assertEquals(1, recorded.size());
		Part.SystemPart part = (Part.SystemPart) recorded.get(0);
		assertEquals(Part.SystemEventType.SUBAGENT_RESULT, part.eventType());
		assertEquals("The transcript holds verbatim what the model was shown.",
			text(conversation.messages().get(0)), part.details());
		assertEquals("The delivery is not the user's text.", 1, turn.userParts().size());
	}

	/**
	 * A session reloaded from its persisted log tells the model the same thing the live run did.
	 */
	public void testDeliverySurvivesPersistAndReload() {
		finished("researcher", "The answer is 42.");

		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(
			new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), "Any news?")));

		Conversation live = new Conversation();
		try (ConversationLogListenerBinding.Binding binding =
			ConversationLogListenerBinding.bind(new ConversationLogListener(log))) {
			AgentLoop.injectSubagentResults(live, null);
		}
		log.completeTurn();

		ConversationLog reloaded = ConversationLogSerializer.fromJson(ConversationLogSerializer.toJson(log));
		Conversation rebuilt = new Conversation();
		ConversationLogReplay.rebuildMemory(reloaded, rebuilt);

		assertEquals("The user's question and the delivered result.", 2, rebuilt.size());
		assertEquals("Any news?", text(rebuilt.messages().get(0)));
		assertEquals(text(live.messages().get(0)), text(rebuilt.messages().get(1)));
	}

	/** The text of a message, however many content parts it has. */
	private static String text(Message message) {
		StringBuilder result = new StringBuilder();
		if (message instanceof UserMessage user) {
			for (ContentPart part : user.content()) {
				if (part instanceof TextContent content) {
					result.append(content.text());
				}
			}
		}
		return result.toString();
	}
}
