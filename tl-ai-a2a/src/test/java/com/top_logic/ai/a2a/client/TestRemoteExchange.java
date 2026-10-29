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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.top_logic.ai.a2a.A2AArtifactSource;
import com.top_logic.ai.a2a.A2AExchange;
import com.top_logic.ai.a2a.A2AService;
import com.top_logic.ai.a2a.ContextBook;
import com.top_logic.ai.a2a.client.RemoteAgents.RemoteAgent;
import com.top_logic.ai.a2a.protocol.A2AJson;
import com.top_logic.ai.a2a.protocol.ArtifactRef;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.ai.a2a.protocol.TaskState;
import com.top_logic.ai.a2a.server.A2ARequestHandler;
import com.top_logic.ai.a2a.server.A2ATask;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.credential.CredentialProvider;

/**
 * Test case for {@link RemoteExchange} and {@link A2AClient}: the client side over real HTTP and
 * server-sent events against this module's own JSON-RPC handler, with a scripted agent.
 */
public class TestRemoteExchange extends TestCase {

	private static final String TOKEN = "test-token-123";

	private HttpServer _server;

	private ContextBook _contexts;

	private final List<String> _authorizations = new CopyOnWriteArrayList<>();

	private final List<Map<String, Object>> _metadata = new CopyOnWriteArrayList<>();

	private final List<Map<String, Object>> _messages = new CopyOnWriteArrayList<>();

	private RemoteAgents _agents;

	private final List<A2AExchange> _served = new CopyOnWriteArrayList<>();

	private final List<A2AExchange> _asked = new CopyOnWriteArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		A2ARequestHandler handler = new A2ARequestHandler(new ScriptedAgent(_metadata, _messages), "alpha"::equals, 10_000,
			_served::add);
		_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_server.setExecutor(Executors.newCachedThreadPool());
		String base = "http://127.0.0.1:" + _server.getAddress().getPort();
		_server.createContext("/a2a/alpha", exchange -> serve(exchange, handler, base));
		_server.createContext("/a2a-artifacts", this::serveArtifact);
		_server.start();

		System.setProperty("a2a_test_token", TOKEN);
		_agents = new RemoteAgents(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, List.of());
		_agents.add(new RemoteAgent("alpha", base + "/a2a/alpha/agent-card.json",
			request -> request.setHeader(CredentialProvider.AUTHORIZATION,
				CredentialProvider.BEARER + " " + System.getProperty("a2a_test_token"))));
		_contexts = new ContextBook();
	}

	@Override
	protected void tearDown() throws Exception {
		_server.stop(0);
		System.clearProperty("a2a_test_token");
		super.tearDown();
	}

	public void testAskBackReplyAndProgress() throws Exception {
		RemoteExchange.Caller caller = new RemoteExchange.Caller("beta", "Beta", "conv-1", "project-7", "Projekt Prüfmittel",
			"run-1");
		List<String> progress = new CopyOnWriteArrayList<>();

		RemoteExchange exchange =
			new RemoteExchange(_agents, _contexts, caller, 10_000, progress::add, () -> false, _asked::add);
		RemoteExchange.Outcome asked = exchange.ask("alpha", "ask: build REQ-001", null, null, null, List.of());
		assertEquals(TaskState.INPUT_REQUIRED, asked._state);
		assertEquals("Which site?", asked._statusText);
		assertTrue("The remote agent's steps reach the caller: " + progress,
			progress.contains("Alpha: reading ask: build REQ-001"));
		assertEquals("The caller names itself, its conversation and the conversation's subject.",
			Json.map("app", "beta", "agent", "Beta", "conversationId", "conv-1", "subject", "Projekt Prüfmittel"),
			Json.obj(_metadata.get(0), "tlAi"));
		assertEquals("The bearer token is sent on every request.", List.of("Bearer " + TOKEN),
			_authorizations.stream().distinct().toList());

		String formatted = RemoteExchange.format(asked);
		assertTrue(formatted, formatted.contains("replyAgent(taskId=\"" + asked._taskId + "\""));
		assertTrue("Remote text is enveloped: " + formatted, formatted.contains("source=\"a2a:alpha\"") && formatted.contains("question:\nWhich site?"));

		RemoteExchange.Outcome done = exchange.reply(null, asked._taskId, "Werk Nord");
		assertEquals(asked._taskId, done._taskId);
		assertEquals(TaskState.COMPLETED, done._state);
		assertEquals("Built for Werk Nord", done._statusText);
		assertEquals("design.json", Json.str(new ArrayList<>(done._artifacts.values()).get(1), "name"));

		assertEquals("A follow-up of the same conversation talks in the same context.", asked._contextId,
			_contexts.contextFor("conv-1"));
		assertEquals("project-7", _contexts.rememberedSubject(asked._contextId));

		assertObservedOnBothSides(asked);
	}

	/**
	 * Both sides tell their observers of one conversation with the same ids, so that the two can be
	 * stitched together.
	 */
	private void assertObservedOnBothSides(RemoteExchange.Outcome asked) {
		A2AExchange sent = _asked.get(0);
		assertEquals(A2AExchange.Kind.MESSAGE_SENT, sent.kind());
		assertEquals(A2AExchange.Direction.OUTGOING, sent.direction());
		assertEquals("Beta", sent.agent());
		assertEquals("alpha", sent.peer());
		assertEquals("run-1", sent.runId());
		A2AExchange received = _served.get(0);
		assertEquals(A2AExchange.Kind.MESSAGE_RECEIVED, received.kind());
		assertEquals(A2AExchange.Direction.INCOMING, received.direction());
		assertEquals("The receiver sees the message the sender sent.", Json.str(sent.content(), "messageId"),
			Json.str(received.content(), "messageId"));
		assertEquals(asked._taskId, received.taskId());
		assertEquals(asked._contextId, received.contextId());

		for (List<A2AExchange> side : List.of(_served, _asked)) {
			List<String> steps = side.stream()
				.filter(step -> asked._taskId.equals(step.taskId()) && step.kind() == A2AExchange.Kind.STATE_CHANGED)
				.map(step -> step.state().name()).distinct().toList();
			assertTrue("Each side sees the task ask and complete: " + steps,
				steps.containsAll(List.of("INPUT_REQUIRED", "COMPLETED")));
			assertTrue("Each side sees the artifacts.",
				side.stream().anyMatch(step -> step.kind() == A2AExchange.Kind.ARTIFACT_PRODUCED
					&& asked._contextId.equals(step.contextId())));
		}
	}

	public void testConversationsByTopicAndHandOffByReference() throws Exception {
		RemoteExchange.Caller caller = new RemoteExchange.Caller("beta", "Beta", "conv-2", null, null, null);
		RemoteExchange.Outcome main = ask(caller, "hello", null, null, List.of());
		RemoteExchange.Outcome design = ask(caller, "design the screens", "new", "design",
			List.of("tl-artifact://beta/spec/7"));
		assertFalse("A new conversation has a context of its own.", main._contextId.equals(design._contextId));

		Map<String, Object> handedOver = _messages.get(_messages.size() - 1);
		assertTrue("The reference travels as a part, not as its content: " + A2AJson.text(handedOver),
			A2AJson.text(handedOver).contains("fetchArtifact(ref=\"tl-artifact://beta/spec/7\")"));

		assertEquals("A topic names the conversation.", design._contextId,
			ask(caller, "and the tokens?", "design", null, List.of())._contextId);
		assertEquals("Continue is the conversation last used with the agent.", design._contextId,
			ask(caller, "thanks", "continue", null, List.of())._contextId);
		assertEquals("A known context id works too.", main._contextId,
			ask(caller, "back to the first", main._contextId, null, List.of())._contextId);

		List<String> labels = new ArrayList<>();
		for (ContextBook.RemoteConversation conversation : _contexts.conversations("conv-2")) {
			labels.add(conversation.label() + "/" + conversation.lastState());
		}
		assertEquals(List.of("main/COMPLETED", "design/COMPLETED"), labels);
	}

	public void testFetchArtifactFindsTheApplicationOverItsCard() throws Exception {
		Map<String, Object> section = _agents.fetchArtifact(ArtifactRef.parse("tl-artifact://alpha-app/spec/1"), "REQ-002");
		assertEquals("## REQ-002 Kalibrierplan\nIntervall in Tagen.", section.get("content"));
		assertEquals(List.of("Spezifikation", "REQ-001 Prüfmittel", "REQ-002 Kalibrierplan"), section.get("sections"));

		Map<String, Object> missing = _agents.fetchArtifact(ArtifactRef.parse("tl-artifact://alpha-app/spec/1"), "REQ-099");
		assertNotNull(missing.get("error"));
		try {
			A2AClientTools.formatArtifact(missing, ArtifactRef.parse("tl-artifact://alpha-app/spec/1"));
			fail("An unknown section is an error.");
		} catch (com.top_logic.ai.a2a.protocol.A2AError ex) {
			assertTrue("The error lists the sections there are: " + ex.getMessage(),
				ex.getMessage().contains("REQ-002 Kalibrierplan"));
		}
	}

	public void testTheCallerReadsAShortAnswerAndReferencesNotBodies() {
		RemoteExchange.Outcome outcome = new RemoteExchange.Outcome();
		outcome._agent = "gamma";
		outcome._state = TaskState.COMPLETED;
		outcome._taskId = "t1";
		outcome._statusText = "x".repeat(RemoteExchange.MAX_ANSWER_CHARS + 500);
		outcome.acceptArtifact(withRef(A2AJson.artifact("answer", null, List.of(A2AJson.textPart(outcome._statusText))),
			"tl-artifact://gamma/task/t1/answer"));
		outcome.acceptArtifact(withRef(A2AJson.artifact("frame.html", "Frame SCR-01",
			List.of(Json.map("text", "<html>" + "y".repeat(5000) + "</html>", "mediaType", "text/html"))),
			"tl-artifact://gamma/task/t1/frame.html"));
		outcome.acceptArtifact(A2AJson.artifact("foreign", null, List.of(A2AJson.textPart("z".repeat(1000)))));

		String formatted = RemoteExchange.format(outcome);

		assertTrue(formatted, formatted.contains("full answer: fetchArtifact(ref=\"tl-artifact://gamma/task/t1/answer\")"));
		assertFalse("The answer is cut.", formatted.contains("x".repeat(RemoteExchange.MAX_ANSWER_CHARS + 1)));
		assertTrue(formatted, formatted.contains("- frame.html (text/html, 5013 characters) - Frame SCR-01\n  ref: tl-artifact://gamma/task/t1/frame.html"));
		assertFalse("No artifact body reaches the caller.", formatted.contains("yyyy"));
		outcome._statusText = "kurz";
		assertTrue("A short answer names its reference too.", RemoteExchange.format(outcome)
			.contains("kurz\n[answer ref: tl-artifact://gamma/task/t1/answer]"));
		assertTrue("A foreign artifact without reference shows a glimpse only.", formatted.contains("z".repeat(300) + "…"));
		assertFalse(formatted.contains("z".repeat(301)));
	}

	private static Map<String, Object> withRef(Map<String, Object> artifact, String ref) {
		artifact.put("metadata", Json.map("ref", ref));
		return artifact;
	}

	private RemoteExchange exchange(RemoteExchange.Caller caller, long waitMillis) {
		return new RemoteExchange(_agents, _contexts, caller, waitMillis, line -> {
			// ignored
		}, () -> false, null);
	}

	private RemoteExchange.Outcome ask(RemoteExchange.Caller caller, String text, String conversation, String topic,
			List<String> refs) throws Exception {
		return exchange(caller, 10_000).ask("alpha", text, conversation, topic, null, refs);
	}

	private void serveArtifact(HttpExchange exchange) throws IOException {
		try {
			Map<String, String> query = new java.util.HashMap<>();
			for (String pair : exchange.getRequestURI().getRawQuery().split("&")) {
				String[] kv = pair.split("=", 2);
				query.put(kv[0], java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
			}
			ArtifactRef ref = ArtifactRef.parse(query.get("ref"));
			A2AArtifactSource.Artifact spec = new A2AArtifactSource.Artifact("Spezifikation", "text/markdown",
				"# Spezifikation\n## REQ-001 Prüfmittel\nEin Messgerät.\n## REQ-002 Kalibrierplan\nIntervall in Tagen.");
			Map<String, Object> answer = A2AService.artifactResponse("1".equals(ref.id()) ? spec : null, ref, query.get("section"));
			write(exchange, answer.containsKey("error") ? 404 : 200, "application/json", Json.write(answer));
		} finally {
			exchange.close();
		}
	}

	public void testWaitLimitLeavesTheTaskToGetAgentTaskWhichWaits() throws Exception {
		RemoteExchange.Caller caller = new RemoteExchange.Caller("beta", "Beta", null, null, null, null);
		RemoteExchange.Outcome outcome = exchange(caller, 1_000).ask("alpha", "slow", null, null, null, List.of());
		assertEquals(TaskState.WORKING, outcome._state);
		assertTrue(RemoteExchange.format(outcome).contains("getAgentTask(taskId=\""));

		RemoteExchange.Outcome now = exchange(caller, 10_000).follow(null, outcome._taskId, false);
		assertEquals("Without a wait, the current state.", TaskState.WORKING, now._state);

		RemoteExchange.Outcome later = exchange(caller, 10_000).follow(null, outcome._taskId, true);
		assertEquals("getAgentTask waits for the task to end.", TaskState.COMPLETED, later._state);
		assertEquals("Echo: slow", later._statusText);
	}

	public void testUnknownAgentAndRefusedTokenAreReportedAsErrors() throws Exception {
		RemoteExchange.Caller caller = new RemoteExchange.Caller("beta", "Beta", null, null, null, null);
		try {
			exchange(caller, 1_000).ask("nobody", "hi", null, null, null, List.of());
			fail("An unknown agent was asked.");
		} catch (com.top_logic.ai.a2a.protocol.A2AError ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("Known: alpha"));
		}
		System.setProperty("a2a_test_token", "wrong");
		try {
			exchange(caller, 1_000).ask("alpha", "hi", null, null, null, List.of());
			fail("A refused token went unnoticed.");
		} catch (com.top_logic.ai.a2a.protocol.A2AError ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("refused the credentials"));
		}
	}

	private void serve(HttpExchange exchange, A2ARequestHandler handler, String base) throws IOException {
		try {
			if ("GET".equals(exchange.getRequestMethod())) {
				write(exchange, 200, "application/json", Json.write(Json.map("name", "Alpha", "description", "Builds.",
					"supportedInterfaces", List.of(Json.map("url", base + "/a2a/alpha", "protocolBinding", "JSONRPC",
						"protocolVersion", "1.0")),
					"capabilities", Json.map("extensions", List.of(Json.map("uri", A2AService.ARTIFACTS_EXTENSION,
						"params", Json.map("app", "alpha-app", "url", base + "/a2a-artifacts")))))));
				return;
			}
			String authorization = exchange.getRequestHeaders().getFirst("Authorization");
			_authorizations.add(String.valueOf(authorization));
			if (!("Bearer " + TOKEN).equals(authorization)) {
				write(exchange, 401, "application/json", "{\"error\":\"unauthorized\"}");
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

	/**
	 * Asks "Which site?" for a message starting with "ask" and then answers with two artifacts; takes
	 * two seconds for "slow"; echoes otherwise.
	 */
	private static final class ScriptedAgent implements A2ARequestHandler.TaskBackend {

		private final List<Map<String, Object>> _metadata;

		private final List<Map<String, Object>> _messages;

		ScriptedAgent(List<Map<String, Object>> metadata, List<Map<String, Object>> messages) {
			_metadata = metadata;
			_messages = messages;
		}

		@Override
		public void start(A2ATask task, Map<String, Object> message, Map<String, Object> requestMetadata) {
			_metadata.add(requestMetadata);
			_messages.add(message);
			String text = A2AJson.text(message);
			Thread run = new Thread(() -> {
				pause(100);
				task.moveTo(TaskState.WORKING, null);
				task.moveTo(TaskState.WORKING, A2AJson.textMessage(A2AJson.ROLE_AGENT, null, null, "reading " + text));
				if (text.startsWith("ask")) {
					task.requestInput(A2AJson.textMessage(A2AJson.ROLE_AGENT, null, null, "Which site?"), answer -> {
						task.addArtifact(A2AJson.artifact("answer", null, List.of(A2AJson.textPart("Built for " + answer))));
						task.addArtifact(A2AJson.artifact("design.json", null,
							List.of(A2AJson.dataPart(Json.map("frame", "FRAME-SCR-01")))));
						task.moveTo(TaskState.COMPLETED,
							A2AJson.textMessage(A2AJson.ROLE_AGENT, null, null, "Built for " + answer));
					});
					return;
				}
				if (text.startsWith("slow")) {
					pause(2_000);
				}
				task.moveTo(TaskState.COMPLETED, A2AJson.textMessage(A2AJson.ROLE_AGENT, null, null, "Echo: " + text));
			});
			run.setDaemon(true);
			run.start();
		}

		private static void pause(long millis) {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
