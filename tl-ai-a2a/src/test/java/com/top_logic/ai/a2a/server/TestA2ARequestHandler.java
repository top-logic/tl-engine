/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.ai.a2a.protocol.A2AError;
import com.top_logic.ai.a2a.protocol.A2AJson;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.ai.a2a.protocol.TaskState;

/**
 * Test case for {@link A2ARequestHandler}: the JSON-RPC binding and the task lifecycle rules, with a
 * scripted agent instead of tl-ai.
 */
public class TestA2ARequestHandler extends TestCase {

	private static final String AGENT = "alpha";

	private FakeAgent _agent;

	private A2ARequestHandler _handler;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_agent = new FakeAgent();
		_handler = new A2ARequestHandler(_agent, AGENT::equals, 10_000, null);
	}

	public void testBlockingSendAnswersWithTheCompletedTask() throws IOException {
		Map<String, Object> task = task(call("beta", "SendMessage", sendParams(null, null, "Wie heißt du?")));
		assertEquals(TaskState.COMPLETED, A2AJson.state(task));
		assertEquals("Echo: Wie heißt du?", A2AJson.plainText(Json.obj(Json.obj(task, "status"), "message")));
		assertEquals("answer", Json.str(Json.list(task, "artifacts").get(0), "name"));
		assertNotNull(Json.str(task, "contextId"));
	}

	public void testReturnImmediatelyThenGetAndCancel() throws IOException {
		Map<String, Object> params = sendParams(null, null, "hold on");
		params.put("configuration", Json.map("returnImmediately", Boolean.TRUE));
		Map<String, Object> task = task(call("beta", "SendMessage", params));
		String id = Json.str(task, "id");
		assertFalse(A2AJson.state(task).isTerminal());

		Map<String, Object> got = result(call("beta", "GetTask", Json.map("id", id)));
		assertEquals(id, Json.str(got, "id"));

		Map<String, Object> canceled = result(call("beta", "CancelTask", Json.map("id", id)));
		assertEquals(TaskState.CANCELED, A2AJson.state(canceled));
		assertTrue("The run was told to stop.", _agent._cancelled.contains(id));
		assertEquals(A2AError.TASK_NOT_CANCELABLE, errorCode(call("beta", "CancelTask", Json.map("id", id))));
	}

	public void testQuestionAndAnswerResumeTheSameTask() throws IOException {
		Map<String, Object> asked = task(call("beta", "SendMessage", sendParams(null, null, "ask: build it")));
		assertEquals(TaskState.INPUT_REQUIRED, A2AJson.state(asked));
		String id = Json.str(asked, "id");
		assertEquals("Which site?", A2AJson.plainText(Json.obj(Json.obj(asked, "status"), "message")));

		Map<String, Object> done = task(call("beta", "SendMessage", sendParams(id, null, "Werk Nord")));
		assertEquals(id, Json.str(done, "id"));
		assertEquals(TaskState.COMPLETED, A2AJson.state(done));
		assertEquals("Built for Werk Nord", A2AJson.plainText(Json.obj(Json.obj(done, "status"), "message")));
		List<String> history = new ArrayList<>();
		for (Object message : Json.list(done, "history")) {
			history.add(Json.str(message, "role") + ":" + A2AJson.plainText(message));
		}
		assertEquals(List.of("ROLE_USER:ask: build it", "ROLE_AGENT:Which site?", "ROLE_USER:Werk Nord",
			"ROLE_AGENT:Built for Werk Nord"), history);
		assertEquals("One task, one run.", 1, _agent._started.size());
	}

	public void testAMessageToTheContextOfAWaitingTaskAnswersIt() throws IOException {
		Map<String, Object> asked = task(call("beta", "SendMessage", sendParams(null, "ctx-1", "ask: build it")));
		Map<String, Object> done = task(call("beta", "SendMessage", sendParams(null, "ctx-1", "Werk Süd")));
		assertEquals(Json.str(asked, "id"), Json.str(done, "id"));
		assertEquals(TaskState.COMPLETED, A2AJson.state(done));
	}

	public void testAnotherCallerSeesNoTaskAndCannotAnswerIt() throws IOException {
		Map<String, Object> asked = task(call("beta", "SendMessage", sendParams(null, "ctx-2", "ask: build it")));
		String id = Json.str(asked, "id");
		assertEquals(A2AError.TASK_NOT_FOUND, errorCode(call("gamma", "GetTask", Json.map("id", id))));
		assertEquals(A2AError.TASK_NOT_FOUND, errorCode(call("gamma", "SendMessage", sendParams(id, null, "x"))));
		assertEquals(A2AError.UNSUPPORTED_OPERATION, errorCode(call("gamma", "SendMessage", sendParams(null, "ctx-2", "x"))));
	}

	public void testABusyContextRefusesASecondTask() throws IOException {
		Map<String, Object> params = sendParams(null, "ctx-3", "hold");
		params.put("configuration", Json.map("returnImmediately", Boolean.TRUE));
		call("beta", "SendMessage", params);
		assertEquals(A2AError.UNSUPPORTED_OPERATION, errorCode(call("beta", "SendMessage", sendParams(null, "ctx-3", "next"))));
	}

	public void testAMessageToAFinishedTaskContinuesItsContext() throws IOException {
		Map<String, Object> first = task(call("beta", "SendMessage", sendParams(null, null, "one")));
		Map<String, Object> second = task(call("beta", "SendMessage", sendParams(Json.str(first, "id"), null, "two")));
		assertFalse(Json.str(first, "id").equals(Json.str(second, "id")));
		assertEquals(Json.str(first, "contextId"), Json.str(second, "contextId"));
		assertEquals("Echo: two", A2AJson.plainText(Json.obj(Json.obj(second, "status"), "message")));
	}

	public void testStreamSendsSnapshotProgressArtifactAndFinalStatus() throws IOException {
		Recorder recorder = new Recorder();
		_handler.handle(request("SendStreamingMessage", sendParams(null, null, "stream me")), "1.0", "beta", AGENT,
			recorder);
		assertNull("A stream answers with events, not a document.", recorder._json);
		List<String> kinds = new ArrayList<>();
		for (String event : recorder._events) {
			Map<String, Object> result = Json.obj(Json.parse(event), "result");
			String kind = result.keySet().iterator().next();
			Map<String, Object> status = Json.obj(Json.obj(result, kind), "status");
			kinds.add(kind + (status == null ? "" : ":" + TaskState.parse(Json.str(status, "state"))));
		}
		assertEquals("task", kinds.get(0).replaceAll(":.*", ""));
		assertTrue(kinds.toString(), kinds.contains("statusUpdate:WORKING"));
		assertEquals("Artifacts arrive before the final status, which ends the stream.",
			List.of("artifactUpdate", "statusUpdate:COMPLETED"), kinds.subList(kinds.size() - 2, kinds.size()));
	}

	public void testProtocolErrors() throws IOException {
		Recorder recorder = new Recorder();
		_handler.handle("{not json", null, "beta", AGENT, recorder);
		Map<String, Object> response = Json.obj(Json.parse("{\"r\":" + recorder._json + "}"), "r");
		assertTrue("JSON-RPC answers a request it could not read with id null.", response.containsKey("id"));
		assertNull(response.get("id"));
		assertEquals(A2AError.PARSE_ERROR, (long) Json.integer(Json.obj(response, "error"), "code"));

		assertEquals(A2AError.METHOD_NOT_FOUND, errorCode(call("beta", "DoSomething", Json.map())));
		assertEquals(A2AError.INVALID_PARAMS, errorCode(call("beta", "SendMessage", Json.map())));
		Recorder unknownAgent = new Recorder();
		_handler.handle(request("GetTask", Json.map("id", "x")), null, "beta", "nobody", unknownAgent);
		assertEquals(A2AError.INVALID_PARAMS, errorCode(unknownAgent._json));
		Recorder version = new Recorder();
		_handler.handle(request("GetTask", Json.map("id", "x")), "2.0", "beta", AGENT, version);
		assertEquals(A2AError.VERSION_NOT_SUPPORTED, errorCode(version._json));
	}

	// ---------- helpers ----------

	private String call(String caller, String method, Map<String, Object> params) throws IOException {
		Recorder recorder = new Recorder();
		_handler.handle(request(method, params), "1.0", caller, AGENT, recorder);
		return recorder._json;
	}

	private static String request(String method, Map<String, Object> params) {
		return Json.write(Json.map("jsonrpc", "2.0", "id", 7L, "method", method, "params", params));
	}

	private static Map<String, Object> sendParams(String taskId, String contextId, String text) {
		Map<String, Object> message = A2AJson.message(A2AJson.ROLE_USER, contextId, taskId, List.of(A2AJson.textPart(text)));
		return Json.map("message", message);
	}

	private static Map<String, Object> result(String json) {
		Map<String, Object> response = Json.obj(Json.parse("{\"r\":" + json + "}"), "r");
		assertNull(json, response.get("error"));
		return Json.obj(response, "result");
	}

	private static Map<String, Object> task(String json) {
		return Json.obj(result(json), "task");
	}

	private static long errorCode(String json) {
		Map<String, Object> error = Json.obj(Json.parse(json), "error");
		assertNotNull("Expected an error: " + json, error);
		return Json.integer(error, "code");
	}

	private static final class Recorder implements A2ARequestHandler.Transport {

		String _json;

		final List<String> _events = new ArrayList<>();

		@Override
		public void sendJson(String json) {
			_json = json;
		}

		@Override
		public A2ARequestHandler.EventSink openStream() {
			return new A2ARequestHandler.EventSink() {
				@Override
				public void send(String json) {
					_events.add(json);
				}

				@Override
				public void keepAlive() {
					// Not recorded.
				}
			};
		}
	}

	/**
	 * An agent that echoes, asks "Which site?" for a message starting with "ask", keeps working
	 * forever on a message starting with "hold", and starts late on one starting with "stream".
	 */
	static final class FakeAgent implements A2ARequestHandler.TaskBackend {

		final List<String> _started = new java.util.concurrent.CopyOnWriteArrayList<>();

		final List<String> _cancelled = new java.util.concurrent.CopyOnWriteArrayList<>();

		@Override
		public void start(A2ATask task, Map<String, Object> message, Map<String, Object> requestMetadata) {
			_started.add(task.id());
			task.setCancelHandler(() -> _cancelled.add(task.id()));
			String text = A2AJson.text(message);
			Thread run = new Thread(() -> {
				if (text.startsWith("stream")) {
					// Lets the stream subscribe before anything happens.
					pause(200);
				}
				task.moveTo(TaskState.WORKING, null);
				task.moveTo(TaskState.WORKING, A2AJson.textMessage(A2AJson.ROLE_AGENT, null, null, "reading " + text));
				if (text.startsWith("ask")) {
					task.requestInput(A2AJson.textMessage(A2AJson.ROLE_AGENT, null, null, "Which site?"),
						answer -> complete(task, "Built for " + answer));
				} else if (!text.startsWith("hold")) {
					complete(task, "Echo: " + text);
				}
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

		private static void complete(A2ATask task, String answer) {
			task.addArtifact(A2AJson.artifact("answer", null, List.of(A2AJson.textPart(answer))));
			task.moveTo(TaskState.COMPLETED, A2AJson.textMessage(A2AJson.ROLE_AGENT, null, null, answer));
		}
	}
}
