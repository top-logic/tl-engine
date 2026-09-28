/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.servlet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.layout.servlet.CacheControlFilter;

/**
 * Test case for {@link CacheControlFilter}.
 */
@SuppressWarnings("javadoc")
public class TestCacheControlFilter extends TestCase {

	private static final String CONTEXT_PATH = "/app";

	private static final String EXCLUDED_PREFIX = "/images/tmp";

	private CacheControlFilter _filter;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Map<String, String> initParams = new HashMap<>();
		initParams.put(CacheControlFilter.MAX_AGE_PARAM, "180");
		initParams.put(CacheControlFilter.VERSIONED_MAX_AGE_PARAM, "31536000");
		initParams.put(CacheControlFilter.EXCLUDES_PARAM, EXCLUDED_PREFIX);
		initParams.put(CacheControlFilter.DEBUG_PARAM, "false");
		_filter = new CacheControlFilter();
		_filter.init(filterConfig(initParams));
	}

	@Override
	protected void tearDown() throws Exception {
		_filter.destroy();
		_filter = null;
		super.tearDown();
	}

	public void testUnversioned() throws Exception {
		assertEquals("max-age=180", filter("/script/tl.js", null));
		assertEquals("max-age=180", filter("/script/tl.js", ""));
		assertEquals("max-age=180", filter("/script/tl.js", "x=1"));
		assertEquals("max-age=180", filter("/script/tl.js", "vv=1&xv=2"));
	}

	public void testVersioned() throws Exception {
		String expected = "max-age=31536000, immutable";
		assertEquals(expected, filter("/style/tl.css", CacheControlFilter.VERSION_PARAMETER + "=abc123"));
		assertEquals(expected, filter("/style/tl.css", "x=1&" + CacheControlFilter.VERSION_PARAMETER + "=abc123"));
		assertEquals(expected, filter("/style/tl.css", CacheControlFilter.VERSION_PARAMETER));
		assertEquals(expected, filter("/style/tl.css", "x=1&" + CacheControlFilter.VERSION_PARAMETER + "&y=2"));
	}

	public void testDefaults() throws Exception {
		_filter = new CacheControlFilter();
		_filter.init(filterConfig(new HashMap<>()));
		assertEquals("max-age=" + CacheControlFilter.DEFAULT_MAX_AGE, filter("/script/tl.js", null));
		assertEquals("max-age=" + CacheControlFilter.DEFAULT_VERSIONED_MAX_AGE + ", immutable",
			filter("/script/tl.js", CacheControlFilter.VERSION_PARAMETER + "=1"));
	}

	public void testExcluded() throws Exception {
		assertNull(filter(EXCLUDED_PREFIX + "/img.png", null));
		assertNull(filter(EXCLUDED_PREFIX + "/img.png", CacheControlFilter.VERSION_PARAMETER + "=abc123"));
	}

	/**
	 * Runs the filter on a request for the given context-relative path and query string.
	 *
	 * @return The value of the {@link CacheControlFilter#CACHE_CONTROL_HEADER} set on the
	 *         response, or <code>null</code> if none was set.
	 */
	private String filter(String path, String queryString) throws Exception {
		Map<String, String> headers = new HashMap<>();
		HttpServletRequest request = request(CONTEXT_PATH + path, queryString);
		HttpServletResponse response = response(headers);
		boolean[] chainInvoked = { false };
		FilterChain chain = (req, resp) -> {
			assertSame(request, req);
			assertSame(response, resp);
			chainInvoked[0] = true;
		};

		_filter.doFilter(request, response, chain);

		assertTrue("Filter chain must always be invoked.", chainInvoked[0]);
		return headers.get(CacheControlFilter.CACHE_CONTROL_HEADER);
	}

	private static FilterConfig filterConfig(Map<String, String> initParams) {
		return proxy(FilterConfig.class, (proxy, method, args) -> {
			switch (method.getName()) {
				case "getInitParameter":
					return initParams.get(args[0]);
				default:
					throw new UnsupportedOperationException(method.getName());
			}
		});
	}

	private static HttpServletRequest request(String requestURI, String queryString) {
		return proxy(HttpServletRequest.class, (proxy, method, args) -> {
			switch (method.getName()) {
				case "getRequestURI":
					return requestURI;
				case "getContextPath":
					return CONTEXT_PATH;
				case "getQueryString":
					return queryString;
				case "getMethod":
					return "GET";
				default:
					throw new UnsupportedOperationException(method.getName());
			}
		});
	}

	private static HttpServletResponse response(Map<String, String> headers) {
		return proxy(HttpServletResponse.class, (proxy, method, args) -> {
			switch (method.getName()) {
				case "setHeader":
					headers.put((String) args[0], (String) args[1]);
					return null;
				default:
					throw new UnsupportedOperationException(method.getName());
			}
		});
	}

	private static <T> T proxy(Class<T> type, InvocationHandler handler) {
		return type.cast(
			Proxy.newProxyInstance(TestCacheControlFilter.class.getClassLoader(), new Class<?>[] { type }, handler));
	}
}
