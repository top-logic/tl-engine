/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.agent.claude;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.TestCase;

import test.com.top_logic.factory.agent.EventLog;

import com.top_logic.basic.json.JSON;
import com.top_logic.factory.agent.WorkspaceAgent;
import com.top_logic.factory.agent.claude.ClaudeCodeProtocol;
import com.top_logic.factory.agent.claude.StreamJson;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * Test of {@link ClaudeCodeProtocol} on the {@link Fixtures} recorded from Claude Code.
 */
public class TestClaudeCodeProtocol extends TestCase {

	private static final String RUN_ID = "run-1";

	private ClaudeCodeProtocol _protocol;

	private EventLog _events;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_protocol = new ClaudeCodeProtocol(Clock.systemUTC());
		_events = new EventLog();
	}

	/**
	 * A plain answer: started with the model, one turn, the text streamed once, completed with its
	 * usage and cost.
	 */
	public void testAnswer() throws Exception {
		run(Fixtures.OK);

		AgentEvent.RunCompleted completed = _events.completion();
		assertEquals(StopReason.COMPLETE, completed.reason());
		Map<?, ?> result = result(Fixtures.OK);
		assertEquals(result.get(StreamJson.RESULT), _protocol.answer());
		assertEquals("The text is streamed once.", _protocol.answer(), _events.text());
		assertFalse(_events.of(AgentEvent.RunStarted.class).get(0).model().isEmpty());
		assertEquals(RUN_ID, completed.runId());
		assertEquals(sessionId(Fixtures.OK), _protocol.sessionId());

		assertEquals(1, _events.of(AgentEvent.TurnStarted.class).size());
		List<AgentEvent.TurnCompleted> turns = _events.of(AgentEvent.TurnCompleted.class);
		assertEquals(1, turns.size());
		TokenUsage usage = completed.totalUsage();
		assertEquals(usage, turns.get(0).turnUsage());
		Map<?, ?> resultUsage = (Map<?, ?>) result.get(StreamJson.USAGE);
		assertEquals(number(resultUsage, StreamJson.OUTPUT_TOKENS), usage.outputTokens());
		assertEquals(number(resultUsage, StreamJson.CACHE_CREATION_INPUT_TOKENS), usage.cacheWriteTokens());
		assertEquals(((Number) result.get(StreamJson.TOTAL_COST_USD)).doubleValue(),
			completed.totalCost().amount(), 1e-9);
		assertTrue(_events.of(AgentEvent.ProviderError.class).isEmpty());
		assertEquals("The window of the model the result names.", contextWindow(result), _protocol.contextWindow());
	}

	/**
	 * An answer with a tool call: the call starts and completes with its result, in two turns, and
	 * the cost is that of this message, not of the session.
	 */
	public void testToolCall() throws Exception {
		run(Fixtures.OK);
		_events = new EventLog();
		run(Fixtures.TOOL);

		assertEquals(StopReason.COMPLETE, _events.reason());
		List<AgentEvent.ToolCallStarted> started = _events.of(AgentEvent.ToolCallStarted.class);
		List<AgentEvent.ToolCallCompleted> completed = _events.of(AgentEvent.ToolCallCompleted.class);
		assertFalse("No tool call.", started.isEmpty());
		assertEquals(ids(started.stream().map(AgentEvent.ToolCallStarted::toolCallId)),
			ids(completed.stream().map(AgentEvent.ToolCallCompleted::toolCallId)));
		for (AgentEvent.ToolCallStarted call : started) {
			assertTrue("Arguments are JSON: " + call.argsJson(), JSON.fromString(call.argsJson()) instanceof Map);
			assertNull(call.mode());
		}
		for (int n = 0; n < completed.size(); n++) {
			assertEquals(started.get(n).toolName(), completed.get(n).toolName());
		}
		assertTrue("Result: " + completed, completed.stream().anyMatch(c -> c.result().contains("claude-it-demo")));
		assertTrue(_events.of(AgentEvent.ToolCallFailed.class).isEmpty());

		assertEquals(_events.of(AgentEvent.TurnStarted.class).size(), _events.of(AgentEvent.TurnCompleted.class).size());
		assertTrue(_events.of(AgentEvent.TurnStarted.class).size() >= 2);
		int toolTurn = started.get(0).turnNumber();
		assertEquals("The result belongs to the turn of its call.", toolTurn, completed.get(0).turnNumber());
		assertEquals(_protocol.answer(), _events.text());
		assertTrue(_protocol.answer().contains("claude-it-demo"));

		Map<?, ?> result = result(Fixtures.TOOL);
		double sessionCost = ((Number) result.get(StreamJson.TOTAL_COST_USD)).doubleValue();
		double messageCost = _events.completion().totalCost().amount();
		assertTrue("Cost " + messageCost + " of the session " + sessionCost, messageCost > 0 && messageCost < sessionCost);
	}

	/**
	 * Line ends inside and at the start of the streamed pieces of text reach the deltas unchanged.
	 */
	public void testLineEndsInDeltas() {
		_protocol.startRun(RUN_ID, _events);
		for (String line : Fixtures.lines(Fixtures.OK)) {
			if (line.contains("\"text_delta\"")) {
				_protocol.onLine(line.replace("\"text\":\"OK\"", "\"text\":\"1\\n2\""));
				_protocol.onLine(line.replace("\"text\":\"OK\"", "\"text\":\"\\n3\\n\""));
			} else {
				_protocol.onLine(line);
			}
		}
		assertEquals("1\n2\n3\n", _events.text());
		assertEquals(List.of("1\n2", "\n3\n"),
			_events.of(AgentEvent.TextDelta.class).stream().map(AgentEvent.TextDelta::text).toList());
	}

	/**
	 * The text of a message is streamed once even though Claude Code also writes the complete
	 * message.
	 */
	public void testNoDuplicateText() throws Exception {
		run(Fixtures.RESUME);
		assertEquals(StopReason.COMPLETE, _events.reason());
		assertEquals(_protocol.answer(), _events.text());
		assertEquals(sessionId(Fixtures.RESUME), _protocol.sessionId());
	}

	/**
	 * An answer interrupted after the user stopped it ends as stopped, with what was answered so
	 * far and the usage of its request.
	 */
	public void testInterrupted() throws Exception {
		_protocol.startRun(RUN_ID, _events);
		List<String> lines = Fixtures.lines(Fixtures.INTERRUPT);
		for (int n = 0; n < lines.size(); n++) {
			if (n == 1) {
				_protocol.cancelRequested();
			}
			boolean done = _protocol.onLine(lines.get(n));
			assertEquals(n == lines.size() - 1, done);
		}
		AgentEvent.RunCompleted completed = _events.completion();
		assertEquals(StopReason.ABORTED, completed.reason());
		assertTrue(_events.of(AgentEvent.ProviderError.class).isEmpty());
		assertEquals(_events.text(), _protocol.answer());
		assertFalse("Streamed before the stop.", _protocol.answer().isEmpty());
		assertTrue("Usage: " + completed.totalUsage(), completed.totalUsage().totalTokens() > 0);
	}

	/**
	 * A run that ends in an error without a stop of the user is an error.
	 */
	public void testErrorResult() throws Exception {
		run(Fixtures.INTERRUPT);
		assertEquals(StopReason.ERROR, _events.reason());
		assertEquals(1, _events.of(AgentEvent.ProviderError.class).size());
	}

	/**
	 * A process the proxy refuses answers with an error naming the HTTP status; its message is not
	 * streamed and reaches the chat once.
	 */
	public void testLoginFailed() throws Exception {
		run(Fixtures.LOGIN_FAILED);
		assertEquals(StopReason.ERROR, _events.reason());
		List<AgentEvent.ProviderError> errors = _events.of(AgentEvent.ProviderError.class);
		assertEquals(1, errors.size());
		assertEquals(ClaudeCodeProtocol.ERROR_KIND_API_PREFIX + 401, errors.get(0).kind());
		assertFalse(errors.get(0).message().isEmpty());
		assertEquals("The message Claude Code wrote itself.", errors.get(0).message(), _events.text());
		assertEquals(0, _events.completion().totalCost().amount(), 0);

		List<AgentEvent.RetryScheduled> retries = _events.of(AgentEvent.RetryScheduled.class);
		assertFalse("Claude Code repeats the request.", retries.isEmpty());
		for (int n = 0; n < retries.size(); n++) {
			assertEquals(ClaudeCodeProtocol.ERROR_KIND_API_PREFIX + 401, retries.get(n).kind());
			assertEquals(n + 1, retries.get(n).attempt());
			assertTrue(retries.get(n).waitMs() > 0);
		}
	}

	/**
	 * A process that ends before its result ends the run in an error.
	 */
	public void testProcessEnded() throws Exception {
		_protocol.startRun(RUN_ID, _events);
		List<String> lines = Fixtures.lines(Fixtures.OK);
		for (String line : lines.subList(0, lines.size() - 1)) {
			assertFalse(_protocol.onLine(line));
		}
		_protocol.endRun(StopReason.ERROR, WorkspaceAgent.ERROR_PROCESS_EXIT, "Gone.");

		assertEquals(StopReason.ERROR, _events.reason());
		List<AgentEvent.ProviderError> errors = _events.of(AgentEvent.ProviderError.class);
		assertEquals(1, errors.size());
		assertEquals(WorkspaceAgent.ERROR_PROCESS_EXIT, errors.get(0).kind());
		assertEquals(_events.of(AgentEvent.TurnStarted.class).size(), _events.of(AgentEvent.TurnCompleted.class).size());
		assertEquals(_events.text(), _protocol.answer());
	}

	/**
	 * A tool call that is running when the process ends gets its end as failed.
	 */
	public void testProcessEndedDuringToolCall() throws Exception {
		_protocol.startRun(RUN_ID, _events);
		for (String line : Fixtures.lines(Fixtures.TOOL)) {
			if (line.contains("\"" + StreamJson.BLOCK_TOOL_RESULT + "\"")) {
				break;
			}
			_protocol.onLine(line);
		}
		_protocol.endRun(StopReason.ERROR, WorkspaceAgent.ERROR_PROCESS_EXIT, "Gone.");

		assertEquals(StopReason.ERROR, _events.reason());
		List<AgentEvent.ToolCallStarted> started = _events.of(AgentEvent.ToolCallStarted.class);
		List<AgentEvent.ToolCallFailed> failed = _events.of(AgentEvent.ToolCallFailed.class);
		assertFalse(started.isEmpty());
		assertEquals(ids(started.stream().map(AgentEvent.ToolCallStarted::toolCallId)),
			ids(failed.stream().map(AgentEvent.ToolCallFailed::toolCallId)));
	}

	/**
	 * A run ended without any line of the process still starts and completes.
	 */
	public void testEndedBeforeStart() {
		_protocol.startRun(RUN_ID, _events);
		_protocol.endRun(StopReason.ABORTED, null, null);
		assertEquals(StopReason.ABORTED, _events.reason());
		assertEquals("", _protocol.answer());
		assertEquals(2, _events.events().size());
	}

	/**
	 * The lines outside of a run produce no events; the session ID is still taken over.
	 */
	public void testOutsideOfRun() {
		for (String line : Fixtures.lines(Fixtures.OK)) {
			assertFalse(_protocol.onLine(line));
		}
		assertTrue(_events.events().isEmpty());
		assertEquals(sessionId(Fixtures.OK), _protocol.sessionId());
	}

	/**
	 * Lines that are no JSON are skipped.
	 */
	public void testNoJson() {
		_protocol.startRun(RUN_ID, _events);
		assertFalse(_protocol.onLine("not json"));
		assertFalse(_protocol.onLine("[1, 2]"));
		assertFalse(_protocol.onLine(""));
		assertTrue(_events.events().isEmpty());
	}

	/**
	 * A user message is one JSON line with the text as it is.
	 */
	public void testUserMessage() throws Exception {
		String text = "Sag \"Hallo\"\nund dann\\ nichts.";
		String input = _protocol.userMessage(text);
		assertTrue(input.endsWith("\n"));
		assertEquals("One line.", input.length() - 1, input.indexOf('\n'));
		Map<?, ?> json = (Map<?, ?>) JSON.fromString(input.strip());
		assertEquals(StreamJson.TYPE_USER, json.get(StreamJson.TYPE));
		Map<?, ?> message = (Map<?, ?>) json.get(StreamJson.MESSAGE);
		assertEquals(StreamJson.ROLE_USER, message.get(StreamJson.ROLE));
		Map<?, ?> block = (Map<?, ?>) ((List<?>) message.get(StreamJson.CONTENT)).get(0);
		assertEquals(StreamJson.BLOCK_TEXT, block.get(StreamJson.TYPE));
		assertEquals(text, block.get(StreamJson.TEXT));
	}

	/**
	 * An interrupt is a control request with an ID of its own.
	 */
	public void testInterruptRequest() throws Exception {
		Map<?, ?> first = (Map<?, ?>) JSON.fromString(_protocol.interrupt().strip());
		Map<?, ?> second = (Map<?, ?>) JSON.fromString(_protocol.interrupt().strip());
		assertEquals(StreamJson.TYPE_CONTROL_REQUEST, first.get(StreamJson.TYPE));
		assertEquals(StreamJson.SUBTYPE_INTERRUPT,
			((Map<?, ?>) first.get(StreamJson.REQUEST)).get(StreamJson.SUBTYPE));
		assertFalse(first.get(StreamJson.REQUEST_ID).equals(second.get(StreamJson.REQUEST_ID)));
	}

	private void run(String fixture) {
		_protocol.startRun(RUN_ID, _events);
		List<String> lines = Fixtures.lines(fixture);
		for (int n = 0; n < lines.size(); n++) {
			boolean done = _protocol.onLine(lines.get(n));
			assertEquals("Line " + n + " of " + fixture, n == lines.size() - 1, done);
		}
	}

	private static Set<String> ids(java.util.stream.Stream<String> ids) {
		return ids.collect(Collectors.toSet());
	}

	private static Map<?, ?> result(String fixture) throws Exception {
		List<String> lines = Fixtures.lines(fixture);
		Map<?, ?> result = (Map<?, ?>) JSON.fromString(lines.get(lines.size() - 1));
		assertEquals(StreamJson.TYPE_RESULT, result.get(StreamJson.TYPE));
		return result;
	}

	private static String sessionId(String fixture) {
		try {
			return (String) result(fixture).get(StreamJson.SESSION_ID);
		} catch (Exception ex) {
			throw new AssertionError(ex);
		}
	}

	private static int contextWindow(Map<?, ?> result) {
		Map<?, ?> models = (Map<?, ?>) result.get(StreamJson.MODEL_USAGE_SINCE_LAST_RESULT);
		return number((Map<?, ?>) models.values().iterator().next(), StreamJson.CONTEXT_WINDOW);
	}

	private static int number(Map<?, ?> json, String field) {
		return ((Number) json.get(field)).intValue();
	}

}
