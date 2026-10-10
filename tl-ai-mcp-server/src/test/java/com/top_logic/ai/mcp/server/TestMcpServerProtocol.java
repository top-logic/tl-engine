/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.mcp.server.tools.ExposedTool;
import com.top_logic.ai.mcp.server.tools.McpExposure;
import com.top_logic.ai.mcp.server.tools.McpToolAdapter;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.HttpHeaders;

/**
 * What an MCP client sees of a configured {@link MCPServerService}, spoken over the real transport
 * in the JSON-RPC a client sends: the published tools, the instructions and the {@code Origin} check.
 *
 * <p>
 * The transport and the server are built by the same {@link MCPServerService#createTransport} and
 * {@link MCPServerService#createServer} the service starts with; only the servlet container is
 * replaced by request and response doubles. Authentication is not part of this: it is the filter in
 * front of the transport, tested on its own.
 * </p>
 */
public class TestMcpServerProtocol extends TestCase {

	private static final String INSTRUCTIONS = "Use list_things to see what there is.";

	private static final McpJsonMapper JSON = McpJsonDefaults.getMapper();

	private HttpServletStreamableServerTransportProvider _transport;

	/**
	 * The service's configuration is polymorphic, so creating one needs the type index.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestMcpServerProtocol.class));
	}

	private McpSyncServer _server;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		MCPServerService.Config<?> config = TypedConfiguration.newConfigItem(MCPServerService.Config.class);
		set(config, MCPServerService.Config.INSTRUCTIONS, INSTRUCTIONS);
		set(config, MCPServerService.Config.ALLOWED_ORIGINS, List.of("https://app.example.com"));

		ExposedTool listThings = TypedConfiguration.newConfigItem(ExposedTool.class);
		listThings.setTool("demo.listThings");
		listThings.setName("list_things");
		listThings.setTitle("List the things");
		listThings.setDescription("Lists every thing the caller may see.");

		Map<String, Tool> registry = new LinkedHashMap<>();
		registry.put("demo.listThings", new StubTool("listThings", ToolMode.READ));
		registry.put("demo.deleteThing", new StubTool("deleteThing", ToolMode.DATA_WRITE));

		_transport = MCPServerService.createTransport(config);
		var builder = MCPServerService.createServer(_transport, config);
		for (McpExposure.Published published : McpExposure.resolve(List.of(listThings), registry,
			name -> ToolGroups.DATA_READ)) {
			builder.tools(McpToolAdapter.adapt(published));
		}
		_server = builder.build();
	}

	@Override
	protected void tearDown() throws Exception {
		_server.close();
		super.tearDown();
	}

	/** {@code initialize} answers the configured instructions. */
	public void testInitializeSendsInstructions() throws IOException {
		Exchange init = initialize(null);

		assertEquals(init.body, HttpServletResponse.SC_OK, init.status);
		Map<String, Object> result = result(init.body);
		assertEquals(INSTRUCTIONS, result.get("instructions"));
	}

	/**
	 * {@code tools/list} shows the published tool under its configured name, title and description,
	 * with the annotations a directory requires - and nothing else of the registry.
	 */
	public void testOnlyThePublishedToolIsListed() throws IOException {
		String session = initialize(null).sessionId;

		Exchange list = post(session, null, request(2, "tools/list", Map.of()));

		List<Map<String, Object>> tools = asList(result(sseData(list.body)).get("tools"));
		assertEquals(list.body, 1, tools.size());
		Map<String, Object> tool = tools.get(0);
		assertEquals("list_things", tool.get("name"));
		assertEquals("List the things", tool.get("title"));
		assertEquals("Lists every thing the caller may see.", tool.get("description"));
		Map<String, Object> annotations = asMap(tool.get("annotations"));
		assertEquals("List the things", annotations.get("title"));
		assertEquals(Boolean.TRUE, annotations.get("readOnlyHint"));
		assertEquals(Boolean.FALSE, annotations.get("destructiveHint"));
	}

	/**
	 * A registry tool that is not published is an unknown tool, under any name a client may guess.
	 */
	public void testAToolThatIsNotPublishedIsUnknown() throws IOException {
		String session = initialize(null).sessionId;

		for (String name : List.of("demo__deleteThing", "demo.deleteThing", "tl-demo-deleteThing")) {
			Exchange call = post(session, null,
				request(3, "tools/call", Map.of("name", name, "arguments", Map.of())));

			Map<String, Object> answer = JSON.readValue(sseData(call.body), new TypeRef<>() {
				// Type capture.
			});
			assertNull(call.body, answer.get("result"));
			String message = (String) asMap(answer.get("error")).get("message");
			assertTrue(message, message.contains("Unknown tool"));
		}
	}

	/** A request from a browser on a foreign origin is refused before it is read. */
	public void testAForeignOriginIsRefused() throws IOException {
		Exchange init = initialize("https://evil.example.org");

		assertEquals(HttpServletResponse.SC_FORBIDDEN, init.status);
		assertNull("No session was opened for it.", init.sessionId);
	}

	/** A configured origin is served, and so is a client that sends no origin at all. */
	public void testAnAllowedOriginAndNoOriginAreServed() throws IOException {
		assertEquals(HttpServletResponse.SC_OK, initialize("https://app.example.com").status);
		assertEquals(HttpServletResponse.SC_OK, initialize(null).status);
	}

	private Exchange initialize(String origin) throws IOException {
		return post(null, origin, request(1, "initialize", Map.of(
			"protocolVersion", "2025-06-18",
			"capabilities", Map.of(),
			"clientInfo", Map.of("name", "test", "version", "1"))));
	}

	private static String request(int id, String method, Map<String, Object> params) throws IOException {
		return JSON.writeValueAsString(Map.of("jsonrpc", "2.0", "id", id, "method", method, "params", params));
	}

	private static Map<String, Object> result(String json) throws IOException {
		Map<String, Object> answer = JSON.readValue(json, new TypeRef<>() {
			// Type capture.
		});
		assertNull(json, answer.get("error"));
		return asMap(answer.get("result"));
	}

	/** The payload of the single {@code data:} line of a server-sent event stream. */
	private static String sseData(String stream) {
		for (String line : stream.split("\n")) {
			if (line.startsWith("data: ")) {
				return line.substring("data: ".length());
			}
		}
		throw new AssertionError("No event in: " + stream);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		return (Map<String, Object>) value;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> asList(Object value) {
		return (List<Map<String, Object>>) value;
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	/** One request and what the transport answered. */
	private static final class Exchange {

		final Map<String, String> requestHeaders = new LinkedHashMap<>();

		final Map<String, String> responseHeaders = new LinkedHashMap<>();

		final StringWriter output = new StringWriter();

		int status = HttpServletResponse.SC_OK;

		String body;

		String sessionId;
	}

	private Exchange post(String session, String origin, String body) throws IOException {
		Exchange exchange = new Exchange();
		exchange.requestHeaders.put("Host", "localhost:8080");
		exchange.requestHeaders.put("Accept", "application/json, text/event-stream");
		exchange.requestHeaders.put("Content-Type", "application/json");
		if (session != null) {
			exchange.requestHeaders.put(HttpHeaders.MCP_SESSION_ID, session);
		}
		if (origin != null) {
			exchange.requestHeaders.put("Origin", origin);
		}

		try {
			_transport.service(request(exchange, body), response(exchange));
		} catch (jakarta.servlet.ServletException ex) {
			throw new IOException(ex);
		}

		exchange.body = exchange.output.toString();
		exchange.sessionId = exchange.responseHeaders.get(HttpHeaders.MCP_SESSION_ID);
		return exchange;
	}

	private static HttpServletRequest request(Exchange exchange, String body) {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		AsyncContext async = proxy(AsyncContext.class, (method, args) -> null);
		return proxy(HttpServletRequest.class, (method, args) -> switch (method.getName()) {
			case "getMethod" -> "POST";
			case "getRequestURI" -> "/mcp";
			case "getCharacterEncoding" -> "UTF-8";
			case "getContentLengthLong" -> Long.valueOf(bytes.length);
			case "getHeader" -> header(exchange.requestHeaders, (String) args[0]);
			case "getHeaderNames" -> Collections.enumeration(exchange.requestHeaders.keySet());
			case "getHeaders" -> {
				String value = header(exchange.requestHeaders, (String) args[0]);
				yield Collections.enumeration(value == null ? List.of() : List.of(value));
			}
			case "getInputStream" -> new ServletInputStream() {
				@Override
				public int read() {
					return in.read();
				}

				@Override
				public boolean isFinished() {
					return in.available() == 0;
				}

				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setReadListener(ReadListener listener) {
					throw new UnsupportedOperationException();
				}
			};
			case "startAsync" -> async;
			case "getAttribute" -> null;
			default -> throw new UnsupportedOperationException("request." + method.getName());
		});
	}

	private static HttpServletResponse response(Exchange exchange) {
		PrintWriter writer = new PrintWriter(exchange.output);
		return proxy(HttpServletResponse.class, (method, args) -> switch (method.getName()) {
			case "setStatus" -> {
				exchange.status = ((Integer) args[0]).intValue();
				yield null;
			}
			case "sendError" -> {
				exchange.status = ((Integer) args[0]).intValue();
				yield null;
			}
			case "setHeader", "addHeader" -> {
				exchange.responseHeaders.put((String) args[0], (String) args[1]);
				yield null;
			}
			case "getHeader" -> header(exchange.responseHeaders, (String) args[0]);
			case "getWriter" -> writer;
			case "setContentType", "setCharacterEncoding", "flushBuffer" -> null;
			case "isCommitted" -> Boolean.FALSE;
			default -> throw new UnsupportedOperationException("response." + method.getName());
		});
	}

	private static String header(Map<String, String> headers, String name) {
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(name)) {
				return entry.getValue();
			}
		}
		return null;
	}

	/** Answers the calls of an interface, with {@code toString} and friends taken care of. */
	private interface Answer {
		Object answer(Method method, Object[] args) throws Throwable;
	}

	private static <T> T proxy(Class<T> type, Answer answer) {
		InvocationHandler handler = (self, method, args) -> switch (method.getName()) {
			case "toString" -> type.getSimpleName() + " double";
			case "hashCode" -> Integer.valueOf(System.identityHashCode(self));
			case "equals" -> Boolean.valueOf(self == args[0]);
			default -> answer.answer(method, args);
		};
		return type.cast(Proxy.newProxyInstance(TestMcpServerProtocol.class.getClassLoader(),
			new Class<?>[] { type }, handler));
	}

	/** A tool that is nothing but its declarations; none of these tests calls one. */
	private record StubTool(String name, ToolMode mode) implements Tool {

		@Override
		public String description() {
			return "Written for the application's own agent.";
		}

		@Override
		public ToolSchema schema() {
			return new ToolSchema(name, description(), "{\"type\":\"object\",\"properties\":{}}");
		}

		@Override
		public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
			throw new AssertionError("Not called by these tests.");
		}
	}
}
