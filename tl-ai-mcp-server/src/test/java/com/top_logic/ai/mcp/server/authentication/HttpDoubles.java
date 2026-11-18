/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The few servlet methods the MCP authentication path actually uses, as test doubles.
 *
 * <p>
 * Dynamic proxies rather than hand-written stubs: {@code HttpServletRequest} and
 * {@code HttpServletResponse} declare some sixty methods each, and a stub class would be sixty
 * lines of noise around the six that matter. A method these doubles do not answer throws, so a test
 * that reaches one learns about it rather than reading a silent default.
 * </p>
 */
public final class HttpDoubles {

	/** The host name every request double is sent to. */
	public static final String SERVER_NAME = "apps.example.com";

	/** The context path every request double is sent to. */
	public static final String CONTEXT_PATH = "/orders";

	/** The application URL of the request doubles. */
	public static final String APPLICATION_URL = "https://" + SERVER_NAME + ":443" + CONTEXT_PATH;

	private HttpDoubles() {
		// Static utility.
	}

	/**
	 * What one request carried and what the code under test answered.
	 */
	public static final class Recorded {

		/** Request headers, by name. */
		public final Map<String, String> requestHeaders = new HashMap<>();

		/** Request method; {@code POST} unless a test says otherwise. */
		public String method = "POST";

		/** Request path. */
		public String path = "/mcp";

		/** The status the code under test sent, or {@code 0} if it sent none. */
		public int status;

		/** The message the code under test sent with {@link #status}. */
		public String message;

		/** The content type the code under test set. */
		public String contentType;

		/** What the code under test wrote to the body. */
		public final StringWriter body = new StringWriter();

		/** The bytes the code under test wrote to the body. */
		public final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		/** Response headers the code under test set, by name. */
		public final Map<String, String> responseHeaders = new LinkedHashMap<>();

		/**
		 * Every value of every response header, by name, in the order they were added; a
		 * <code>setHeader</code> replaces the values of its name.
		 */
		public final Map<String, List<String>> responseHeaderValues = new LinkedHashMap<>();

		/** Whether the filter chain was invoked. */
		public boolean chainInvoked;

		/**
		 * The response header of the given name, ignoring case as HTTP does.
		 *
		 * @return The value, or <code>null</code>.
		 */
		public String responseHeader(String name) {
			for (Map.Entry<String, String> entry : responseHeaders.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(name)) {
					return entry.getValue();
				}
			}
			return null;
		}

		/**
		 * All values of the response header of the given name, ignoring case as HTTP does.
		 */
		public List<String> responseHeaders(String name) {
			List<String> result = new ArrayList<>();
			for (Map.Entry<String, List<String>> entry : responseHeaderValues.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(name)) {
					result.addAll(entry.getValue());
				}
			}
			return result;
		}
	}

	/**
	 * A request reading its headers, method and path from {@code recorded}.
	 */
	public static HttpServletRequest request(Recorded recorded) {
		return (HttpServletRequest) Proxy.newProxyInstance(HttpDoubles.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class }, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) {
					switch (method.getName()) {
						case "getHeader":
							return header(recorded.requestHeaders, (String) args[0]);
						case "getMethod":
							return recorded.method;
						case "getRequestURI":
						case "getPathInfo":
							return recorded.path;
						case "getParameter":
						case "getQueryString":
							return null;
						case "getCookies":
							return null;
						case "getScheme":
							return "https";
						case "getServerName":
							return SERVER_NAME;
						case "getServerPort":
							return Integer.valueOf(443);
						case "getContextPath":
							return CONTEXT_PATH;
						case "toString":
							return "request " + recorded.method + " " + recorded.path;
						default:
							throw unsupported(method);
					}
				}
			});
	}

	/**
	 * A response recording status, message and headers into {@code recorded}.
	 */
	public static HttpServletResponse response(Recorded recorded) {
		return (HttpServletResponse) Proxy.newProxyInstance(HttpDoubles.class.getClassLoader(),
			new Class<?>[] { HttpServletResponse.class }, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) {
					switch (method.getName()) {
						case "sendError":
							recorded.status = ((Integer) args[0]).intValue();
							recorded.message = args.length > 1 ? (String) args[1] : null;
							return null;
						case "setStatus":
							recorded.status = ((Integer) args[0]).intValue();
							return null;
						case "setHeader":
							recorded.responseHeaders.put((String) args[0], (String) args[1]);
							recorded.responseHeaderValues.put((String) args[0],
								new ArrayList<>(List.of((String) args[1])));
							return null;
						case "addHeader":
							recorded.responseHeaders.put((String) args[0], (String) args[1]);
							recorded.responseHeaderValues.computeIfAbsent((String) args[0], x -> new ArrayList<>())
								.add((String) args[1]);
							return null;
						case "getHeader":
							return recorded.responseHeader((String) args[0]);
						case "containsHeader":
							return Boolean.valueOf(recorded.responseHeader((String) args[0]) != null);
						case "setContentType":
							recorded.contentType = (String) args[0];
							return null;
						case "setCharacterEncoding":
						case "setContentLength":
							return null;
						case "getWriter":
							return new PrintWriter(recorded.body);
						case "getOutputStream":
							return new ServletOutputStream() {
								@Override
								public void write(int b) {
									recorded.bytes.write(b);
								}

								@Override
								public boolean isReady() {
									return true;
								}

								@Override
								public void setWriteListener(WriteListener writeListener) {
									throw new UnsupportedOperationException();
								}
							};
						case "toString":
							return "response " + recorded.status;
						default:
							throw unsupported(method);
					}
				}
			});
	}

	/**
	 * A chain that records that it ran, and runs {@code body} while it does.
	 *
	 * @param body
	 *        What to assert about the context the filter established, or <code>null</code> for
	 *        nothing.
	 */
	public static FilterChain chain(Recorded recorded, Runnable body) {
		return (request, response) -> {
			recorded.chainInvoked = true;
			if (body != null) {
				body.run();
			}
		};
	}

	private static String header(Map<String, String> headers, String name) {
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(name)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private static UnsupportedOperationException unsupported(Method method) {
		return new UnsupportedOperationException("The MCP authentication path is not expected to call "
			+ method.getDeclaringClass().getSimpleName() + "." + method.getName()
			+ "(); add it to HttpDoubles if that changed.");
	}

}
