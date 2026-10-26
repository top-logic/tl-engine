/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import junit.framework.TestCase;

import com.top_logic.layout.react.servlet.SessionActivityFilter;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests {@link SessionActivityFilter}: every request carrying a session reaches the activity
 * listeners of that session, whichever servlet answers it.
 */
public class TestSessionActivityFilter extends TestCase {

	private static final int HALF_AN_HOUR = 1800;

	/**
	 * A request of any servlet - a chat request, say - that carries the session is announced, with
	 * the session's inactivity interval, before the servlet answers it.
	 */
	public void testRequestWithSessionIsAnnounced() throws Exception {
		HttpSession session = session("s1");
		ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
		List<Integer> announced = new ArrayList<>();
		registry.addActivityListener((accessTime, maxInactiveSeconds) -> announced.add(maxInactiveSeconds));

		List<ServletRequest> passed = new ArrayList<>();
		new SessionActivityFilter().doFilter(request(session), response(), chain(passed));

		assertEquals(List.of(HALF_AN_HOUR), announced);
		assertEquals("The request must still reach the servlet.", 1, passed.size());
	}

	/**
	 * A request without a session - a poll identifying its window by a token - restarts no timeout
	 * and is not announced, but is answered.
	 */
	public void testRequestWithoutSessionPasses() throws Exception {
		List<ServletRequest> passed = new ArrayList<>();
		new SessionActivityFilter().doFilter(request(null), response(), chain(passed));

		assertEquals(1, passed.size());
	}

	/**
	 * A session the React layer has not used gets no registry from a request passing by.
	 */
	public void testSessionWithoutRegistryGetsNone() throws Exception {
		HttpSession session = session("s2");
		new SessionActivityFilter().doFilter(request(session), response(), chain(new ArrayList<>()));

		assertNull(ReactWindowRegistry.lookup(session));
	}

	private static FilterChain chain(List<ServletRequest> passed) {
		return (request, response) -> passed.add(request);
	}

	private static HttpSession session(String id) {
		Map<String, Object> attributes = new HashMap<>();
		return (HttpSession) Proxy.newProxyInstance(TestSessionActivityFilter.class.getClassLoader(),
			new Class<?>[] { HttpSession.class }, (proxy, method, args) -> {
				switch (method.getName()) {
					case "getId":
						return id;
					case "getMaxInactiveInterval":
						return HALF_AN_HOUR;
					case "getAttribute":
						return attributes.get(args[0]);
					case "setAttribute":
						attributes.put((String) args[0], args[1]);
						return null;
					case "removeAttribute":
						attributes.remove(args[0]);
						return null;
					case "hashCode":
						return System.identityHashCode(proxy);
					case "equals":
						return proxy == args[0];
					default:
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

	private static HttpServletRequest request(HttpSession session) {
		return (HttpServletRequest) Proxy.newProxyInstance(TestSessionActivityFilter.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class }, (proxy, method, args) -> {
				if (method.getName().equals("getSession") && args != null && args.length == 1
					&& Boolean.FALSE.equals(args[0])) {
					return session;
				}
				throw new UnsupportedOperationException(method.getName());
			});
	}

	private static HttpServletResponse response() {
		return (HttpServletResponse) Proxy.newProxyInstance(TestSessionActivityFilter.class.getClassLoader(),
			new Class<?>[] { HttpServletResponse.class }, (proxy, method, args) -> {
				throw new UnsupportedOperationException(method.getName());
			});
	}
}
