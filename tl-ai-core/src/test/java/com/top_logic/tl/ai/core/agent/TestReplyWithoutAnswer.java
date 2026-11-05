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

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ScriptedModel;
import com.top_logic.tl.ai.core.provider.ScriptedModel.Reply;
import com.top_logic.tl.ai.core.provider.ScriptedModel.Script;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.ConversationLogListenerBinding;
import com.top_logic.tl.ai.core.session.ConversationLogReplay;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.core.tools.ToolCatalog;

/**
 * Tests that a reply with no text and no tool call does not end a run as a finished answer (#677).
 *
 * @see AgentLoop#unfinished(AssistantMessage)
 */
public class TestReplyWithoutAnswer extends TestCase {

	/** The agent loop executes inside a TopLogic interaction, as in {@code TestAgentLoop}. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestReplyWithoutAnswer.class)));
	}

	private static final String MODEL = "test-model";

	private static Reply thinkingOnly() {
		return Reply.of(new ThinkingContent("The calculation checks out.", null)).withFinishReason("stop");
	}

	private static Reply thinkingCutOff() {
		return Reply.of(new ThinkingContent("Let me think about this at length", null))
			.withFinishReason(ScriptedModel.MAX_TOKENS);
	}

	private static Reply answer(String text) {
		return Reply.text(text).withFinishReason("stop");
	}

	private static Reply answerCutOff(String text) {
		return Reply.text(text).withFinishReason(ScriptedModel.MAX_TOKENS);
	}

	/**
	 * A model answering its requests with the given replies, in order: the conversation does not
	 * keep an empty reply, so it does not tell the turn.
	 */
	private static ScriptedModel scripted(Reply... replies) {
		return new ScriptedModel(Script.inOrder(replies));
	}

	private static AgentLoop loop(ScriptedModel provider) {
		ToolCatalog catalog = new ToolCatalog();
		catalog.freeze();
		return new AgentLoop(new AgentParams(MODEL, "You are a test assistant.", 5,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), provider, catalog);
	}

	private static Conversation asked(String question) {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent(question))));
		return conversation;
	}

	private static final AgentEventListener NONE = event -> {
		/* not observed */
	};

	/**
	 * The bug: a reply that holds nothing but thinking is not the end of the run. The model is asked
	 * to continue, and the answer it then writes is the run's answer.
	 */
	public void testThinkingOnlyReplyIsContinued() {
		ScriptedModel provider = scripted(thinkingOnly(), answer("Revenue is 28,288."));
		Conversation conversation = asked("What is the revenue?");

		AgentResult result = loop(provider).run(conversation, NONE);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals("The empty reply must be followed by a second request.", 2, provider.requests().size());
		List<Message> sent = provider.requests().get(1).messages();
		Message last = sent.get(sent.size() - 1);
		assertTrue("The model is told to continue, in the one role every provider transmits.",
			last instanceof UserMessage);
		assertEquals(AgentLoop.CONTINUE_NOTICE, text(last));
		assertEquals("Revenue is 28,288.", AgentFactory.extractResponseText(conversation, 0));
	}

	/** A model that still says nothing ends the run with a reason of its own, after one request. */
	public void testSecondEmptyReplyEndsWithoutAnswer() {
		ScriptedModel provider = scripted(thinkingOnly(), thinkingOnly(),
			answer("never asked for"));
		List<AgentEvent> events = new ArrayList<>();

		AgentResult result = loop(provider).run(asked("What is the revenue?"), events::add);

		assertEquals(StopReason.NO_ANSWER, result.stopReason());
		assertEquals("The continuation is bounded: one request, then the run ends.",
			2, provider.requests().size());
		AgentEvent end = events.get(events.size() - 1);
		assertTrue(end instanceof AgentEvent.RunCompleted);
		assertEquals(StopReason.NO_ANSWER, ((AgentEvent.RunCompleted) end).reason());
	}

	/**
	 * A reply cut off at the output limit before it said anything is asked to continue too, and is
	 * told why.
	 */
	public void testReplyCutOffWhileThinkingIsContinued() {
		ScriptedModel provider = scripted(thinkingCutOff(), answer("Short answer."));

		AgentResult result = loop(provider).run(asked("Explain."), NONE);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		List<Message> sent = provider.requests().get(1).messages();
		assertEquals(AgentLoop.CONTINUE_NOTICE_OUTPUT_LIMIT, text(sent.get(sent.size() - 1)));
	}

	/** Text cut off at the output limit is kept, but the run does not read as complete. */
	public void testAnswerCutOffAtTheOutputLimitIsNotComplete() {
		ScriptedModel provider = scripted(answerCutOff("The first half of the"));
		Conversation conversation = asked("Explain.");

		AgentResult result = loop(provider).run(conversation, NONE);

		assertEquals(StopReason.OUTPUT_LIMIT, result.stopReason());
		assertEquals("The answer has started: asking again would only start a second one.",
			1, provider.requests().size());
		assertEquals("The first half of the", AgentFactory.extractResponseText(conversation, 0));
	}

	/**
	 * The request to continue is recorded, and a session reloaded from its persisted log puts it back
	 * between the two replies instead of merging them into one.
	 */
	public void testContinuationSurvivesPersistAndReload() {
		ConversationLog log = new ConversationLog();
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(
			new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), "What is the revenue?")));
		ConversationLogListener recorder = new ConversationLogListener(log);

		Conversation live = asked("What is the revenue?");
		ScriptedModel provider = scripted(thinkingOnly(), answer("Revenue is 28,288."));
		try (ConversationLogListenerBinding.Binding binding = ConversationLogListenerBinding.bind(recorder)) {
			loop(provider).run(live, recorder);
		}
		log.completeTurn();

		assertTrue("The transcript shows that the run asked the model to continue.",
			turn.assistantParts().stream().anyMatch(part -> part instanceof Part.SystemPart system
				&& system.eventType() == Part.SystemEventType.CONTINUATION_REQUESTED));

		ConversationLog reloaded = ConversationLogSerializer.fromJson(ConversationLogSerializer.toJson(log));
		Conversation rebuilt = new Conversation();
		ConversationLogReplay.rebuildMemory(reloaded, rebuilt);

		assertEquals(shape(live), shape(rebuilt));
		assertEquals(AgentLoop.CONTINUE_NOTICE, text(rebuilt.messages().get(2)));
	}

	/** The roles of the messages, in order. */
	private static List<String> shape(Conversation conversation) {
		return conversation.messages().stream().map(message -> message.getClass().getSimpleName()).toList();
	}

	/** The text of a message, however many content parts it has. */
	private static String text(Message message) {
		List<ContentPart> content = message instanceof UserMessage user ? user.content()
			: message instanceof AssistantMessage assistant ? assistant.content() : List.of();
		StringBuilder result = new StringBuilder();
		for (ContentPart part : content) {
			if (part instanceof TextContent text) {
				result.append(text.text());
			}
		}
		return result.toString();
	}
}
