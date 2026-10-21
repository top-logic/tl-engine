/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import jakarta.websocket.OnMessage;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpoint;

import junit.framework.TestCase;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import com.top_logic.util.WebSocketEndpointListener;

/**
 * Test case for {@link WebSocketEndpointListener} in an embedded Jetty server.
 */
public class TestWebSocketEndpointListener extends TestCase {

	private static final String CONTEXT_PATH = "/app";

	private static final String ECHO_PATH = "/echo";

	private static final long TIMEOUT_SECONDS = 10;

	private Server _server;

	private ServletContextHandler _context;

	@Override
	protected void tearDown() throws Exception {
		if (_server != null) {
			_server.stop();
			_server = null;
		}
		super.tearDown();
	}

	/**
	 * An endpoint named in {@link WebSocketEndpointListener#ENDPOINTS_PARAM} is reachable.
	 */
	public void testEndpointRegistered() throws Exception {
		int port = startServer(EchoEndpoint.class.getName());

		CompletableFuture<String> reply = new CompletableFuture<>();
		WebSocket socket = HttpClient.newHttpClient().newWebSocketBuilder()
			.buildAsync(URI.create("ws://localhost:" + port + CONTEXT_PATH + ECHO_PATH), new WebSocket.Listener() {
				private final StringBuilder _buffer = new StringBuilder();

				@Override
				public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
					_buffer.append(data);
					if (last) {
						reply.complete(_buffer.toString());
					}
					webSocket.request(1);
					return null;
				}

				@Override
				public void onError(WebSocket webSocket, Throwable error) {
					reply.completeExceptionally(error);
				}
			})
			.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		try {
			socket.sendText("hello", true).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			assertEquals("echo: hello", reply.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		} finally {
			socket.sendClose(WebSocket.NORMAL_CLOSURE, "").get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		}
	}

	/**
	 * A class name that cannot be loaded does not prevent the other endpoints from being
	 * registered, nor the application from starting.
	 */
	public void testMissingClassDoesNotFailStartup() throws Exception {
		startServer("test.com.top_logic.util.DoesNotExist, " + EchoEndpoint.class.getName());

		assertTrue(_context.isAvailable());
		assertTrue(_server.isStarted());
		ServerContainer container =
			(ServerContainer) _context.getServletContext().getAttribute(
				WebSocketEndpointListener.SERVER_CONTAINER_ATTRIBUTE);
		assertNotNull(container);
	}

	private int startServer(String endpoints) throws Exception {
		_server = new Server();
		ServerConnector connector = new ServerConnector(_server);
		connector.setPort(0);
		_server.addConnector(connector);

		_context = new ServletContextHandler(CONTEXT_PATH);
		_context.setInitParameter(WebSocketEndpointListener.ENDPOINTS_PARAM, endpoints);
		JakartaWebSocketServletContainerInitializer.configure(_context, null);
		_context.addEventListener(new WebSocketEndpointListener());
		_server.setHandler(_context);

		_server.start();
		return connector.getLocalPort();
	}

	/**
	 * Endpoint answering each text message with the message prefixed by {@code "echo: "}.
	 */
	@ServerEndpoint(ECHO_PATH)
	public static class EchoEndpoint {

		/**
		 * Answers a text message.
		 */
		@OnMessage
		public String onMessage(String message) {
			return "echo: " + message;
		}
	}

}
