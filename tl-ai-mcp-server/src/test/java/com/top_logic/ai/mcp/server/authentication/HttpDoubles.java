/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.FilterChain;
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
final class HttpDoubles {

	private HttpDoubles() {
		// Static utility.
	}

	/**
	 * What one request carried and what the code under test answered.
	 */
	static final class Recorded {

		/** Request headers, by name. */
		final Map<String, String> requestHeaders = new HashMap<>();

		/** Request method; {@code POST} unless a test says otherwise. */
		String method = "POST";

		/** Request path. */
		String path = "/mcp";

		/** The status the code under test sent, or {@code 0} if it sent none. */
		int status;

		/** The message the code under test sent with {@link #status}. */
		String message;

		/** Response headers the code under test set, by name. */
		final Map<String, String> responseHeaders = new LinkedHashMap<>();

		/** Whether the filter chain was invoked. */
		boolean chainInvoked;

		/**
		 * The response header of the given name, ignoring case as HTTP does.
		 *
		 * @return The value, or <code>null</code>.
		 */
		String responseHeader(String name) {
			for (Map.Entry<String, String> entry : responseHeaders.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(name)) {
					return entry.getValue();
				}
			}
			return null;
		}
	}

	/**
	 * A request reading its headers, method and path from {@code recorded}.
	 */
	static HttpServletRequest request(Recorded recorded) {
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
	static HttpServletResponse response(Recorded recorded) {
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
						case "addHeader":
							recorded.responseHeaders.put((String) args[0], (String) args[1]);
							return null;
						case "getHeader":
							return recorded.responseHeader((String) args[0]);
						case "containsHeader":
							return Boolean.valueOf(recorded.responseHeader((String) args[0]) != null);
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
	static FilterChain chain(Recorded recorded, Runnable body) {
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
