/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.http.HttpServletRequest;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.model.search.expr.config.operations.RequestFunctions;

/**
 * Test of {@link RequestFunctions}.
 */
@SuppressWarnings("javadoc")
public class TestRequestFunctions extends AbstractSearchExpressionTest {

	private static final String SCHEME = "requestScheme()";

	private static final String SERVER_NAME = "requestServerName()";

	private static final String SERVER_PORT = "requestServerPort()";

	private static final String REMOTE_ADDR = "requestRemoteAddr()";

	private static final String URL = "requestUrl()";

	public void testInRequest() throws Throwable {
		HttpServletRequest request = request("https", "app.example.com", 8443, "203.0.113.7",
			"https://app.example.com:8443/app/view/");

		Map<String, Object> values = inThread(() -> {
			ThreadContextManager.setupInteractionContext(null, null, request, null);
			try {
				return evalAll();
			} finally {
				ThreadContextManager.getManager().removeInteraction();
			}
		});

		assertEquals("https", values.get(SCHEME));
		assertEquals("app.example.com", values.get(SERVER_NAME));
		assertEquals(8443.0, values.get(SERVER_PORT));
		assertEquals("203.0.113.7", values.get(REMOTE_ADDR));
		assertEquals("https://app.example.com:8443/app/view/", values.get(URL));
	}

	/**
	 * The test body runs in a system interaction, which has no request.
	 */
	public void testInSystemInteraction() throws Throwable {
		assertNotNull(ThreadContextManager.getInteraction());
		assertAllNull(evalAll());
	}

	/**
	 * A thread without any interaction, e.g. a plain worker thread.
	 *
	 * <p>
	 * The functions are called directly, since compiling a script requires an interaction for
	 * accessing the knowledge base.
	 * </p>
	 */
	public void testWithoutInteraction() throws Throwable {
		assertAllNull(inThread(() -> {
			assertNull(ThreadContextManager.getInteraction());
			Map<String, Object> result = new LinkedHashMap<>();
			result.put(SCHEME, RequestFunctions.scheme());
			result.put(SERVER_NAME, RequestFunctions.serverName());
			result.put(SERVER_PORT, RequestFunctions.serverPort());
			result.put(REMOTE_ADDR, RequestFunctions.remoteAddr());
			result.put(URL, RequestFunctions.url());
			return result;
		}));
	}

	private void assertAllNull(Map<String, Object> values) {
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			assertNull(entry.getKey(), entry.getValue());
		}
	}

	private Map<String, Object> evalAll() throws Exception {
		Map<String, Object> result = new LinkedHashMap<>();
		for (String script : new String[] { SCHEME, SERVER_NAME, SERVER_PORT, REMOTE_ADDR, URL }) {
			result.put(script, evalWithoutErrors(script));
		}
		return result;
	}

	/**
	 * Runs the given computation in a fresh thread, which has no interaction of its own.
	 */
	private static <T> T inThread(ThrowingSupplier<T> computation) throws Throwable {
		AtomicReference<T> result = new AtomicReference<>();
		AtomicReference<Throwable> problem = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			try {
				result.set(computation.get());
			} catch (Throwable ex) {
				problem.set(ex);
			}
		});
		thread.start();
		thread.join();
		if (problem.get() != null) {
			throw problem.get();
		}
		return result.get();
	}

	private interface ThrowingSupplier<T> {
		T get() throws Exception;
	}

	private static HttpServletRequest request(String scheme, String serverName, int serverPort, String remoteAddr,
			String url) {
		return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class },
			(proxy, method, args) -> {
				switch (method.getName()) {
					case "getScheme":
						return scheme;
					case "getServerName":
						return serverName;
					case "getServerPort":
						return serverPort;
					case "getRemoteAddr":
						return remoteAddr;
					case "getRequestURL":
						return new StringBuffer(url);
					case "toString":
						return "TestRequest(" + url + ")";
					case "hashCode":
						return System.identityHashCode(proxy);
					case "equals":
						return proxy == args[0];
					default:
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

	public static Test suite() {
		return suite(TestRequestFunctions.class);
	}

}
