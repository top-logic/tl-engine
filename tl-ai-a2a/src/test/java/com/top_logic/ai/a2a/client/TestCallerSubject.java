/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.top_logic.ai.a2a.A2AService;
import com.top_logic.ai.a2a.client.RemoteAgents.RemoteAgent;
import com.top_logic.ai.a2a.protocol.A2AJson;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.ai.a2a.protocol.TaskState;
import com.top_logic.ai.a2a.server.A2ARequestHandler;
import com.top_logic.ai.a2a.server.A2ATask;
import com.top_logic.ai.core.agent.ToolContext;
import com.top_logic.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.ExecutionRecorder;
import com.top_logic.ai.core.execution.model.AgentExecution;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.ai.core.security.RunEntryPoint;
import com.top_logic.ai.core.security.RunPrincipal;
import com.top_logic.ai.core.security.RunSecurity;
import com.top_logic.ai.tool.api.ToolResult;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.model.ModelService;

/**
 * The A2A client sends the subject of the calling run to another application only when the account
 * of the run may read it.
 *
 * <p>
 * The calling conversation is about a conversation of another account. Under the access policy that
 * {@code tl-ai-core} ships for conversations, only its owner may read it. A session can hold such a
 * subject: a page writes the object it shows onto a new conversation, and the rights on an object
 * can change after that. The run states the subject of its session, as the chat does. The remote
 * agent is a scripted A2A peer over real HTTP; no language model is involved.
 * </p>
 */
public class TestCallerSubject extends BasicTestCase {

	/** May read {@link #subject}. */
	private static Person owner;

	/** May not read {@link #subject}. */
	private static Person stranger;

	/** What the calling conversations are about: a conversation of {@link #owner}. */
	private static AgentSession subject;

	private HttpServer _server;

	private RunningService _service;

	/** The {@code tlAi} metadata of each message the peer received. */
	private final List<Map<String, Object>> _received = new CopyOnWriteArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("a2a-caller-owner");
			stranger = TestPerson.createPerson("a2a-caller-stranger");
			subject = AgentExecutionStore.getInstance().createSession(SessionKind.CHAT, owner, "Website relaunch");
			HistoryUtils.updateSessionRevision();
		}

		A2ARequestHandler handler = new A2ARequestHandler(new EchoAgent(_received), "alpha"::equals, 10_000, null);
		_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_server.setExecutor(Executors.newCachedThreadPool());
		String base = "http://127.0.0.1:" + _server.getAddress().getPort();
		_server.createContext("/a2a/alpha", exchange -> serve(exchange, handler, base));
		_server.start();

		_service = new RunningService();
		_service.remoteAgents().add(new RemoteAgent("alpha", base + "/a2a/alpha/agent-card.json", List.of(),
			request -> request.setHeader("Authorization", "Bearer test")));
	}

	@Override
	protected void tearDown() throws Exception {
		_service.stop();
		_server.stop(0);
		super.tearDown();
	}

	/** The control case: a run whose account may read the subject names it to the remote agent. */
	public void testReadableSubjectIsSent() {
		askAlpha(owner);

		assertNotNull("The caller names the subject of its conversation: " + _received,
			Json.str(_received.get(0), A2AJson.SUBJECT_METADATA));
	}

	/** A run whose account may not read the subject does not name it to the remote agent. */
	public void testUnreadableSubjectIsNotSent() {
		assertFalse("Precondition: the run's account may not read the subject.",
			new ModelAccessRightsSecurity(stranger).canRead(subject));

		askAlpha(stranger);

		assertNull("The caller sent the subject of its conversation, which its account may not read: "
			+ _received, Json.str(_received.get(0), A2AJson.SUBJECT_METADATA));
	}

	/**
	 * Asks the remote agent from a run of the given account, in a conversation of that account about
	 * {@link #subject}.
	 */
	private void askAlpha(Person account) {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession conversation = store.createSession(SessionKind.CHAT, account, "About the relaunch");
		HistoryUtils.updateSessionRevision();
		store.setSubject(conversation, subject);
		AgentExecution execution =
			store.beginExecution(conversation, "run-" + account.getName(), "TL_ModelExplorer", null, null, null);
		HistoryUtils.updateSessionRevision();

		ToolContext context = new ToolContext(
			RunSecurity.contextFor(new RunPrincipal(account, RunEntryPoint.CHAT, false,
				ExecutionRecorder.conversationIdOf(conversation), subject)),
			ApprovalCallback.AUTO_APPROVE);
		context.executionHandle().set(execution);

		ToolResult result = new A2AClientTools().askAgent("alpha", "Which tickets are still open?", null, null,
			null, null, context);

		assertFalse("The remote agent answered: " + result.text(), result.failed());
		assertEquals(1, _received.size());
	}

	/** The service as it runs, configured with no agent of its own and with the peer as remote agent. */
	private static final class RunningService extends A2AService {

		RunningService() throws Exception {
			super(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config());
			startUp();
		}

		void stop() {
			shutDown();
		}

		private static Config<?> config() throws Exception {
			return (Config<?>) TypedConfiguration.parse(CharacterContents.newContent(
				"<service xmlns:config='http://www.top-logic.com/ns/config/6.0'"
					+ " config:interface='" + Config.class.getName() + "'"
					+ " class='" + A2AService.class.getName() + "' app='beta'/>"));
		}
	}

	private static void serve(HttpExchange exchange, A2ARequestHandler handler, String base) throws IOException {
		try {
			if ("GET".equals(exchange.getRequestMethod())) {
				write(exchange, 200, "application/json", Json.write(Json.map("name", "Alpha", "description", "Echoes.",
					"supportedInterfaces", List.of(Json.map("url", base + "/a2a/alpha", "protocolBinding", "JSONRPC",
						"protocolVersion", "1.0")))));
				return;
			}
			String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
			handler.handle(body, exchange.getRequestHeaders().getFirst("A2A-Version"), "beta", "alpha",
				new A2ARequestHandler.Transport() {
					@Override
					public void sendJson(String json) throws IOException {
						write(exchange, 200, "application/json", json);
					}

					@Override
					public A2ARequestHandler.EventSink openStream() throws IOException {
						exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
						exchange.sendResponseHeaders(200, 0);
						OutputStream out = exchange.getResponseBody();
						return new A2ARequestHandler.EventSink() {
							@Override
							public void send(String json) throws IOException {
								out.write(("data: " + json + "\n\n").getBytes(StandardCharsets.UTF_8));
								out.flush();
							}

							@Override
							public void keepAlive() throws IOException {
								out.write(": keepalive\n\n".getBytes(StandardCharsets.UTF_8));
								out.flush();
							}
						};
					}
				});
		} finally {
			exchange.close();
		}
	}

	private static void write(HttpExchange exchange, int status, String type, String body) throws IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", type);
		exchange.sendResponseHeaders(status, bytes.length);
		exchange.getResponseBody().write(bytes);
	}

	/** Records the caller metadata of each message and answers it at once. */
	private static final class EchoAgent implements A2ARequestHandler.TaskBackend {

		private final List<Map<String, Object>> _received;

		EchoAgent(List<Map<String, Object>> received) {
			_received = received;
		}

		@Override
		public void start(A2ATask task, Map<String, Object> message, Map<String, Object> requestMetadata) {
			Map<String, Object> caller = Json.obj(requestMetadata, A2AJson.CALLER_METADATA);
			_received.add(caller == null ? Map.of() : caller);
			// Answered on a thread of its own, as a run is: the handler opens its stream after start.
			Thread run = new Thread(() -> {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				task.moveTo(TaskState.WORKING, null);
				task.moveTo(TaskState.COMPLETED,
					A2AJson.textMessage(A2AJson.ROLE_AGENT, null, null, "Echo: " + A2AJson.text(message)));
			});
			run.setDaemon(true);
			run.start();
		}
	}

	/**
	 * The services of the conversations and of their access policy, and the tool access the context
	 * of a run is built with.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestCallerSubject.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE));
	}
}
