/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.agent;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.factory.agent.claude.Fixtures;
import test.com.top_logic.factory.workspace.RecordingWorkspaceProvider;

import com.top_logic.ai.chat.backend.BackendTurn;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.DaemonThreadFactory;
import com.top_logic.factory.agent.AgentRuntime;
import com.top_logic.factory.agent.WorkspaceAgent;
import com.top_logic.factory.agent.claude.ClaudeCodeHarness;
import com.top_logic.factory.agent.claude.StreamJson;
import com.top_logic.factory.llm.LLMProxyService;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.ExecProcess;
import com.top_logic.factory.workspace.WorkspaceSpec;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of {@link WorkspaceAgent} with Claude Code played by a {@link ScriptedProcess} answering
 * with the recorded {@link Fixtures}: the process is started on the first message with a token
 * of its own, kept between messages, stopped after the idle timeout, resumed with the stored
 * session ID, and its end, crash and interrupt end the run as they should.
 */
public class TestWorkspaceAgent extends TestCase {

	private static final String WORKSPACE_ID = "agent-test";

	private static final String WORKING_DIR = ApplicationWorkspaces.SOURCE_PATH + "/demo";

	private static final Duration WAIT = Duration.ofSeconds(10);

	private static final Duration LONG = Duration.ofHours(1);

	private Provider _provider;

	private MemoryTokens _tokens;

	private MemoryConversation _conversation;

	private MutableClock _clock;

	private ScheduledExecutorService _scheduler;

	private ExecutorService _watchers;

	private Duration _idleTimeout = LONG;

	private Duration _interruptTimeout = WAIT;

	/**
	 * The answer of the next process to its input.
	 */
	private Function<String, List<String>> _answers = TestWorkspaceAgent::answerByFixture;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_provider = new Provider();
		_provider.create(new WorkspaceSpec(WORKSPACE_ID, "image"));
		_provider.start(WORKSPACE_ID);
		_clock = new MutableClock(Instant.parse("2026-10-23T10:00:00Z"));
		_tokens = new MemoryTokens(_clock);
		_conversation = new MemoryConversation(WORKSPACE_ID, WORKING_DIR);
		_scheduler = Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory.daemonThreadFactory());
		_watchers = Executors.newCachedThreadPool(DaemonThreadFactory.daemonThreadFactory());
	}

	@Override
	protected void tearDown() throws Exception {
		_scheduler.shutdownNow();
		_watchers.shutdownNow();
		super.tearDown();
	}

	/**
	 * The first message starts the process as the workspace user in the working directory with a
	 * token of its own, writes the message as one line, delivers the run and stores the session.
	 */
	public void testFirstMessage() throws Exception {
		WorkspaceAgent agent = agent();
		assertEquals("No window before the agent reported one.", 0, agent.contextWindow());
		EventLog events = new EventLog();
		String answer = agent.send(turn("Antworte nur mit OK."), events, new CancellationToken());

		assertEquals(StopReason.COMPLETE, events.reason());
		assertEquals("OK", answer);
		assertEquals("OK", events.text());
		assertEquals("The window the agent reported.", 1_000_000, agent.contextWindow());

		ScriptedProcess process = _provider.only();
		ExecCommand command = process.command();
		assertEquals(WORKING_DIR, command.getWorkingDir());
		assertEquals(ApplicationWorkspaces.WORKSPACE_USER, command.getUser());
		assertEquals(ClaudeCodeHarness.DEFAULT_COMMAND, command.getCommand().get(0));
		assertTrue(command.getCommand().contains(ClaudeCodeHarness.OPTION_INCLUDE_PARTIAL_MESSAGES));
		assertFalse("A new conversation.", command.getCommand().contains(ClaudeCodeHarness.OPTION_RESUME));
		assertEquals(1, _tokens.issued().size());
		String token = _tokens.issued().get(0);
		assertEquals(Map.of(LLMProxyService.ENV_AUTH_TOKEN, token), command.getEnv());
		assertFalse("The token is not on the command line.", command.getCommand().toString().contains(token));
		assertTrue("Kept for the next message.", _tokens.isValid(token));

		assertEquals(1, process.input().size());
		Map<?, ?> input = (Map<?, ?>) JSON.fromString(process.input().get(0));
		assertEquals(StreamJson.TYPE_USER, input.get(StreamJson.TYPE));
		assertTrue(process.input().get(0).contains("Antworte nur mit OK."));

		assertEquals(List.of(sessionId(Fixtures.OK)), _conversation.stored());
		assertTrue(agent.hasProcess());
	}

	/**
	 * The process serves the following messages of the conversation; the session is stored once.
	 */
	public void testProcessKeptBetweenMessages() throws Exception {
		WorkspaceAgent agent = agent();
		agent.send(turn("Antworte nur mit OK."), new EventLog(), new CancellationToken());
		EventLog events = new EventLog();
		String answer = agent.send(turn("Lies pom.xml."), events, new CancellationToken());

		assertEquals(StopReason.COMPLETE, events.reason());
		assertTrue(answer.contains("claude-it-demo"));
		assertFalse(events.of(AgentEvent.ToolCallStarted.class).isEmpty());
		assertEquals(1, _provider.processes().size());
		assertEquals(2, _provider.only().input().size());
		assertEquals(1, _tokens.issued().size());
		assertEquals(1, _conversation.stored().size());
	}

	/**
	 * An idle process is stopped and its token revoked; the next message starts a process that
	 * resumes the conversation.
	 */
	public void testIdleTimeout() throws Exception {
		_idleTimeout = Duration.ofMillis(50);
		WorkspaceAgent agent = agent();
		agent.send(turn("Antworte nur mit OK."), new EventLog(), new CancellationToken());
		ScriptedProcess first = _provider.only();
		assertTrue("Not stopped when idle.", first.awaitEnd(WAIT));
		assertTrue("Stopped by the end of its input.", first.isStdinClosed());
		awaitRevoked(_tokens.issued().get(0));
		awaitNoProcess(agent);

		EventLog events = new EventLog();
		agent.send(turn("Welche Frage?"), events, new CancellationToken());
		assertEquals(StopReason.COMPLETE, events.reason());
		assertEquals(2, _provider.processes().size());
		assertEquals(List.of(ClaudeCodeHarness.OPTION_RESUME, sessionId(Fixtures.OK)),
			resumeArgs(_provider.processes().get(1).command()));
		assertEquals(2, _tokens.issued().size());
	}

	/**
	 * A conversation with a stored session is continued by the first process.
	 */
	public void testResumeStoredSession() throws Exception {
		_conversation.storeSessionId("stored-session");
		agent().send(turn("Weiter."), new EventLog(), new CancellationToken());
		assertEquals(List.of(ClaudeCodeHarness.OPTION_RESUME, "stored-session"),
			resumeArgs(_provider.only().command()));
	}

	/**
	 * A process whose token has less than half of its lifetime left is replaced before the run.
	 */
	public void testTokenRenewal() throws Exception {
		WorkspaceAgent agent = agent();
		agent.send(turn("Antworte nur mit OK."), new EventLog(), new CancellationToken());
		_clock.advance(LONG.dividedBy(2).plusMinutes(1));
		agent.send(turn("Weiter."), new EventLog(), new CancellationToken());

		assertEquals(2, _provider.processes().size());
		assertTrue(_provider.processes().get(0).awaitEnd(WAIT));
		assertTrue(_tokens.revoked().contains(_tokens.issued().get(0)));
		assertEquals(List.of(ClaudeCodeHarness.OPTION_RESUME, sessionId(Fixtures.OK)),
			resumeArgs(_provider.processes().get(1).command()));
	}

	/**
	 * A process that ends during a run ends the run in an error; its token is revoked and the next
	 * message starts a new process.
	 */
	public void testCrash() throws Exception {
		_answers = line -> {
			List<String> lines = new ArrayList<>(Fixtures.lines(Fixtures.OK));
			lines.set(lines.size() - 1, ScriptedProcess.CRASH);
			return lines;
		};
		WorkspaceAgent agent = agent();
		EventLog events = new EventLog();
		agent.send(turn("Antworte nur mit OK."), events, new CancellationToken());

		assertEquals(StopReason.ERROR, events.reason());
		List<AgentEvent.ProviderError> errors = events.of(AgentEvent.ProviderError.class);
		assertEquals(1, errors.size());
		assertEquals(WorkspaceAgent.ERROR_PROCESS_EXIT, errors.get(0).kind());
		assertTrue(errors.get(0).message().contains(Integer.toString(ScriptedProcess.CRASH_EXIT_CODE)));
		awaitRevoked(_tokens.issued().get(0));
		assertFalse(agent.hasProcess());

		_answers = TestWorkspaceAgent::answerByFixture;
		EventLog next = new EventLog();
		agent.send(turn("Antworte nur mit OK."), next, new CancellationToken());
		assertEquals(StopReason.COMPLETE, next.reason());
		assertEquals(2, _provider.processes().size());
	}

	/**
	 * A stop of the user is sent to Claude Code as interrupt; the run ends as stopped, and the
	 * process is kept.
	 */
	public void testInterrupt() throws Exception {
		List<String> lines = Fixtures.lines(Fixtures.INTERRUPT);
		int firstText = indexOf(lines, "\"" + StreamJson.DELTA_TEXT + "\"");
		_answers = line -> {
			if (line.contains(StreamJson.TYPE_CONTROL_REQUEST)) {
				return lines.subList(firstText + 1, lines.size());
			}
			return lines.subList(0, firstText + 1);
		};
		CancellationToken cancel = new CancellationToken();
		EventLog events = new EventLog().observe(event -> {
			if (event instanceof AgentEvent.TextDelta && !cancel.isCancelled()) {
				new Thread(cancel::cancel).start();
			}
		});
		WorkspaceAgent agent = agent();
		String answer = agent.send(turn("Zähle."), events, cancel);

		assertEquals(StopReason.ABORTED, events.reason());
		assertTrue(events.of(AgentEvent.ProviderError.class).isEmpty());
		assertEquals(events.text(), answer);
		ScriptedProcess process = _provider.only();
		assertEquals(2, process.input().size());
		Map<?, ?> interrupt = (Map<?, ?>) JSON.fromString(process.input().get(1));
		assertEquals(StreamJson.TYPE_CONTROL_REQUEST, interrupt.get(StreamJson.TYPE));
		assertFalse(process.isTerminated());
		assertTrue(agent.hasProcess());
	}

	/**
	 * A process that does not stop when asked is terminated; the run ends as stopped.
	 */
	public void testInterruptIgnored() throws Exception {
		_interruptTimeout = Duration.ofMillis(100);
		List<String> lines = Fixtures.lines(Fixtures.INTERRUPT);
		int firstText = indexOf(lines, "\"" + StreamJson.DELTA_TEXT + "\"");
		_answers = line -> line.contains(StreamJson.TYPE_CONTROL_REQUEST) ? List.of() : lines.subList(0, firstText + 1);
		CancellationToken cancel = new CancellationToken();
		EventLog events = new EventLog().observe(event -> {
			if (event instanceof AgentEvent.TextDelta && !cancel.isCancelled()) {
				new Thread(cancel::cancel).start();
			}
		});
		WorkspaceAgent agent = agent();
		agent.send(turn("Zähle."), events, cancel);

		assertEquals(StopReason.ABORTED, events.reason());
		assertTrue(_provider.only().isTerminated());
		awaitRevoked(_tokens.issued().get(0));
		assertFalse(agent.hasProcess());
	}

	/**
	 * A stopped agent ends its process and revokes its token.
	 */
	public void testStop() throws Exception {
		WorkspaceAgent agent = agent();
		agent.send(turn("Antworte nur mit OK."), new EventLog(), new CancellationToken());
		agent.stop();
		assertTrue(_provider.only().awaitEnd(WAIT));
		assertEquals(_tokens.issued(), _tokens.revoked());
		assertFalse(agent.hasProcess());
	}

	/**
	 * A workspace that is not running cannot be talked to; no token is issued.
	 */
	public void testWorkspaceNotRunning() throws Exception {
		_provider.stop(WORKSPACE_ID);
		try {
			agent().send(turn("Hallo?"), new EventLog(), new CancellationToken());
			fail("Answered in a stopped workspace.");
		} catch (TopLogicException ex) {
			assertEquals(com.top_logic.factory.agent.I18NConstants.ERROR_WORKSPACE_NOT_RUNNING__WORKSPACE
				.fill(WORKSPACE_ID), ex.getErrorKey());
		}
		assertTrue(_tokens.issued().isEmpty());
		assertTrue(_provider.processes().isEmpty());
	}

	private WorkspaceAgent agent() {
		ClaudeCodeHarness harness = (ClaudeCodeHarness) TypedConfigUtil
			.createInstance(TypedConfiguration.newConfigItem(ClaudeCodeHarness.Config.class));
		AgentRuntime runtime = new AgentRuntime(harness, _provider, _tokens, _idleTimeout, LONG,
			_interruptTimeout, WAIT, _scheduler, _watchers, _clock);
		return new WorkspaceAgent(runtime, _conversation);
	}

	/**
	 * Answers the messages of a conversation with the fixtures in the order they were recorded.
	 */
	private static List<String> answerByFixture(String line) {
		if (line.contains("pom.xml")) {
			return Fixtures.lines(Fixtures.TOOL);
		}
		if (line.contains("Welche Frage")) {
			return Fixtures.lines(Fixtures.RESUME);
		}
		return Fixtures.lines(Fixtures.OK);
	}

	private static BackendTurn turn(String text) {
		return new BackendTurn("test", null, null, text, null);
	}

	private static List<String> resumeArgs(ExecCommand command) {
		List<String> args = command.getCommand();
		int index = args.indexOf(ClaudeCodeHarness.OPTION_RESUME);
		return index < 0 ? Collections.emptyList() : args.subList(index, index + 2);
	}

	private static int indexOf(List<String> lines, String text) {
		for (int n = 0; n < lines.size(); n++) {
			if (lines.get(n).contains(text)) {
				return n;
			}
		}
		throw new AssertionError("No line with " + text);
	}

	private static String sessionId(String fixture) throws Exception {
		List<String> lines = Fixtures.lines(fixture);
		return (String) ((Map<?, ?>) JSON.fromString(lines.get(lines.size() - 1))).get(StreamJson.SESSION_ID);
	}

	private void awaitRevoked(String token) throws InterruptedException {
		long deadline = System.currentTimeMillis() + WAIT.toMillis();
		while (!_tokens.revoked().contains(token)) {
			assertTrue("Token not revoked.", System.currentTimeMillis() < deadline);
			Thread.sleep(10);
		}
	}

	private static void awaitNoProcess(WorkspaceAgent agent) throws InterruptedException {
		long deadline = System.currentTimeMillis() + WAIT.toMillis();
		while (agent.hasProcess()) {
			assertTrue("Process kept.", System.currentTimeMillis() < deadline);
			Thread.sleep(10);
		}
	}

	/**
	 * Provider starting {@link ScriptedProcess}es.
	 */
	private final class Provider extends RecordingWorkspaceProvider {

		private final List<ScriptedProcess> _processes = new CopyOnWriteArrayList<>();

		@Override
		public ExecProcess startExec(String workspaceId, ExecCommand command, ExecOutput output) {
			ScriptedProcess process = new ScriptedProcess(command, output, _answers);
			_processes.add(process);
			return process;
		}

		List<ScriptedProcess> processes() {
			return _processes;
		}

		ScriptedProcess only() {
			assertEquals(1, _processes.size());
			return _processes.get(0);
		}
	}

	/**
	 * A clock the test moves on.
	 */
	private static final class MutableClock extends Clock {

		private volatile Instant _now;

		MutableClock(Instant now) {
			_now = now;
		}

		void advance(Duration duration) {
			_now = _now.plus(duration);
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return _now;
		}
	}

	/**
	 * The suite, with the type index the configured harness needs.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestWorkspaceAgent.class, TypeIndex.Module.INSTANCE);
	}

}
