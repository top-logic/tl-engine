/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.ai.chat.TestChatServletOwnership.Recording;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.ai.chat.api.ChatServlet;
import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.BackendTurn;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.backend.I18NConstants;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.ScriptedBackend;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
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
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A conversation answered by a {@link ConversationBackend} is served by what the backend supports,
 * not by what the agent policy says about the account's own agents.
 *
 * <p>
 * The servlet is driven directly, exactly as {@link TestChatServletOwnership} drives it. The backend
 * is the test resolver {@link ScriptedBackend}, claiming one conversation per test.
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

	private static ServletRunner runner;

	private TestChatServletOwnership.Probe _servlet;

	private TaskProgressTracker _window;

	private AgentSession _chat;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-with-a-backend");
			runner = new ServletRunner();
			runner.registerServlet("chat/*", ChatServlet.class.getName());
		}
		_chat = (AgentSession) ChatHistoryService.createSession("Backend " + getName(), anna);
		HistoryUtils.updateSessionRevision();
		_servlet = new TestChatServletOwnership.Probe();

		TLSubSessionContext subSession = TLContextManager.getSubSession();
		assertNotNull("The test needs a sub-session to hang the tracker on.", subSession);
		_window = new TaskProgressTracker();
		ChatSessionRegistry.register(subSession, null, _window);
	}

	@Override
	protected void tearDown() throws Exception {
		awaitIdle();
		ScriptedBackend.reset();
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

		Recording response = post("/send", "{\"text\":\"Hallo\",\"sessionId\":\"" + id()
			+ "\",\"agent\":\"" + INFRASTRUCTURE_AGENT + "\"}");

		assertEquals(response._body.toString(), HttpServletResponse.SC_OK, response._status);
		String body = response._body.toString();
		assertTrue(body, body.contains("\"status\":\"processing\""));
		assertTrue("The send tells the client what the conversation supports: " + body,
			body.contains("\"regenerate\":false"));

		awaitIdle();
		HistoryUtils.updateSessionRevision();
		String transcript = ChatHistoryService.loadConversationJson(_chat);
		assertNotNull(transcript);
		assertTrue(transcript, transcript.contains(ANSWER));
	}

	/** The same send in a conversation of the framework's own agent is refused, as before. */
	public void testInternalConversationIsStillAskedAboutTheAccountsAgents() throws Exception {
		Recording response = post("/send", "{\"text\":\"Hallo\",\"sessionId\":\"" + id()
			+ "\",\"agent\":\"" + INFRASTRUCTURE_AGENT + "\"}");

		assertEquals(HttpServletResponse.SC_FORBIDDEN, response._status);
		assertTrue(response._body.toString(), response._body.toString().contains("\"status\":\"forbidden\""));
	}

	/** A backend that cannot answer again is refused a regenerate, with the reason. */
	public void testRegenerateIsRefused() throws Exception {
		claim(BackendFeatures.NONE);

		Recording response = post("/regenerate", "{\"sessionId\":\"" + id() + "\"}");

		assertRefused(response, I18NConstants.REGENERATE_NOT_SUPPORTED);
	}

	/**
	 * A backend that can answer again is not refused a regenerate for an agent the policy refuses.
	 */
	public void testSupportedRegenerateIsNotAskedAboutTheAccountsAgents() throws Exception {
		claim(new BackendFeatures(true, false, false, false, false, false, false, false));

		Recording response = post("/regenerate", "{\"sessionId\":\"" + id() + "\",\"agent\":\""
			+ INFRASTRUCTURE_AGENT + "\"}");

		assertEquals(response._body.toString(), HttpServletResponse.SC_OK, response._status);
		// The chat has no turn to answer again, which is how this request gets that far and starts
		// nothing: what is asserted is that it was not refused.
		assertTrue(response._body.toString(), response._body.toString().contains("\"status\":\"error\""));
	}

	/** A backend that cannot drop turns is refused an edit. */
	public void testTruncateIsRefused() throws Exception {
		claim(BackendFeatures.NONE);

		Recording response = post("/truncate", "{\"sessionId\":\"" + id() + "\",\"fromTurn\":0}");

		assertRefused(response, I18NConstants.EDIT_NOT_SUPPORTED);
	}

	/** A backend that accepts no files is refused a message with one. */
	public void testAttachmentIsRefused() throws Exception {
		claim(BackendFeatures.NONE);

		Recording response = post("/send", "{\"text\":\"Sieh dir das an\",\"sessionId\":\"" + id()
			+ "\",\"attachments\":[{\"fileName\":\"dot.png\",\"mimeType\":\"image/png\",\"size\":4,"
			+ "\"base64Data\":\"iVBORw0K\"}]}");

		assertRefused(response, I18NConstants.ATTACHMENTS_NOT_SUPPORTED);
	}

	/** Loading a backend's conversation tells the client what it supports. */
	public void testSessionDetailCarriesTheBackendsFeatures() throws Exception {
		claim(BackendFeatures.NONE);

		String body = get("/sessions/" + id());

		assertTrue(body, body.contains("\"features\":{"));
		assertTrue(body, body.contains("\"regenerate\":false"));
		assertTrue(body, body.contains("\"editMessages\":false"));
		assertTrue(body, body.contains("\"attachments\":false"));
		assertTrue(body, body.contains("\"approvals\":false"));
		assertFalse(body, body.contains("\":true"));
	}

	/** Loading a conversation of the framework's own agent tells the client: everything. */
	public void testSessionDetailOfAnInternalConversationSupportsEverything() throws Exception {
		String body = get("/sessions/" + id());

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

	private static void assertRefused(Recording response, ResKey reason) {
		String body = response._body.toString();
		assertEquals(body, HttpServletResponse.SC_FORBIDDEN, response._status);
		assertTrue(body, body.contains("\"status\":\"forbidden\""));
		assertTrue("The client shows the reason: " + body,
			body.contains(Resources.getInstance().getString(reason)));
	}

	/** Waits until no run of the test's chat is in flight, so that none outlives its test. */
	private void awaitIdle() throws InterruptedException {
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (_window.hasActiveProcessingStateFor(id())) {
			assertTrue("The run did not end.", System.currentTimeMillis() < deadline);
			Thread.sleep(20);
		}
	}

	private Recording post(String path, String body) throws Exception {
		Recording response = new Recording();
		InvocationContext invocation = runner.newClient().newInvocation(new PostMethodWebRequest(
			"http://localhost/chat" + path,
			new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
			"application/json"));
		HttpServletRequest request = invocation.getRequest();
		login(request, response);
		_servlet.post(request, response.asResponse());
		return response;
	}

	private String get(String path) throws Exception {
		Recording response = new Recording();
		InvocationContext invocation =
			runner.newClient().newInvocation(new GetMethodWebRequest("http://localhost/chat" + path));
		HttpServletRequest request = invocation.getRequest();
		login(request, response);
		_servlet.get(request, response.asResponse());
		assertEquals(response._body.toString(), HttpServletResponse.SC_OK, response._status);
		return response._body.toString();
	}

	private static void login(HttpServletRequest request, Recording response) throws Exception {
		ThreadContext.withoutSecurityChecks(() -> {
			SessionService.getInstance().loginUser(request, response.asResponse(), anna);
		});
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
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE,
				ConversationBackendService.Module.INSTANCE));
	}
}
