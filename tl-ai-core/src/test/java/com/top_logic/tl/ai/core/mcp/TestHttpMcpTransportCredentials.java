/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
import com.top_logic.basic.io.character.CharacterContents;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests how {@link HttpMcpTransport} authenticates its requests with the configured
 * <code>credentials</code>, and repeats a rejected request once with fresh ones.
 *
 * <p>
 * A local HTTP server plays the MCP server: it answers <code>initialize</code> and accepts
 * notifications, if the request carries the expected <code>Authorization</code> header.
 * </p>
 */
public class TestHttpMcpTransportCredentials extends TestCase {

	/** The system property holding the token of {@link #testEnvironmentToken()}. */
	private static final String VARIABLE = "test_http_mcp_transport_token";

	private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\"[^\"]*\"|\\d+)");

	private static final Pattern PROTOCOL_VERSION = Pattern.compile("\"protocolVersion\"\\s*:\\s*\"([^\"]*)\"");

	private HttpServer _server;

	/** The <code>Authorization</code> headers of the received POST requests. */
	private final List<String> _authorizations = new ArrayList<>();

	/** Decides from the <code>Authorization</code> header whether a request is accepted. */
	private Predicate<String> _accepted = authorization -> true;

	/**
	 * Parsing the transport configuration resolves the nested configuration types of the
	 * credentials through the <code>TypeIndex</code>, which needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestHttpMcpTransportCredentials.class)));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		RotatingCredentials.INVALIDATIONS.set(0);
		_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_server.createContext("/", this::handle);
		_server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		_server.stop(0);
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	/** A bearer token from a variable is sent with each request. */
	public void testEnvironmentToken() throws Exception {
		System.setProperty(VARIABLE, "mcp-token");
		_accepted = "Bearer mcp-token"::equals;

		initialize(transport(
			"<credentials class='" + HeaderCredentials.class.getName() + "' scheme='Bearer'>"
				+ "<secret class='" + EnvironmentSecretSource.class.getName() + "' name='" + VARIABLE + "'/>"
				+ "</credentials>"));

		assertFalse(_authorizations.isEmpty());
		for (String authorization : _authorizations) {
			assertEquals("Bearer mcp-token", authorization);
		}
	}

	/** Without credentials, the requests carry none. */
	public void testNoCredentials() throws Exception {
		initialize(transport(""));

		assertFalse(_authorizations.isEmpty());
		for (String authorization : _authorizations) {
			assertNull(authorization);
		}
	}

	/** A rejected request is repeated once with fresh credentials. */
	public void testRetryWithFreshCredentials() throws Exception {
		_accepted = authorization -> !"Bearer token-0".equals(authorization);

		initialize(transport("<credentials class='" + RotatingCredentials.class.getName() + "'/>"));

		assertEquals(1, RotatingCredentials.INVALIDATIONS.get());
		assertEquals("Bearer token-0", _authorizations.get(0));
		assertEquals("Bearer token-1", _authorizations.get(1));
	}

	/** Credentials rejected again are not tried a third time. */
	public void testRetryOnlyOnce() throws Exception {
		_accepted = authorization -> false;

		try {
			initialize(transport("<credentials class='" + RotatingCredentials.class.getName() + "'/>"));
			fail("A rejected initialization must fail.");
		} catch (RuntimeException expected) {
			// The server refuses the client.
		}

		assertEquals(2, _authorizations.size());
		assertEquals(1, RotatingCredentials.INVALIDATIONS.get());
	}

	private void initialize(HttpMcpTransport transport) {
		McpSyncClient client = McpClient.sync(transport.createTransport())
			.requestTimeout(Duration.ofSeconds(10))
			.clientInfo(new McpSchema.Implementation("test", "1.0.0"))
			.build();
		try {
			client.initialize();
		} finally {
			client.close();
		}
	}

	private HttpMcpTransport transport(String content) throws ConfigurationException {
		String xml = "<transport xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + HttpMcpTransport.Config.class.getName() + "'"
			+ " class='" + HttpMcpTransport.class.getName() + "'"
			+ " url='http://127.0.0.1:" + _server.getAddress().getPort() + "/mcp'>"
			+ content + "</transport>";
		HttpMcpTransport.Config config =
			(HttpMcpTransport.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private void handle(HttpExchange exchange) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		if (!"POST".equals(exchange.getRequestMethod())) {
			reply(exchange, HttpURLConnection.HTTP_BAD_METHOD, null);
			return;
		}
		String authorization = exchange.getRequestHeaders().getFirst(CredentialProvider.AUTHORIZATION);
		boolean accepted;
		synchronized (_authorizations) {
			_authorizations.add(authorization);
			accepted = _accepted.test(authorization);
		}
		if (!accepted) {
			reply(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, null);
			return;
		}
		Matcher id = ID.matcher(body);
		if (!body.contains("\"initialize\"") || !id.find()) {
			reply(exchange, HttpURLConnection.HTTP_ACCEPTED, null);
			return;
		}
		Matcher version = PROTOCOL_VERSION.matcher(body);
		String protocolVersion = version.find() ? version.group(1) : "2025-06-18";
		reply(exchange, HttpURLConnection.HTTP_OK, "{\"jsonrpc\":\"2.0\",\"id\":" + id.group(1)
			+ ",\"result\":{\"protocolVersion\":\"" + protocolVersion + "\",\"capabilities\":{},"
			+ "\"serverInfo\":{\"name\":\"mock\",\"version\":\"1.0.0\"}}}");
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

	/**
	 * Credentials sending a bearer token that is replaced by the next one on each invalidation.
	 */
	public static class RotatingCredentials extends AbstractConfiguredInstance<RotatingCredentials.Config>
			implements CredentialProvider {

		/** The number of invalidations since the start of the test. */
		static final AtomicInteger INVALIDATIONS = new AtomicInteger();

		/**
		 * Configuration options for {@link RotatingCredentials}.
		 */
		public interface Config extends PolymorphicConfiguration<RotatingCredentials> {
			// No options.
		}

		/**
		 * Creates a {@link RotatingCredentials} from configuration.
		 */
		public RotatingCredentials(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public void authenticate(RequestAuthentication request) {
			request.setHeader(CredentialProvider.AUTHORIZATION,
				CredentialProvider.BEARER + " token-" + INVALIDATIONS.get());
		}

		@Override
		public void invalidate() {
			INVALIDATIONS.incrementAndGet();
		}

	}

}
