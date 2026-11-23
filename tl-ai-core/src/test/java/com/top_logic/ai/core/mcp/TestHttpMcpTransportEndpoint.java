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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

import io.modelcontextprotocol.spec.HttpHeaders;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests that {@link HttpMcpTransport} sends its requests to the path and query of the configured
 * <code>url</code>, and refuses a <code>url</code> that it cannot send requests to.
 *
 * <p>
 * A local HTTP server plays the MCP server: it answers MCP only at the path and query of the
 * configured <code>url</code> and answers 404 at each other request target. The client connects
 * through {@link McpClientConnection}, as {@link McpServerRegistry} does.
 * </p>
 *
 * <p>
 * The fake server is not shared with the one of {@link TestHttpMcpTransportCredentials}, because
 * that one checks the credentials and this one answers the tool requests and the end of the
 * session.
 * </p>
 */
public class TestHttpMcpTransportEndpoint extends TestCase {

	private static final String TOOL = "echo";

	private static final String SESSION = "session-1";

	private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\"[^\"]*\"|\\d+)");

	private static final Pattern METHOD = Pattern.compile("\"method\"\\s*:\\s*\"([^\"]*)\"");

	private static final Pattern PROTOCOL_VERSION = Pattern.compile("\"protocolVersion\"\\s*:\\s*\"([^\"]*)\"");

	private HttpServer _server;

	/**
	 * The request target (path and query) at which the server answers MCP.
	 *
	 * <p>
	 * Read by the thread of the server.
	 * </p>
	 */
	private volatile String _target;

	/** The HTTP method and the request target of each received request, e.g. "POST /mcp". */
	private final List<String> _requests = new CopyOnWriteArrayList<>();

	/** The JSON-RPC methods that the server got at {@link #_target}. */
	private final List<String> _methods = new CopyOnWriteArrayList<>();

	/**
	 * Parsing the transport configuration needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestHttpMcpTransportEndpoint.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_server.createContext("/", this::handle);
		_server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		_server.stop(0);
		super.tearDown();
	}

	/**
	 * A server below a path other than <code>/mcp</code>, e.g. an application with a context path.
	 */
	public void testPathBelowContext() throws Exception {
		assertServed("/app/mcp", "/app/mcp");
	}

	/** A server at the path <code>/mcp</code>. */
	public void testPathMcp() throws Exception {
		assertServed("/mcp", "/mcp");
	}

	/** A url with a trailing slash. */
	public void testTrailingSlash() throws Exception {
		assertServed("/app/mcp/", "/app/mcp/");
	}

	/** A url without a path names the root, not a default endpoint of the client. */
	public void testNoPath() throws Exception {
		assertServed("", "/");
	}

	/** The query of the url is sent with each request. */
	public void testQuery() throws Exception {
		assertServed("/app/mcp?tenant=a", "/app/mcp?tenant=a");
	}

	/** A url without an http or https scheme or without a host is refused when it is configured. */
	public void testInvalidUrl() throws ConfigurationException {
		for (String url : List.of("localhost:8080/mcp", "http:///mcp", "/app/mcp", "ftp://host/mcp",
			"http://host/a b")) {
			try {
				new HttpMcpTransport(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config(url));
				fail("Accepted: " + url);
			} catch (ConfigurationError ex) {
				assertEquals(I18NConstants.ERROR_INVALID_URL__URL.fill(url), ex.getErrorKey());
			}
		}
	}

	/**
	 * Connects with a url that ends with the given part, lists the tools, calls one and ends the
	 * session.
	 *
	 * @param urlEnd
	 *        The part of the url after the host and port.
	 * @param target
	 *        The request target (path and query) that the requests must have.
	 */
	private void assertServed(String urlEnd, String target) throws ConfigurationException {
		_target = target;
		McpClientConnection connection = new McpClientConnection("test", transport(urlEnd), 10);
		try {
			try {
				connection.connect();
			} catch (RuntimeException ex) {
				fail("No connection to " + target + ", the requests went to " + _requests + ": " + ex);
			}
			assertEquals(1, connection.tools().size());
			McpSchema.CallToolResult result = connection.callTool(TOOL, Collections.emptyMap());
			assertEquals("called", ((McpSchema.TextContent) result.content().get(0)).text());
		} finally {
			connection.close();
		}

		for (String request : _requests) {
			assertEquals(request, target, request.substring(request.indexOf(' ') + 1));
		}
		assertTrue("Methods at " + target + ": " + _methods,
			_methods.containsAll(List.of("initialize", "tools/list", "tools/call")));
		// The GET of the event stream is sent asynchronously, so only the end of the session is
		// checked besides the POST requests.
		assertTrue("Requests: " + _requests, _requests.contains("DELETE " + target));
	}

	private HttpMcpTransport transport(String urlEnd) throws ConfigurationException {
		HttpMcpTransport.Config config = config("http://127.0.0.1:" + _server.getAddress().getPort() + urlEnd);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private static HttpMcpTransport.Config config(String url) throws ConfigurationException {
		String xml = "<transport xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + HttpMcpTransport.Config.class.getName() + "'"
			+ " class='" + HttpMcpTransport.class.getName() + "'"
			+ " url='" + url + "'/>";
		return (HttpMcpTransport.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
	}

	private void handle(HttpExchange exchange) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		String target = exchange.getRequestURI().toString();
		String httpMethod = exchange.getRequestMethod();
		_requests.add(httpMethod + " " + target);
		if (!target.equals(_target)) {
			reply(exchange, HttpURLConnection.HTTP_NOT_FOUND, null);
			return;
		}
		if ("DELETE".equals(httpMethod)) {
			reply(exchange, HttpURLConnection.HTTP_OK, null);
			return;
		}
		if (!"POST".equals(httpMethod)) {
			reply(exchange, HttpURLConnection.HTTP_BAD_METHOD, null);
			return;
		}
		Matcher method = METHOD.matcher(body);
		if (!method.find()) {
			reply(exchange, HttpURLConnection.HTTP_BAD_REQUEST, null);
			return;
		}
		_methods.add(method.group(1));
		Matcher id = ID.matcher(body);
		if (!id.find()) {
			// A notification.
			reply(exchange, HttpURLConnection.HTTP_ACCEPTED, null);
			return;
		}
		if ("initialize".equals(method.group(1))) {
			exchange.getResponseHeaders().set(HttpHeaders.MCP_SESSION_ID, SESSION);
		}
		reply(exchange, HttpURLConnection.HTTP_OK,
			"{\"jsonrpc\":\"2.0\",\"id\":" + id.group(1) + "," + result(method.group(1), body) + "}");
	}

	private static String result(String method, String body) {
		switch (method) {
			case "initialize":
				Matcher version = PROTOCOL_VERSION.matcher(body);
				String protocolVersion = version.find() ? version.group(1) : "2025-06-18";
				return "\"result\":{\"protocolVersion\":\"" + protocolVersion + "\","
					+ "\"capabilities\":{\"tools\":{}},\"serverInfo\":{\"name\":\"mock\",\"version\":\"1.0.0\"}}";
			case "tools/list":
				return "\"result\":{\"tools\":[{\"name\":\"" + TOOL + "\",\"inputSchema\":{\"type\":\"object\"}}]}";
			case "tools/call":
				return "\"result\":{\"content\":[{\"type\":\"text\",\"text\":\"called\"}]}";
			default:
				return "\"error\":{\"code\":-32601,\"message\":\"Method not found\"}";
		}
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

}
