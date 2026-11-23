/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.mcp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.top_logic.ai.core.agent.RunCancellationBinding;
import com.top_logic.ai.tool.api.CancellationToken;
import com.top_logic.ai.tool.api.ProgressReporter;
import com.top_logic.ai.tool.api.Tool;
import com.top_logic.ai.tool.api.ToolExecutionContext;
import com.top_logic.ai.tool.api.ToolResult;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.thread.CancelScope.BlockingCall;
import com.top_logic.basic.tools.CollectingLogListener;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests how the MCP client authenticates the requests of an {@link HttpMcpTransport} with the
 * configured <code>credentials</code>, and repeats a rejected call once with fresh ones.
 *
 * <p>
 * A local HTTP server plays the MCP server: it answers <code>initialize</code>,
 * <code>tools/list</code> and <code>tools/call</code> and accepts notifications, if the request
 * carries the expected <code>Authorization</code> header.
 * </p>
 */
public class TestHttpMcpTransportCredentials extends TestCase {

	/** The system property holding the token of {@link #ENVIRONMENT_TOKEN}. */
	private static final String VARIABLE = "test_http_mcp_transport_token";

	/** Credentials with a bearer token from {@link #VARIABLE}. */
	private static final String ENVIRONMENT_TOKEN =
		"<credentials class='" + HeaderCredentials.class.getName() + "' scheme='Bearer'>"
			+ "<secret class='" + EnvironmentSecretSource.class.getName() + "' name='" + VARIABLE + "'/>"
			+ "</credentials>";

	/** Credentials of {@link RecordingCredentials}. */
	private static final String RECORDING = "<credentials class='" + RecordingCredentials.class.getName() + "'/>";

	/** The name of the MCP server in the {@link McpServerRegistry} of a test. */
	private static final String SERVER = "test";

	private static final String SESSION_HEADER = "Mcp-Session-Id";

	private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\"[^\"]*\"|\\d+)");

	private static final Pattern METHOD = Pattern.compile("\"method\"\\s*:\\s*\"([^\"]*)\"");

	private static final Pattern PROTOCOL_VERSION = Pattern.compile("\"protocolVersion\"\\s*:\\s*\"([^\"]*)\"");

	private HttpServer _server;

	/** The <code>Authorization</code> headers of the received POST requests. */
	private final List<String> _authorizations = new ArrayList<>();

	/**
	 * The method of the first request with each <code>Authorization</code> header: the JSON-RPC
	 * method of a POST request, else the HTTP method.
	 */
	private final Map<String, String> _methods = new HashMap<>();

	/** Decides from the <code>Authorization</code> header whether a request is accepted. */
	private Predicate<String> _accepted = authorization -> true;

	/** The JSON-RPC methods whose first request the server rejects with HTTP 401. */
	private final Set<String> _rejectedOnce = new HashSet<>();

	/** The sessions that the server knows, <code>null</code> for a server without sessions. */
	private Set<String> _sessions;

	/** The number of sessions that the server started. */
	private int _sessionCount;

	/**
	 * When set, the server changes its tools with the next <code>tools/call</code> and runs this
	 * action, under the lock of {@link #_authorizations}.
	 */
	private Runnable _onToolChange;

	/** Whether the server offers its second tool. */
	private boolean _toolsChanged;

	/** The credentials that the server got before its tools changed, <code>null</code> before. */
	private Set<String> _expired;

	/** The number of POST requests with {@link #_expired} credentials. */
	private int _expiredRequests;

	/** The number of the next <code>tools/list</code> requests that fail with HTTP 500. */
	private int _failingToolLists;

	/**
	 * The server holds its answer to a <code>tools/list</code> after the change back until this
	 * latch opens.
	 */
	private CountDownLatch _changedToolListAnswered = new CountDownLatch(0);

	/** Opens when the server receives a <code>tools/call</code>. */
	private final CountDownLatch _toolCallReceived = new CountDownLatch(1);

	/** The server holds its answer to a <code>tools/call</code> back until this latch opens. */
	private CountDownLatch _toolCallAnswered = new CountDownLatch(0);

	/**
	 * Parsing the transport configuration resolves the nested configuration types of the
	 * credentials through the <code>TypeIndex</code>, which needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(
			ServiceTestSetup.withThreadContext(new TestSuite(TestHttpMcpTransportCredentials.class)), SecretStoreService.Module.INSTANCE));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		RecordingCredentials.CALLS.clear();
		_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_server.createContext("/", this::handle);
		_server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		_toolCallAnswered.countDown();
		_changedToolListAnswered.countDown();
		_server.stop(0);
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	/** A bearer token from a variable is sent with each request. */
	public void testEnvironmentToken() throws Exception {
		System.setProperty(VARIABLE, "mcp-token");
		_accepted = "Bearer mcp-token"::equals;

		assertTrue(connect(ENVIRONMENT_TOKEN));

		List<String> authorizations = authorizations();
		assertFalse(authorizations.isEmpty());
		for (String authorization : authorizations) {
			assertEquals("Bearer mcp-token", authorization);
		}
	}

	/** Without credentials, the requests carry none. */
	public void testNoCredentials() throws Exception {
		assertTrue(connect(""));

		List<String> authorizations = authorizations();
		assertFalse(authorizations.isEmpty());
		for (String authorization : authorizations) {
			assertNull(authorization);
		}
	}

	/** A rejected request is repeated once with fresh credentials. */
	public void testRetryWithFreshCredentials() throws Exception {
		_accepted = authorization -> !"Bearer call-0".equals(authorization);

		assertTrue(connect(RECORDING));

		assertEquals(1, invalidations(RecordingCredentials.CALLS));
		List<String> authorizations = authorizations();
		assertEquals("Bearer call-0", authorizations.get(0));
		assertEquals("Bearer call-1", authorizations.get(1));
	}

	/** Credentials rejected again are not tried a third time. */
	public void testRetryOnlyOnce() throws Exception {
		_accepted = authorization -> false;

		assertFalse("A rejected initialization must fail.", connect(RECORDING));

		assertEquals(2, authorizations().size());
		assertEquals(1, invalidations(RecordingCredentials.CALLS));
	}

	/**
	 * The credential provider runs on the thread that uses the MCP client, and never inside the
	 * wait that a stop of the run ends: for <code>initialize</code>, <code>tools/list</code>,
	 * <code>tools/call</code>, and for the repeat after a rejection.
	 *
	 * <p>
	 * A provider may read the knowledge base (a secret of the database), and it may take the
	 * person from the thread. An interrupt of the wait during a read of the database closes the
	 * database file, and a thread of the MCP library has no person. The connect runs in the run as
	 * well, as it does for a connection of a person.
	 * </p>
	 */
	public void testProviderRunsOnCallerThread() throws Exception {
		List<String> methods = List.of("initialize", "tools/list", "tools/call");
		_rejectedOnce.addAll(methods);

		McpServerRegistry registry = registry(RECORDING);
		WaitTrackingToken token = new WaitTrackingToken();
		ToolResult result;
		List<ProviderCall> calls;
		try (RunCancellationBinding.Binding run = RunCancellationBinding.bind(token)) {
			registry.startUp();
			try {
				result = tool(registry).execute(Map.of(), context(token));
			} finally {
				calls = List.copyOf(RecordingCredentials.CALLS);
				registry.shutDown();
			}
		}

		String report = report(calls);
		assertFalse(result.text() + " " + report, result.failed());
		for (String method : methods) {
			assertEquals("Rejected request and repeat of " + method + ". " + report, 2, authentications(calls, method));
		}
		assertEquals("One invalidation for each rejection. " + report, methods.size(), invalidations(calls));
		for (ProviderCall call : calls) {
			assertSame("Not on the thread of the caller. " + report, Thread.currentThread(), call.thread());
			assertFalse("Inside the cancellable wait. " + report, call.inWait());
		}
	}

	/** A stop of the run ends a tool call that waits for the answer of the server. */
	public void testStopEndsWaitingCall() throws Exception {
		_toolCallAnswered = new CountDownLatch(1);
		McpServerRegistry registry = registry("");
		registry.startUp();
		try {
			Tool tool = tool(registry);
			CancellationToken token = new CancellationToken();
			Thread stop = onToolCall(token::cancel);
			// As in the agent loop: the token of the run is bound to its thread and given to the tool.
			try (RunCancellationBinding.Binding run = RunCancellationBinding.bind(token)) {
				tool.execute(Map.of(), context(token));
				fail("A stop of the run must end the wait for the server.");
			} catch (CancellationException expected) {
				// The stop ended the wait.
			}
			stop.join();
			assertFalse("The interrupt of the stop stays inside the wait.", Thread.currentThread().isInterrupted());
		} finally {
			_toolCallAnswered.countDown();
			registry.shutDown();
		}
	}

	/**
	 * Another party than the run interrupts a tool call that waits for the server: the call ends
	 * with a failure, and the interrupt flag stays set for that party.
	 */
	public void testForeignInterruptEndsCall() throws Exception {
		_toolCallAnswered = new CountDownLatch(1);
		McpServerRegistry registry = registry("");
		registry.startUp();
		try {
			Tool tool = tool(registry);
			CancellationToken token = new CancellationToken();
			Thread interrupt = onToolCall(Thread.currentThread()::interrupt);
			ToolResult result;
			boolean interrupted;
			try (RunCancellationBinding.Binding run = RunCancellationBinding.bind(token)) {
				result = tool.execute(Map.of(), context(token));
			} finally {
				// Taken back here, so that the flag does not reach the rest of the test.
				interrupted = Thread.interrupted();
			}
			interrupt.join();
			assertTrue("The interrupt flag stays set.", interrupted);
			assertTrue(result.failed());
			assertEquals("Error: the call to " + tool.name() + " was interrupted.", result.text());
		} finally {
			_toolCallAnswered.countDown();
			registry.shutDown();
		}
	}

	/**
	 * A server that forgot the session (for example after a restart) answers 404. The library then
	 * starts a new session by itself, with no caller. Its requests carry the credential all the
	 * same, so that the next call works.
	 */
	public void testNewSessionKeepsCredential() throws Exception {
		System.setProperty(VARIABLE, "mcp-token");
		_accepted = "Bearer mcp-token"::equals;
		_sessions = new HashSet<>();

		McpServerRegistry registry = registry(ENVIRONMENT_TOKEN);
		registry.startUp();
		try {
			Tool tool = tool(registry);
			synchronized (_authorizations) {
				_sessions.clear();
			}
			try {
				tool.execute(Map.of(), context(CancellationToken.UNCANCELLABLE));
			} catch (RuntimeException expected) {
				// The call that meets the forgotten session fails, and the library starts a new one.
			}
			// The library starts the new session in the background. The registry offers the server
			// again when it is done, and never again when the new session is refused.
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
			while (!registry.isConnected(SERVER) && System.nanoTime() < deadline) {
				Thread.sleep(10);
			}
			assertTrue("The new session is not ready.", registry.isConnected(SERVER));
			ToolResult result = tool.execute(Map.of(), context(CancellationToken.UNCANCELLABLE));
			assertFalse(result.text(), result.failed());
		} finally {
			registry.shutDown();
		}

		synchronized (_authorizations) {
			assertEquals("The library started a new session.", 2, _sessionCount);
		}
		for (String authorization : authorizations()) {
			assertEquals("Each request carries the credential.", "Bearer mcp-token", authorization);
		}
	}

	/**
	 * The server says that its tools changed. From then on it rejects all credentials that it got
	 * before, and the first new tools/list once more (HTTP 401).
	 *
	 * <p>
	 * The library sends no tools/list by itself. A later caller gets the new list: its refresh is
	 * rejected, and the refresh of the next caller invalidates the credentials and asks with fresh
	 * ones. The credential provider runs only on the thread of the caller, never inside the
	 * cancellable wait.
	 * </p>
	 */
	public void testChangedToolsWithFreshCredentials() throws Exception {
		_onToolChange = () -> {
			_expired = new HashSet<>(_authorizations);
			_accepted = value -> !_expired.contains(value);
			_rejectedOnce.add("tools/list");
		};

		McpServerRegistry registry = registry(RECORDING);
		WaitTrackingToken token = new WaitTrackingToken();
		List<String> names;
		List<ProviderCall> calls;
		try (RunCancellationBinding.Binding run = RunCancellationBinding.bind(token)) {
			registry.startUp();
			try {
				ToolResult result = tool(registry).execute(Map.of(), context(token));
				assertFalse(result.text(), result.failed());

				// The refresh runs in the background: a later call gets the new list.
				names = changedToolNames(registry);
			} finally {
				calls = List.copyOf(RecordingCredentials.CALLS);
				registry.shutDown();
			}
		}

		String report = report(calls);
		assertEquals(report, List.of("mcp__test__echo", "mcp__test__second"), names);
		assertEquals("One invalidation for the rejected refresh. " + report, 1, invalidations(calls));
		for (ProviderCall call : calls) {
			assertSame("Not on the thread of the caller. " + report, Thread.currentThread(), call.thread());
			assertFalse("Inside the cancellable wait. " + report, call.inWait());
		}
		synchronized (_authorizations) {
			assertEquals("Requests with expired credentials after the change.", 0, _expiredRequests);
		}
	}

	/**
	 * A call of <code>getTools</code> after a change of the tools does not wait for the server: it
	 * gets the old list at once, and a later call gets the new list.
	 */
	public void testGetToolsDoesNotWait() throws Exception {
		_onToolChange = () -> {
			// Only the change of the tools.
		};
		_changedToolListAnswered = new CountDownLatch(1);

		McpServerRegistry registry = registry("");
		registry.startUp();
		try {
			ToolResult result = tool(registry).execute(Map.of(), context(CancellationToken.UNCANCELLABLE));
			assertFalse(result.text(), result.failed());

			// The server holds its answer back: a call that waited for it would get the new list.
			assertEquals(List.of("mcp__test__echo"), toolNames(registry));

			_changedToolListAnswered.countDown();
			assertEquals(List.of("mcp__test__echo", "mcp__test__second"), changedToolNames(registry));
		} finally {
			_changedToolListAnswered.countDown();
			registry.shutDown();
		}
	}

	/**
	 * Refreshes of the tool list that fail in a row give one warning, and a line at the level
	 * debug for each further failure. A server that cannot be reached would fail each refresh.
	 */
	public void testFailedRefreshesWarnOnce() throws Exception {
		_onToolChange = () -> _failingToolLists = 3;

		McpServerRegistry registry = registry("");
		registry.startUp();
		CollectingLogListener log = new CollectingLogListener(EnumSet.of(Level.WARN, Level.DEBUG), true);
		List<LogEntry> entries;
		List<String> names;
		try {
			ToolResult result = tool(registry).execute(Map.of(), context(CancellationToken.UNCANCELLABLE));
			assertFalse(result.text(), result.failed());
			names = changedToolNames(registry);
		} finally {
			entries = log.getLogEntries();
			log.deactivate();
			registry.shutDown();
		}

		assertEquals(List.of("mcp__test__echo", "mcp__test__second"), names);
		List<Level> failures = entries.stream()
			.filter(entry -> entry.getMessage() != null && entry.getMessage().contains("Failed to refresh the tools"))
			.map(LogEntry::getPriority)
			.toList();
		assertEquals("Three failed refreshes, then one that succeeded.",
			List.of(Level.WARN, Level.DEBUG, Level.DEBUG), failures);
	}

	/** The tool names of a later call, after the refresh that the change of the tools started. */
	private static List<String> changedToolNames(McpServerRegistry registry) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		List<String> names = toolNames(registry);
		while (names.size() < 2 && System.nanoTime() < deadline) {
			Thread.sleep(10);
			names = toolNames(registry);
		}
		return names;
	}

	private static List<String> toolNames(McpServerRegistry registry) {
		return registry.getTools(SERVER).stream().map(Tool::name).toList();
	}

	/** Whether a registry connects to the test server, as at the start of the service. */
	private boolean connect(String credentials) throws ConfigurationException {
		McpServerRegistry registry = registry(credentials);
		registry.startUp();
		try {
			return registry.isConnected(SERVER);
		} finally {
			registry.shutDown();
		}
	}

	/**
	 * A registry with one HTTP server, the local test server. It connects on
	 * {@link McpServerRegistry#startUp()}.
	 */
	private McpServerRegistry registry(String credentials) throws ConfigurationException {
		String xml = "<registry xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + McpServerRegistry.Config.class.getName() + "'"
			+ " class='" + McpServerRegistry.class.getName() + "'>"
			+ "<servers><server name='" + SERVER + "'>"
			+ "<transport class='" + HttpMcpTransport.class.getName() + "'"
			+ " url='http://127.0.0.1:" + _server.getAddress().getPort() + "/mcp'>"
			+ credentials + "</transport>"
			+ "</server></servers></registry>";
		McpServerRegistry.Config<?> config =
			(McpServerRegistry.Config<?>) TypedConfiguration.parse(CharacterContents.newContent(xml));
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private static Tool tool(McpServerRegistry registry) {
		List<Tool> tools = registry.getTools(SERVER);
		assertNotNull("The registry did not connect to the test server.", tools);
		assertEquals(1, tools.size());
		return tools.get(0);
	}

	private static ToolExecutionContext context(CancellationToken token) {
		return new ToolExecutionContext() {
			@Override
			public ProgressReporter progressReporter() {
				return ProgressReporter.NOOP;
			}

			@Override
			public CancellationToken cancellationToken() {
				return token;
			}
		};
	}

	/** Starts a thread that runs the given action as soon as the server receives a <code>tools/call</code>. */
	private Thread onToolCall(Runnable action) {
		Thread thread = new Thread(() -> {
			try {
				if (_toolCallReceived.await(10, TimeUnit.SECONDS)) {
					action.run();
				}
			} catch (InterruptedException ex) {
				// The test is over.
			}
		}, "on-tool-call");
		thread.start();
		return thread;
	}

	private static long invalidations(List<ProviderCall> calls) {
		return calls.stream().filter(call -> call.authorization() == null).count();
	}

	/** The <code>Authorization</code> headers of the received POST requests until now. */
	private List<String> authorizations() {
		synchronized (_authorizations) {
			return new ArrayList<>(_authorizations);
		}
	}

	private long authentications(List<ProviderCall> calls, String method) {
		return calls.stream()
			.filter(call -> call.authorization() != null && method.equals(method(call.authorization())))
			.count();
	}

	/** The method of the first request that carried the given <code>Authorization</code> header. */
	private String method(String authorization) {
		synchronized (_authorizations) {
			return _methods.get(authorization);
		}
	}

	private String report(List<ProviderCall> calls) {
		StringBuilder result = new StringBuilder("Calls of the credential provider:");
		for (ProviderCall call : calls) {
			result.append("\n  ")
				.append(call.authorization() == null ? "invalidate" : "authenticate for " + method(call.authorization()))
				.append(" on thread '").append(call.thread().getName()).append("'")
				.append(call.inWait() ? ", inside the cancellable wait" : "");
		}
		return result.toString();
	}

	private void handle(HttpExchange exchange) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		String authorization = exchange.getRequestHeaders().getFirst(CredentialProvider.AUTHORIZATION);
		if (!"POST".equals(exchange.getRequestMethod())) {
			// The stream of server messages (GET) and the end of a session (DELETE): not offered.
			synchronized (_authorizations) {
				_methods.putIfAbsent(authorization, exchange.getRequestMethod());
			}
			reply(exchange, HttpURLConnection.HTTP_BAD_METHOD, null);
			return;
		}
		Matcher methodMatch = METHOD.matcher(body);
		String method = methodMatch.find() ? methodMatch.group(1) : null;
		boolean initialize = "initialize".equals(method);
		String session = exchange.getRequestHeaders().getFirst(SESSION_HEADER);
		boolean accepted;
		boolean knownSession;
		synchronized (_authorizations) {
			_authorizations.add(authorization);
			_methods.putIfAbsent(authorization, method);
			if (_expired != null && _expired.contains(authorization)) {
				_expiredRequests++;
			}
			accepted = _accepted.test(authorization) && !_rejectedOnce.remove(method);
			knownSession = _sessions == null || initialize || _sessions.contains(session);
			if (accepted && _sessions != null && initialize) {
				session = "session-" + ++_sessionCount;
				_sessions.add(session);
				exchange.getResponseHeaders().set(SESSION_HEADER, session);
			}
		}
		if (!accepted) {
			reply(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, null);
			return;
		}
		if (!knownSession) {
			reply(exchange, HttpURLConnection.HTTP_NOT_FOUND, null);
			return;
		}
		Matcher id = ID.matcher(body);
		if (method == null || !id.find()) {
			reply(exchange, HttpURLConnection.HTTP_ACCEPTED, null);
			return;
		}
		switch (method) {
			case "initialize": {
				Matcher version = PROTOCOL_VERSION.matcher(body);
				String protocolVersion = version.find() ? version.group(1) : "2025-06-18";
				reply(exchange, HttpURLConnection.HTTP_OK, result(id.group(1),
					"{\"protocolVersion\":\"" + protocolVersion + "\",\"capabilities\":{\"tools\":{}},"
						+ "\"serverInfo\":{\"name\":\"mock\",\"version\":\"1.0.0\"}}"));
				return;
			}
			case "tools/list": {
				boolean changed;
				boolean failing;
				synchronized (_authorizations) {
					changed = _toolsChanged;
					failing = _failingToolLists > 0;
					if (failing) {
						_failingToolLists--;
					}
				}
				if (failing) {
					reply(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, null);
					return;
				}
				if (changed) {
					try {
						_changedToolListAnswered.await(10, TimeUnit.SECONDS);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}
				reply(exchange, HttpURLConnection.HTTP_OK, result(id.group(1),
					"{\"tools\":[" + tool("echo") + (changed ? "," + tool("second") : "") + "]}"));
				return;
			}
			case "tools/call": {
				_toolCallReceived.countDown();
				try {
					_toolCallAnswered.await(10, TimeUnit.SECONDS);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				String answer = result(id.group(1),
					"{\"content\":[{\"type\":\"text\",\"text\":\"done\"}],\"isError\":false}");
				boolean change;
				synchronized (_authorizations) {
					change = _onToolChange != null && !_toolsChanged;
					if (change) {
						_toolsChanged = true;
						_onToolChange.run();
					}
				}
				if (change) {
					replyStream(exchange,
						"{\"jsonrpc\":\"2.0\",\"method\":\"" + McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED + "\"}",
						answer);
				} else {
					reply(exchange, HttpURLConnection.HTTP_OK, answer);
				}
				return;
			}
			default:
				reply(exchange, HttpURLConnection.HTTP_ACCEPTED, null);
		}
	}

	private static String tool(String name) {
		return "{\"name\":\"" + name + "\",\"description\":\"Answers done.\","
			+ "\"inputSchema\":{\"type\":\"object\",\"properties\":{}}}";
	}

	private static String result(String id, String result) {
		return "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":" + result + "}";
	}

	private static void reply(HttpExchange exchange, int status, String json) throws IOException {
		if (json == null) {
			exchange.sendResponseHeaders(status, -1);
			exchange.close();
			return;
		}
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, bytes.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(bytes);
		}
	}

	/** Answers with a stream of server messages (Server-Sent Events). */
	private static void replyStream(HttpExchange exchange, String... messages) throws IOException {
		exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
		try (OutputStream out = exchange.getResponseBody()) {
			for (String message : messages) {
				out.write(("event: message\ndata: " + message + "\n\n").getBytes(StandardCharsets.UTF_8));
			}
		}
	}

	/**
	 * One call of {@link RecordingCredentials}.
	 *
	 * @param authorization
	 *        The header value that {@link CredentialProvider#authenticate(RequestAuthentication)}
	 *        set, <code>null</code> for a call of {@link CredentialProvider#invalidate()}.
	 * @param thread
	 *        The thread of the call.
	 * @param inWait
	 *        Whether the call ran inside the cancellable wait of a {@link WaitTrackingToken}.
	 */
	record ProviderCall(String authorization, Thread thread, boolean inWait) {
		// Pure data.
	}

	/**
	 * Credentials that record the thread of each call, and that send a new bearer token for each
	 * authentication, so that the server log tells which request an authentication was for.
	 */
	public static class RecordingCredentials extends AbstractConfiguredInstance<RecordingCredentials.Config>
			implements CredentialProvider {

		/** The calls since the start of the test. */
		static final List<ProviderCall> CALLS = new CopyOnWriteArrayList<>();

		/**
		 * Configuration options for {@link RecordingCredentials}.
		 */
		public interface Config extends PolymorphicConfiguration<RecordingCredentials> {
			// No options.
		}

		/**
		 * Creates a {@link RecordingCredentials} from configuration.
		 */
		public RecordingCredentials(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public void authenticate(RequestAuthentication request) {
			String authorization = CredentialProvider.BEARER + " call-" + (CALLS.size() - invalidations(CALLS));
			CALLS.add(new ProviderCall(authorization, Thread.currentThread(), WaitTrackingToken.isWaiting()));
			request.setHeader(CredentialProvider.AUTHORIZATION, authorization);
		}

		@Override
		public void invalidate() {
			CALLS.add(new ProviderCall(null, Thread.currentThread(), WaitTrackingToken.isWaiting()));
		}

	}

	/**
	 * A {@link CancellationToken} that knows which threads are inside its cancellable wait, where a
	 * stop of the run interrupts them.
	 */
	static class WaitTrackingToken extends CancellationToken {

		private static final Set<Thread> WAITING = ConcurrentHashMap.newKeySet();

		/** Whether the current thread is inside the cancellable wait of a {@link WaitTrackingToken}. */
		static boolean isWaiting() {
			return WAITING.contains(Thread.currentThread());
		}

		@Override
		public <T, E extends Exception> T interruptibly(BlockingCall<T, E> call) throws E, InterruptedException {
			Thread thread = Thread.currentThread();
			WAITING.add(thread);
			try {
				return super.interruptibly(call);
			} finally {
				WAITING.remove(thread);
			}
		}
	}

}
