/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import junit.framework.TestCase;

import org.junit.Assume;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import com.top_logic.ai.a2a.A2AService;
import com.top_logic.ai.a2a.A2AService.RemoteAgentConfig;
import com.top_logic.ai.a2a.ContextBook;
import com.top_logic.ai.a2a.client.RemoteAgents.RemoteAgent;
import com.top_logic.ai.a2a.protocol.A2AError;
import com.top_logic.ai.a2a.protocol.ArtifactRef;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.ai.a2a.protocol.TaskState;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Tests that the {@link A2AClient} sends the calls to a remote agent, with its credentials and the
 * messages, only to the origin (scheme, host and port) of its card URL and to the origins of its
 * {@link RemoteAgentConfig#getEndpointOrigins() endpoint origins}, whatever addresses the card
 * names.
 *
 * <p>
 * A card host serves a card that names the JSON-RPC address and the artifact address of an agent
 * server at another origin, as a card that a third party can change does. The agent server records
 * each request and answers with fixed results, so that each tool goes through all of its calls.
 * </p>
 */
public class TestRemoteAgentOrigin extends TestCase {

	private static final String TOKEN = "token-of-alpha";

	/** The password of the key store of the HTTPS card host, generated for one test. */
	private static final String STORE_PASSWORD = "test-store";

	/** What the agent server got: the call and its {@code Authorization} header. */
	private final List<String> _received = new CopyOnWriteArrayList<>();

	/** The bodies of the JSON-RPC calls that the agent server got. */
	private final List<String> _bodies = new CopyOnWriteArrayList<>();

	private final AtomicInteger _cardReads = new AtomicInteger();

	private HttpServer _agentServer;

	private HttpServer _cardHost;

	/** The origin of the agent server. */
	private String _agentOrigin;

	/** The JSON-RPC address on the card, at the agent server. */
	private String _endpoint;

	/** The artifact address on the card, at the agent server. */
	private String _artifacts;

	private String _cardUrl;

	private RemoteAgents _agents;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_agentServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_agentServer.createContext("/", this::serveAgent);
		_agentServer.start();
		_agentOrigin = "http://127.0.0.1:" + _agentServer.getAddress().getPort();
		_endpoint = _agentOrigin + "/a2a/alpha";
		_artifacts = _agentOrigin + "/a2a-artifacts";

		_cardHost = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_cardHost.createContext("/", this::serveCard);
		_cardHost.start();
		_cardUrl = "http://localhost:" + _cardHost.getAddress().getPort() + "/card.json";
		_agents = agents(_cardUrl);
	}

	@Override
	protected void tearDown() throws Exception {
		_cardHost.stop(0);
		_agentServer.stop(0);
		super.tearDown();
	}

	/** askAgent: {@code SendStreamingMessage}, then {@code GetTask}. */
	public void testAskAgent() throws Exception {
		assertNothingSent(_endpoint, () -> ask(_agents));
	}

	/** replyAgent: {@code SendStreamingMessage} to a known task, then {@code GetTask}. */
	public void testReplyAgent() throws Exception {
		assertNothingSent(_endpoint, () -> exchange(_agents, () -> false).reply("alpha", "task-1", "Site north"));
	}

	/** getAgentTask: {@code GetTask}, {@code SubscribeToTask} while the task works, {@code GetTask}. */
	public void testGetAgentTask() throws Exception {
		assertNothingSent(_endpoint, () -> exchange(_agents, () -> false).follow("alpha", "task-1", true));
	}

	/** A cancelled askAgent: {@code SendStreamingMessage}, then {@code CancelTask}. */
	public void testCancelTask() throws Exception {
		assertNothingSent(_endpoint,
			() -> exchange(_agents, () -> true).ask("alpha", "The plan of project 7", null, null, null, List.of()));
	}

	/** fetchArtifact: the {@code GET} of the artifact address. */
	public void testFetchArtifact() throws Exception {
		assertNothingSent(_artifacts, () -> fetch(_agents));
	}

	/**
	 * A card read over HTTPS that names an address with {@code http}: the token would go out with no
	 * encryption. The https origin of the same host and port in the list does not allow it.
	 */
	public void testHttpsCardNamesHttpAddress() throws Exception {
		SSLContext tls = selfSignedTls();
		HttpsServer httpsCardHost = httpsCardHost(tls);
		try {
			_cardUrl = "https://127.0.0.1:" + httpsCardHost.getAddress().getPort() + "/card.json";
			RemoteAgents agents =
				agentsTrusting(tls, _cardUrl, "https://127.0.0.1:" + _agentServer.getAddress().getPort());
			assertNothingSent(_endpoint, () -> ask(agents));
		} finally {
			httpsCardHost.stop(0);
		}
	}

	/** A card read over HTTPS may move the calls to http only to an http origin of the list. */
	public void testHttpsCardWithListedHttpOrigin() throws Exception {
		SSLContext tls = selfSignedTls();
		HttpsServer httpsCardHost = httpsCardHost(tls);
		try {
			_cardUrl = "https://127.0.0.1:" + httpsCardHost.getAddress().getPort() + "/card.json";
			ask(agentsTrusting(tls, _cardUrl, _agentOrigin));
			assertEquals(List.of("SendStreamingMessage with Bearer " + TOKEN, "GetTask with Bearer " + TOKEN),
				_received);
		} finally {
			httpsCardHost.stop(0);
		}
	}

	/** An origin in the endpoint origins gets each call with the credentials. */
	public void testListedOriginGetsTheCalls() throws Exception {
		RemoteAgents agents = agents(_cardUrl, _agentOrigin);
		ask(agents);
		fetch(agents);
		assertEquals(List.of("SendStreamingMessage with Bearer " + TOKEN, "GetTask with Bearer " + TOKEN,
			"GET /a2a-artifacts with Bearer " + TOKEN), _received);
	}

	/** Only the exact origin counts: the same port with another host or scheme is no listed origin. */
	public void testUnlistedOriginGetsNoRequest() throws Exception {
		int port = _agentServer.getAddress().getPort();
		RemoteAgents agents = agents(_cardUrl, "http://localhost:" + port, "https://127.0.0.1:" + port);
		assertNothingSent(_endpoint, () -> ask(agents));
		assertNothingSent(_artifacts, () -> fetch(agents));
	}

	/** Case, a trailing dot and a default port do not change an origin. */
	public void testSameOriginInAnotherSpelling() {
		RemoteAgent agent = new RemoteAgent("alpha", "https://Agents.Example.COM./a2a/alpha/agent-card.json",
			List.of("HTTPS://other.example.com:443"), A2AClient.NO_CREDENTIALS);
		agent.checkOrigin("https://agents.example.com:443/a2a/alpha");
		agent.checkOrigin("https://other.example.com/a2a/alpha");
		for (String foreign : List.of("http://agents.example.com/a2a/alpha", "https://agents.example.com:8443/a2a/alpha",
			"https://sub.agents.example.com/a2a/alpha", "/a2a/alpha", "no url")) {
			try {
				agent.checkOrigin(foreign);
				fail("Allowed: " + foreign);
			} catch (A2AError ex) {
				assertTrue(ex.getMessage(), ex.getMessage().contains(foreign));
			}
		}
	}

	/** An entry of the endpoint origins that is no origin stops the start; a valid one does not. */
	public void testInvalidEndpointOriginIsAnErrorAtTheStart() throws Exception {
		List<String> invalid = List.of("bad.example.com", "https://bad.example.com/", "https://bad.example.com/a2a",
			"https://bad.example.com?a=1", "https://bad.example.com#top", "https://user@bad.example.com");
		String xml = "<remote-agent xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + RemoteAgentConfig.class.getName() + "'"
			+ " name='alpha' card-url='https://agents.example.com/a2a/alpha/agent-card.json'"
			+ " endpoint-origins='https://other.example.com:8443, " + String.join(", ", invalid) + "'/>";
		RemoteAgentConfig config = (RemoteAgentConfig) TypedConfiguration.parse(CharacterContents.newContent(xml));
		BufferingProtocol log = new BufferingProtocol();

		RemoteAgents agents = new RemoteAgents(new SimpleInstantiationContext(log), List.of(config));

		assertEquals(log.getErrors().toString(), invalid.size(), log.getErrors().size());
		for (int n = 0; n < invalid.size(); n++) {
			String error = log.getErrors().get(n);
			assertTrue(error, error.contains("'" + invalid.get(n) + "'") && error.contains("'alpha'"));
		}
		agents.get("alpha").checkOrigin("https://other.example.com:8443/a2a/alpha");
		try {
			agents.get("alpha").checkOrigin("https://bad.example.com/a2a/alpha");
			fail("An origin of an invalid entry is allowed.");
		} catch (A2AError ex) {
			// As expected.
		}
	}

	/** Which addresses give the start warning: http to a host that is not a loopback host. */
	public void testUnencryptedAddresses() {
		for (String local : List.of("http://localhost:8081/a2a", "http://LOCALHOST.:8081", "http://127.0.0.1:8080",
			"http://127.1.2.3", "http://[::1]:8080", "https://agents.example.com/a2a", "no url")) {
			assertFalse(local, RemoteAgents.isUnencrypted(local));
		}
		for (String remote : List.of("http://agents.example.com/a2a", "HTTP://10.0.0.5:8080", "http://[::2]",
			"http://127.example.com", "http://localhost.example.com")) {
			assertTrue(remote, RemoteAgents.isUnencrypted(remote));
		}
	}

	/**
	 * Runs a call and checks that no request reached the agent server and that the call ended with
	 * an error that names the address of the card and the card URL.
	 */
	private void assertNothingSent(String address, Call call) throws Exception {
		A2AError refusal = null;
		try {
			call.run();
		} catch (A2AError ex) {
			refusal = ex;
		}
		assertTrue("The card was read.", _cardReads.get() > 0);
		assertEquals("The server at another origin than " + _cardUrl + " gets no request. It got the bodies "
			+ _bodies + ".", List.of(), _received);
		assertNotNull("The call ends with an error.", refusal);
		assertTrue("The error names both addresses: " + refusal.getMessage(),
			refusal.getMessage().contains(address) && refusal.getMessage().contains(_cardUrl));
	}

	/**
	 * A call of the client.
	 */
	private interface Call {
		void run() throws IOException, InterruptedException;
	}

	private static void ask(RemoteAgents agents) throws IOException, InterruptedException {
		exchange(agents, () -> false).ask("alpha", "The plan of project 7", null, null, null, List.of());
	}

	private static void fetch(RemoteAgents agents) throws IOException, InterruptedException {
		agents.fetchArtifact(ArtifactRef.parse("tl-artifact://alpha-app/spec/1"), null);
	}

	private static RemoteAgents agents(String cardUrl, String... endpointOrigins) {
		RemoteAgents agents = new RemoteAgents(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, List.of());
		agents.add(new RemoteAgent("alpha", cardUrl, List.of(endpointOrigins),
			request -> request.setHeader(CredentialProvider.AUTHORIZATION, CredentialProvider.BEARER + " " + TOKEN)));
		return agents;
	}

	/**
	 * Creates the remote agents while the given TLS context is the default, because the HTTP client
	 * of {@link A2AClient} takes the default when it is created. So it trusts the certificate of the
	 * test.
	 *
	 * <p>
	 * This is safe for the other tests of the JVM: the default changes only while
	 * {@link RemoteAgents} builds its client, and the finally block sets the old default again. A
	 * client built later, also of another test, does not trust the test certificate.
	 * </p>
	 */
	private static RemoteAgents agentsTrusting(SSLContext tls, String cardUrl, String... endpointOrigins)
			throws Exception {
		SSLContext before = SSLContext.getDefault();
		SSLContext.setDefault(tls);
		try {
			return agents(cardUrl, endpointOrigins);
		} finally {
			SSLContext.setDefault(before);
		}
	}

	private static RemoteExchange exchange(RemoteAgents agents, BooleanSupplier cancelled) {
		RemoteExchange.Caller caller = new RemoteExchange.Caller("beta", "Beta", "conv-1", null, "Project 7", "run-1");
		return new RemoteExchange(agents, new ContextBook(), caller, 5_000, line -> {
			// ignored
		}, cancelled, null);
	}

	private HttpsServer httpsCardHost(SSLContext tls) throws IOException {
		HttpsServer server = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.setHttpsConfigurator(new HttpsConfigurator(tls));
		server.createContext("/", this::serveCard);
		server.start();
		return server;
	}

	/**
	 * A TLS context with a key and a certificate for {@code 127.0.0.1}, which it also trusts. The JDK
	 * has no API that creates a certificate, so {@code keytool} of the running JDK makes it.
	 */
	private static SSLContext selfSignedTls() throws Exception {
		Path keytool = Path.of(System.getProperty("java.home"), "bin", "keytool");
		Assume.assumeTrue("No keytool at " + keytool, Files.isExecutable(keytool));
		Path dir = Files.createTempDirectory("a2a-tls");
		Path store = dir.resolve("server.p12");
		try {
			Process process = new ProcessBuilder(keytool.toString(), "-genkeypair", "-alias", "server",
				"-keyalg", "EC", "-groupname", "secp256r1", "-dname", "CN=127.0.0.1", "-ext", "SAN=ip:127.0.0.1",
				"-validity", "1", "-storetype", "PKCS12", "-keystore", store.toString(), "-storepass", STORE_PASSWORD)
				.redirectErrorStream(true).start();
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			assertEquals("keytool: " + output, 0, process.waitFor());

			KeyStore keys = KeyStore.getInstance("PKCS12");
			try (InputStream in = Files.newInputStream(store)) {
				keys.load(in, STORE_PASSWORD.toCharArray());
			}
			KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagers.init(keys, STORE_PASSWORD.toCharArray());
			TrustManagerFactory trustManagers =
				TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagers.init(keys);
			SSLContext tls = SSLContext.getInstance("TLS");
			tls.init(keyManagers.getKeyManagers(), trustManagers.getTrustManagers(), null);
			return tls;
		} finally {
			Files.deleteIfExists(store);
			Files.delete(dir);
		}
	}

	private void serveCard(HttpExchange exchange) throws IOException {
		try {
			_cardReads.incrementAndGet();
			write(exchange, Json.map("name", "Alpha", "description", "Builds.",
				"supportedInterfaces", List.of(Json.map("url", _endpoint, "protocolBinding", "JSONRPC",
					"protocolVersion", "1.0")),
				"capabilities", Json.map("extensions", List.of(Json.map("uri", A2AService.ARTIFACTS_EXTENSION,
					"params", Json.map("app", "alpha-app", "url", _artifacts))))));
		} finally {
			exchange.close();
		}
	}

	/**
	 * Answers a JSON-RPC call with a task: working after a message and on a read, completed at the
	 * end of a subscription, cancelled on a cancel. Answers the artifact address with an artifact.
	 */
	private void serveAgent(HttpExchange exchange) throws IOException {
		try {
			String authorization = exchange.getRequestHeaders().getFirst(CredentialProvider.AUTHORIZATION);
			if ("GET".equals(exchange.getRequestMethod())) {
				_received.add("GET " + exchange.getRequestURI().getPath() + " with " + authorization);
				write(exchange, Json.map("ref", "tl-artifact://alpha-app/spec/1", "name", "Spec",
					"mediaType", "text/markdown", "content", "# Spec"));
				return;
			}
			String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
			String method = Json.str(Json.parse(body), "method");
			_received.add(method + " with " + authorization);
			_bodies.add(body);
			Object result = switch (method) {
				case "SendStreamingMessage" -> Json.map("task", task(TaskState.WORKING));
				case "SubscribeToTask" -> Json.map("task", task(TaskState.COMPLETED));
				case "CancelTask" -> task(TaskState.CANCELED);
				default -> task(TaskState.WORKING);
			};
			write(exchange, Json.map("jsonrpc", "2.0", "id", Json.str(Json.parse(body), "id"), "result", result));
		} finally {
			exchange.close();
		}
	}

	private static Map<String, Object> task(TaskState state) {
		return Json.map("id", "task-1", "contextId", "context-1", "status", Json.map("state", state.wireName()));
	}

	private static void write(HttpExchange exchange, Map<String, Object> answer) throws IOException {
		byte[] bytes = Json.write(answer).getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, bytes.length);
		exchange.getResponseBody().write(bytes);
	}
}
