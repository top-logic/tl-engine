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

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests that {@link HttpMcpTransport} sends its requests to the path and query of the configured
 * <code>url</code>.
 *
 * <p>
 * A local HTTP server plays the MCP server: it answers MCP only at the path and query of the
 * configured <code>url</code> and answers 404 at each other request target. The client connects through
 * {@link McpClientConnection}, as {@link McpServerRegistry} does.
 * </p>
 */
public class TestHttpMcpTransportEndpoint extends TestCase {

	private static final String TOOL = "echo";

	private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\"[^\"]*\"|\\d+)");

	private static final Pattern METHOD = Pattern.compile("\"method\"\\s*:\\s*\"([^\"]*)\"");

	private static final Pattern PROTOCOL_VERSION = Pattern.compile("\"protocolVersion\"\\s*:\\s*\"([^\"]*)\"");

	private HttpServer _server;

	/** The request target (path and query) at which the server answers MCP. */
	private String _target;

	/** The request targets of all received requests. */
	private final List<String> _targets = new CopyOnWriteArrayList<>();

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

	/** A server below a path other than <code>/mcp</code>, e.g. an application with a context path. */
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

	/**
	 * Connects with a url that ends with the given part, lists the tools and calls one.
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
			connection.connect();
			assertEquals(1, connection.tools().size());
			McpSchema.CallToolResult result = connection.callTool(TOOL, Collections.emptyMap());
			assertEquals("called", ((McpSchema.TextContent) result.content().get(0)).text());
		} catch (RuntimeException ex) {
			fail("No connection to " + target + ", the requests went to " + _targets + ": " + ex);
		} finally {
			connection.close();
		}

		for (String requestTarget : _targets) {
			assertEquals(target, requestTarget);
		}
		assertTrue("Methods at " + target + ": " + _methods,
			_methods.containsAll(List.of("initialize", "tools/list", "tools/call")));
	}

	private HttpMcpTransport transport(String urlEnd) throws ConfigurationException {
		String xml = "<transport xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + HttpMcpTransport.Config.class.getName() + "'"
			+ " class='" + HttpMcpTransport.class.getName() + "'"
			+ " url='http://127.0.0.1:" + _server.getAddress().getPort() + urlEnd + "'/>";
		HttpMcpTransport.Config config =
			(HttpMcpTransport.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private void handle(HttpExchange exchange) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		String target = exchange.getRequestURI().toString();
		_targets.add(target);
		if (!target.equals(_target)) {
			reply(exchange, HttpURLConnection.HTTP_NOT_FOUND, null);
			return;
		}
		if (!"POST".equals(exchange.getRequestMethod())) {
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
