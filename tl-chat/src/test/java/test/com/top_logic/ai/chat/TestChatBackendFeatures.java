/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.time.Instant;
import java.util.List;
import java.util.Map;


import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;


import com.top_logic.ai.chat.api.ChatTranscripts;
import com.top_logic.ai.chat.api.DtoMapper;
import com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse;
import com.top_logic.ai.chat.api.messages.ConversationFeaturesDto;
import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.BackendTurn;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.backend.I18NConstants;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ScriptedBackend;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.view.ChatCommandDriver;
import com.top_logic.ai.chat.view.ChatControl;
import com.top_logic.ai.chat.view.ChatConversationControl;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * A conversation answered by a {@link ConversationBackend} is served by what the backend supports,
 * not by what the agent policy says about the account's own agents.
 *
 * <p>
 * The commands of the chat controls are run as the browser sends them ({@link ChatCommandDriver}).
 * The backend is the test resolver {@link ScriptedBackend}, claiming one conversation per test.
 * </p>
 */
public class TestChatBackendFeatures extends BasicTestCase {

	/**
	 * An agent the policy refuses as a conversation partner whatever the account's roles say: naming
	 * it is refused in a conversation of the framework's own agent.
	 */
	private static final String INFRASTRUCTURE_AGENT = "TL_PromptImprover";

	private static final String ANSWER = "Answered from outside.";

	private static final long TIMEOUT_MILLIS = 60_000;

	private static Person anna;

	private ChatCommandDriver _driver;

	private ChatConversationControl _conversation;

	private TaskProgressTracker _window;

	private AgentSession _chat;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-with-a-backend");
		}
		_chat = (AgentSession) ChatHistoryService.createSession("Backend " + getName(), anna);
		HistoryUtils.updateSessionRevision();
		_driver = new ChatCommandDriver("backendWindow", "backend-" + getName());
		_conversation = _driver.conversation(anna);
		_window = ChatCommandDriver.tracker(_conversation);
	}

	@Override
	protected void tearDown() throws Exception {
		awaitIdle();
		ScriptedBackend.reset();
		_conversation.cleanupTree();
		_driver.close();
		super.tearDown();
	}

	/** Precondition: what the policy refusal below relies on reached the application. */
	public void testFixture() {
		assertTrue(AiToolAccessService.Module.INSTANCE.isActive());
		assertTrue(ConversationBackendService.Module.INSTANCE.isActive());
	}

	/**
	 * A send naming an agent the policy refuses goes through in a backend's conversation, and the
	 * backend answers it: no agent of this application runs there.
	 */
	public void testSendIsNotAskedAboutTheAccountsAgents() throws Exception {
		claim(BackendFeatures.NONE);

		Map<String, Object> reply = command(ChatControl.CMD_SEND, Map.of(ChatControl.ARG_TEXT, "Hallo",
			ChatControl.ARG_SESSION_ID, id(), ChatControl.ARG_AGENT, INFRASTRUCTURE_AGENT));

		assertEquals(reply.toString(), ChatControl.STATUS_PROCESSING, reply.get(ChatControl.FIELD_STATUS));
		ConversationFeaturesDto features = (ConversationFeaturesDto) reply.get(ChatControl.FIELD_FEATURES);
		assertFalse("The send tells the client what the conversation supports: " + reply, features.isRegenerate());

		awaitIdle();
		HistoryUtils.updateSessionRevision();
		String transcript = ChatHistoryService.loadConversationJson(_chat);
		assertNotNull(transcript);
		assertTrue(transcript, transcript.contains(ANSWER));
	}

	/** The same send in a conversation of the framework's own agent is refused, as before. */
	public void testInternalConversationIsStillAskedAboutTheAccountsAgents() throws Exception {
		Map<String, Object> reply = command(ChatControl.CMD_SEND, Map.of(ChatControl.ARG_TEXT, "Hallo",
			ChatControl.ARG_SESSION_ID, id(), ChatControl.ARG_AGENT, INFRASTRUCTURE_AGENT));

		assertEquals(reply.toString(), ChatControl.STATUS_FORBIDDEN, reply.get(ChatControl.FIELD_STATUS));
	}

	/** A backend that cannot answer again is refused a regenerate, with the reason. */
	public void testRegenerateIsRefused() throws Exception {
		claim(BackendFeatures.NONE);

		Map<String, Object> reply = command(ChatControl.CMD_REGENERATE, Map.of(ChatControl.ARG_SESSION_ID, id()));

		assertRefused(reply, I18NConstants.REGENERATE_NOT_SUPPORTED);
	}

	/**
	 * A backend that can answer again is not refused a regenerate for an agent the policy refuses.
	 */
	public void testSupportedRegenerateIsNotAskedAboutTheAccountsAgents() throws Exception {
		claim(new BackendFeatures(true, false, false, false, false, false, false, false));

		Map<String, Object> reply = command(ChatControl.CMD_REGENERATE,
			Map.of(ChatControl.ARG_SESSION_ID, id(), ChatControl.ARG_AGENT, INFRASTRUCTURE_AGENT));

		// The chat has no turn to answer again, which is how this request gets that far and starts
		// nothing: what is asserted is that it was not refused.
		assertEquals(reply.toString(), ChatControl.STATUS_ERROR, reply.get(ChatControl.FIELD_STATUS));
	}

	/** A backend that cannot drop turns is refused an edit. */
	public void testEditIsRefused() throws Exception {
		claim(BackendFeatures.NONE);

		Map<String, Object> reply = command(ChatControl.CMD_EDIT_SEND, Map.of(ChatControl.ARG_TEXT, "Anders",
			ChatControl.ARG_SESSION_ID, id(), ChatControl.ARG_FROM_TURN, Integer.valueOf(0)));

		assertRefused(reply, I18NConstants.EDIT_NOT_SUPPORTED);
	}

	/** A backend that accepts no files is refused a message with one. */
	public void testAttachmentIsRefused() throws Exception {
		claim(BackendFeatures.NONE);

		assertEquals(Map.of(), _driver.upload(_conversation, "dot.png", "image/png", new byte[] { 1, 2, 3, 4 }));

		Map<String, Object> reply = command(ChatControl.CMD_SEND,
			Map.of(ChatControl.ARG_TEXT, "Sieh dir das an", ChatControl.ARG_SESSION_ID, id()));

		assertRefused(reply, I18NConstants.ATTACHMENTS_NOT_SUPPORTED);
		assertEquals("A refused send leaves the file with the message.", 1,
			ChatCommandDriver.attachments(_conversation).entries().size());
	}

	/** Loading a backend's conversation tells the client what it supports. */
	public void testSessionDetailCarriesTheBackendsFeatures() throws Exception {
		claim(BackendFeatures.NONE);

		String body = detail();

		assertTrue(body, body.contains("\"features\":{"));
		assertTrue(body, body.contains("\"regenerate\":false"));
		assertTrue(body, body.contains("\"editMessages\":false"));
		assertTrue(body, body.contains("\"attachments\":false"));
		assertTrue(body, body.contains("\"approvals\":false"));
		assertFalse(body, body.contains("\":true"));
	}

	/** Loading a conversation of the framework's own agent tells the client: everything. */
	public void testSessionDetailOfAnInternalConversationSupportsEverything() throws Exception {
		String body = detail();

		assertTrue(body, body.contains("\"features\":{"));
		assertTrue(body, body.contains("\"regenerate\":true"));
		assertTrue(body, body.contains("\"agentPicker\":true"));
		assertTrue(body, body.contains("\"approvals\":true"));
		assertFalse(body, body.contains("\":false"));
	}

	private void claim(BackendFeatures features) {
		ConversationBackend backend = new ConversationBackend() {
			@Override
			public String agentName() {
				return "test-backend";
			}

			@Override
			public BackendFeatures features() {
				return features;
			}

			@Override
			public String send(BackendTurn turn, AgentEventListener sink, CancellationToken cancel) {
				String runId = "backend-" + System.nanoTime();
				TokenUsage usage = new TokenUsage(10, 5, 0, 0, 0);
				sink.onEvent(new AgentEvent.RunStarted(runId, 0, Instant.now(), null));
				sink.onEvent(new AgentEvent.TurnStarted(runId, 1, Instant.now()));
				sink.onEvent(new AgentEvent.TextDelta(runId, 1, Instant.now(), ANSWER));
				sink.onEvent(new AgentEvent.TurnCompleted(runId, 1, Instant.now(), usage, List.of()));
				sink.onEvent(new AgentEvent.RunCompleted(runId, 1, Instant.now(), StopReason.COMPLETE, usage,
					new Money(0.001, Money.USD)));
				return ANSWER;
			}
		};
		AgentSession claimed = _chat;
		ScriptedBackend.install(session -> session.equals(claimed) ? backend : null);
	}

	private static void assertRefused(Map<String, Object> reply, ResKey reason) {
		assertEquals(reply.toString(), ChatControl.STATUS_FORBIDDEN, reply.get(ChatControl.FIELD_STATUS));
		assertEquals("The client shows the reason: " + reply, Resources.getInstance().getString(reason),
			reply.get(ChatControl.FIELD_REASON));
	}

	/** Waits until no run of the test's chat is in flight, so that none outlives its test. */
	private void awaitIdle() throws InterruptedException {
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (_window.hasActiveProcessingStateFor(id())) {
			assertTrue("The run did not end.", System.currentTimeMillis() < deadline);
			Thread.sleep(20);
		}
	}

	private Map<String, Object> command(String name, Map<String, Object> arguments) {
		return _driver.command(_conversation, name, arguments);
	}

	/** The chat as Anna's window reads it when it opens it. */
	private String detail() throws Exception {
		ChatSessionDetailResponse detail = ChatTranscripts.sessionDetail(_window, anna, id());
		assertNotNull("Anna's chat must be found.", detail);
		return DtoMapper.toJson(detail);
	}

	private String id() {
		return idOf(_chat);
	}

	private static String idOf(TLObject session) {
		return session.tHandle().getObjectName().toExternalForm();
	}

	/** The suite, on the services a chat request and a backend's run pass through. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatBackendFeatures.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE,
				ConversationBackendService.Module.INSTANCE));
	}
}
