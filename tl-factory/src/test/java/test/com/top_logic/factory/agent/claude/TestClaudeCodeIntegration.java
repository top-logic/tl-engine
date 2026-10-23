/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.agent.claude;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.factory.agent.EventLog;
import test.com.top_logic.factory.agent.MemoryConversation;
import test.com.top_logic.factory.agent.MemoryTokens;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import com.top_logic.ai.chat.backend.BackendTurn;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.DaemonThreadFactory;
import com.top_logic.factory.agent.AgentLaunch;
import com.top_logic.factory.agent.AgentProtocol;
import com.top_logic.factory.agent.AgentRuntime;
import com.top_logic.factory.agent.WorkspaceAgent;
import com.top_logic.factory.agent.claude.ClaudeCodeHarness;
import com.top_logic.factory.llm.LLMProxy;
import com.top_logic.factory.llm.LLMProxyService;
import com.top_logic.factory.llm.LLMProxyServlet;
import com.top_logic.factory.llm.LLMUpstream;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.ExecProcess;
import com.top_logic.factory.workspace.WorkspaceSpec;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.factory.workspace.docker.DockerWorkspaceProvider;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Integration test of Claude Code in a workspace container, talking to the Anthropic API through
 * an {@link LLMProxyServlet} of this test.
 *
 * <p>
 * {@link #testRecordStreams()} records the stream-json output of the pinned Claude Code version,
 * the {@link Fixtures} of the parser tests; {@link #testBackend()} runs a conversation through
 * {@link WorkspaceAgent}. A new Claude Code version is checked with this test: record the streams
 * anew, compare them with the fixtures, and run the parser tests on them.
 * </p>
 *
 * <p>
 * The test talks to the real API and costs a few cents, so it only runs with the system property
 * {@value #ENABLE_PROPERTY}{@code =true} and the API key in the environment variable
 * {@value #API_KEY_ENV}, as for the control application:
 * </p>
 *
 * <pre>
 * set -a; . anthropic.env; set +a
 * mvn test -DskipTests=false -pl tl-factory -Dtest=test.com.top_logic.factory.agent.claude.TestClaudeCodeIntegration \
 *     -Dtl.factory.integration=true -Dtl.factory.record=tl-factory/src/test/java/test/com/top_logic/factory/agent/claude/fixtures
 * </pre>
 *
 * <p>
 * With {@value #RECORD_PROPERTY}{@code =<dir>} the streams are written to that directory. The
 * containers need to reach the test's proxy on the host as {@value #GATEWAY_HOST}.
 * </p>
 */
public class TestClaudeCodeIntegration extends TestCase {

	/** System property enabling this test. */
	public static final String ENABLE_PROPERTY = "tl.factory.integration";

	/** System property naming the directory the recorded streams are written to. */
	public static final String RECORD_PROPERTY = "tl.factory.record";

	/** Environment variable holding the Anthropic API key, as for the control application. */
	public static final String API_KEY_ENV = "tl_anthropic_api_key";

	/** Marker of an unencrypted value of an encrypted configuration property. */
	private static final String UNENCRYPTED_PREFIX = "unencrypted:";

	private static final String API_URL = "https://api.anthropic.com";

	private static final String API_VERSION = "2023-06-01";

	private static final String NAME_PREFIX = "tl-factory-it";

	private static final String LABEL_NAMESPACE = "test.tl-factory.it";

	private static final String DEFAULT_DOCKER_HOST = "unix:///var/run/docker.sock";

	private static final String GATEWAY_HOST = "host.docker.internal";

	private static final String CONTEXT_PATH = "/it";

	private static final String APP_DIR = ApplicationWorkspaces.SOURCE_PATH + "/demo";

	private static final String POM = "<project><modelVersion>4.0.0</modelVersion><groupId>com.example</groupId>"
		+ "<artifactId>claude-it-demo</artifactId><version>1.0</version></project>";

	/** A cheap model for the conversation through the backend. */
	private static final String BACKEND_MODEL = "haiku";

	private static final String RESULT_LINE = "\"type\":\"result\"";

	private static final String TEXT_DELTA = "\"text_delta\"";

	private static final Duration WAIT = Duration.ofMinutes(5);

	private static final String PROMPT_OK = "Antworte nur mit OK.";

	private static final String PROMPT_TOOL = "Lies die Datei pom.xml und nenne nur die artifactId.";

	private static final String PROMPT_LONG =
		"Zähle langsam von 1 bis 200, jede Zahl in einer eigenen Zeile mit einem kurzen Satz dazu.";

	private DockerWorkspaceProvider _provider;

	private Server _jetty;

	private MemoryTokens _tokens;

	private String _workspaceId;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tokens = new MemoryTokens(Clock.systemUTC());
		LLMUpstream upstream =
			new LLMUpstream(API_URL, apiKey(), API_VERSION, Duration.ofSeconds(10), Duration.ofMinutes(10), Map.of());
		LLMProxy proxy = new LLMProxy() {
			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public boolean isAllowedSource(String remoteAddress) {
				return true;
			}

			@Override
			public boolean authenticate(String token) {
				return _tokens.isValid(token);
			}

			@Override
			public LLMUpstream upstream() {
				return upstream;
			}
		};
		_jetty = new Server();
		ServerConnector connector = new ServerConnector(_jetty);
		// The containers reach the host through the gateway of their network.
		connector.setHost("0.0.0.0");
		connector.setPort(0);
		_jetty.addConnector(connector);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath(CONTEXT_PATH);
		context.addServlet(new ServletHolder(new LLMProxyServlet(() -> proxy)), LLMProxyServlet.PATH + "/*");
		_jetty.setHandler(context);
		_jetty.start();
		String proxyUrl = "http://" + GATEWAY_HOST + ":" + connector.getLocalPort() + CONTEXT_PATH + LLMProxyServlet.PATH;

		_provider = createProvider();
		ApplicationWorkspaces applications =
			new ApplicationWorkspaces(TypedConfiguration.newConfigItem(ApplicationWorkspaces.Config.class), _provider);
		String image = applications.ensureImage(ExecOutput.NONE);
		_workspaceId = "claude-" + UUID.randomUUID().toString().substring(0, 8);
		_provider.create(new WorkspaceSpec(_workspaceId, image)
			// No application: the main process only keeps the container alive.
			.putEnv(ApplicationWorkspaces.ENV_BUILT_MARKER, "/tmp/never-built")
			.putEnv(LLMProxyService.ENV_BASE_URL, proxyUrl));
		_provider.start(_workspaceId);
		_provider.exec(_workspaceId, ExecCommand.of("mkdir", "-p", APP_DIR), ExecOutput.NONE);
		_provider.exec(_workspaceId,
			ExecCommand.of("sh", "-c", "printf '%s' '" + POM + "' > pom.xml").setWorkingDir(APP_DIR), ExecOutput.NONE);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			_provider.cleanup(Collections.emptyList());
		} finally {
			_provider.close();
			_jetty.stop();
			super.tearDown();
		}
	}

	/**
	 * Records the streams of a conversation with the command line of {@link ClaudeCodeHarness}: a
	 * plain answer, a tool use, an interrupted answer, a resumed process, and a process whose token
	 * the proxy refuses.
	 */
	public void testRecordStreams() throws Exception {
		ClaudeCodeHarness harness = harness(null);
		AgentProtocol protocol = harness.newProtocol();
		String sessionId;
		try (Recorder recorder = new Recorder()) {
			String token = _tokens.issue(_workspaceId, Duration.ofHours(1));
			ExecProcess process =
				_provider.startExec(_workspaceId, harness.command(new AgentLaunch(APP_DIR, null, token)), recorder);
			try {
				recorder.turn(process, Fixtures.OK, protocol.userMessage(PROMPT_OK));
				sessionId = recorder.sessionId();
				recorder.turn(process, Fixtures.TOOL, protocol.userMessage(PROMPT_TOOL));

				recorder.startPhase(Fixtures.INTERRUPT);
				write(process, protocol.userMessage(PROMPT_LONG));
				recorder.await(TEXT_DELTA);
				write(process, protocol.interrupt());
				recorder.await(RESULT_LINE);
				recorder.endPhase();

				process.closeStdin();
				assertTrue(process.waitFor(WAIT));
			} finally {
				process.close();
			}
		}
		try (Recorder recorder = new Recorder()) {
			String token = _tokens.issue(_workspaceId, Duration.ofHours(1));
			ExecProcess process =
				_provider.startExec(_workspaceId, harness.command(new AgentLaunch(APP_DIR, sessionId, token)),
					recorder);
			try {
				recorder.turn(process, Fixtures.RESUME,
					protocol.userMessage("Welche Frage habe ich dir zuerst gestellt? Antworte kurz."));
				assertEquals("The conversation is continued.", sessionId, recorder.sessionId());
				process.closeStdin();
				assertTrue(process.waitFor(WAIT));
			} finally {
				process.close();
			}
		}
		try (Recorder recorder = new Recorder()) {
			// A well-formed token the proxy never issued.
			String refused = LLMProxyService.TOKEN_PREFIX + UUID.randomUUID();
			ExecProcess process =
				_provider.startExec(_workspaceId, harness.command(new AgentLaunch(APP_DIR, null, refused)), recorder);
			try {
				recorder.turn(process, Fixtures.LOGIN_FAILED, protocol.userMessage(PROMPT_OK));
				process.closeStdin();
				assertTrue(process.waitFor(WAIT));
			} finally {
				process.close();
			}
		}
	}

	/**
	 * A conversation through {@link WorkspaceAgent}: an answer, the same conversation continued by
	 * a new process, and an answer stopped by the user.
	 */
	public void testBackend() throws Exception {
		ScheduledExecutorService scheduler =
			Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory.daemonThreadFactory());
		ExecutorService watchers = Executors.newCachedThreadPool(DaemonThreadFactory.daemonThreadFactory());
		try {
			AgentRuntime runtime = new AgentRuntime(harness(BACKEND_MODEL), _provider, _tokens,
				Duration.ofMinutes(10), Duration.ofHours(1), Duration.ofSeconds(30), Duration.ofSeconds(10),
				scheduler, watchers, Clock.systemUTC());
			MemoryConversation conversation = new MemoryConversation(_workspaceId, APP_DIR);
			WorkspaceAgent agent = new WorkspaceAgent(runtime, conversation);

			EventLog first = new EventLog();
			String answer = agent.send(turn(PROMPT_OK), first, new CancellationToken());
			assertEquals(StopReason.COMPLETE, first.reason());
			assertTrue("Answer: " + answer, answer.contains("OK"));
			assertEquals(answer, first.text());
			String sessionId = conversation.sessionId();
			assertNotNull("The session is stored.", sessionId);
			assertTrue("Cost: " + first.completion().totalCost(), first.completion().totalCost().amount() > 0);
			assertEquals(1, _tokens.active());

			agent.stop();
			assertFalse(agent.hasProcess());
			assertEquals("The token of the stopped process is revoked.", 0, _tokens.active());

			EventLog second = new EventLog();
			answer = agent.send(turn("Mit welchem Wort hast du eben geantwortet? Nur dieses Wort."), second,
				new CancellationToken());
			assertEquals(StopReason.COMPLETE, second.reason());
			assertTrue("The new process knows the conversation: " + answer, answer.contains("OK"));
			assertEquals(sessionId, conversation.sessionId());
			assertEquals(2, _tokens.issued().size());

			CancellationToken cancel = new CancellationToken();
			EventLog third = new EventLog().observe(event -> {
				if (event instanceof AgentEvent.TextDelta && !cancel.isCancelled()) {
					// A stop arrives on the thread of the stop request.
					new Thread(cancel::cancel).start();
				}
			});
			agent.send(turn(PROMPT_LONG), third, cancel);
			assertEquals(StopReason.ABORTED, third.reason());
			assertTrue("The process survives a stop.", agent.hasProcess());

			agent.stop();
			assertEquals(0, _tokens.active());
		} finally {
			scheduler.shutdownNow();
			watchers.shutdownNow();
		}
	}

	private static BackendTurn turn(String text) {
		return new BackendTurn("it", null, null, text, null);
	}

	private static ClaudeCodeHarness harness(String model) {
		ClaudeCodeHarness.Config<?> config = TypedConfiguration.newConfigItem(ClaudeCodeHarness.Config.class);
		if (model != null) {
			TypedConfigUtil.setProperty(config, ClaudeCodeHarness.Config.MODEL, model);
		}
		return (ClaudeCodeHarness) TypedConfigUtil.createInstance(config);
	}

	private static void write(ExecProcess process, String input) throws IOException {
		process.write(input);
		process.flush();
	}

	/**
	 * Collects the output of a process and writes it per phase.
	 */
	private final class Recorder implements ExecOutput, AutoCloseable {

		private final StringBuilder _all = new StringBuilder();

		private final StringBuilder _stderr = new StringBuilder();

		private int _phaseStart;

		private String _phase;

		@Override
		public synchronized void stdout(String text) {
			_all.append(text);
			notifyAll();
		}

		@Override
		public synchronized void stderr(String text) {
			_stderr.append(text);
		}

		void turn(ExecProcess process, String phase, String input) throws Exception {
			startPhase(phase);
			write(process, input);
			await(RESULT_LINE);
			endPhase();
		}

		synchronized void startPhase(String phase) {
			_phase = phase;
			_phaseStart = _all.length();
		}

		/**
		 * Waits for the complete line holding the given text.
		 */
		synchronized void await(String text) throws InterruptedException {
			long deadline = System.currentTimeMillis() + WAIT.toMillis();
			while (true) {
				int found = _all.indexOf(text, _phaseStart);
				if (found >= 0 && _all.indexOf("\n", found) >= 0) {
					return;
				}
				long left = deadline - System.currentTimeMillis();
				assertTrue("No '" + text + "' in phase " + _phase + ": " + _all.substring(_phaseStart) + _stderr,
					left > 0);
				wait(left);
			}
		}

		synchronized String sessionId() {
			String field = "\"session_id\":\"";
			int start = _all.indexOf(field, _phaseStart) + field.length();
			return _all.substring(start, _all.indexOf("\"", start));
		}

		synchronized void endPhase() throws IOException {
			String content = _all.substring(_phaseStart);
			for (String token : _tokens.issued()) {
				assertFalse("Token in the output.", content.contains(token));
			}
			String dir = System.getProperty(RECORD_PROPERTY);
			if (dir != null) {
				Path path = Path.of(dir).resolve(_phase);
				Files.createDirectories(path.getParent());
				Files.writeString(path, content, StandardCharsets.UTF_8);
			}
		}

		@Override
		public synchronized void close() {
			if (_stderr.length() > 0) {
				System.err.println("Claude Code wrote to its error output: " + _stderr);
			}
		}
	}

	/**
	 * The API key from {@value #API_KEY_ENV}, without the marker of an unencrypted value the
	 * configuration of the control application accepts.
	 */
	private static String apiKey() {
		String key = System.getenv(API_KEY_ENV);
		return key.startsWith(UNENCRYPTED_PREFIX) ? key.substring(UNENCRYPTED_PREFIX.length()) : key;
	}

	private static String dockerHost() {
		String host = System.getenv("DOCKER_HOST");
		return host == null || host.isEmpty() ? DEFAULT_DOCKER_HOST : host;
	}

	private static DockerWorkspaceProvider createProvider() {
		DockerWorkspaceProvider.Config<?> config = TypedConfiguration.newConfigItem(DockerWorkspaceProvider.Config.class);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.DOCKER_HOST, dockerHost());
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.NETWORK, NAME_PREFIX);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.NAME_PREFIX, NAME_PREFIX);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.LABEL_NAMESPACE, LABEL_NAMESPACE);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.EXTRA_HOSTS,
			List.of(GATEWAY_HOST + ":host-gateway"));
		return (DockerWorkspaceProvider) TypedConfigUtil.createInstance(config);
	}

	/**
	 * The suite, empty unless enabled.
	 */
	public static Test suite() {
		String name = TestClaudeCodeIntegration.class.getName();
		if (!Boolean.getBoolean(ENABLE_PROPERTY) || System.getenv(API_KEY_ENV) == null) {
			System.err.println("Skipping " + name + ": enable it with -D" + ENABLE_PROPERTY + "=true and "
				+ API_KEY_ENV + " in the environment.");
			return new TestSuite(name);
		}
		return ServiceTestSetup.createSetup(TestClaudeCodeIntegration.class, TypeIndex.Module.INSTANCE);
	}

}
