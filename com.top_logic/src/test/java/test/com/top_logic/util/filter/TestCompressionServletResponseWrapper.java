/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Hashtable;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.CachePolicy;
import com.top_logic.util.TopLogicServlet;
import com.top_logic.util.filter.CompressionServletResponseWrapper;

/**
 * Test case for {@link CompressionServletResponseWrapper}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
/* This test can not extends BasicTestCase, although it uses the PersonManager and the
 * KnowledgeBase. The reason is, that the BasicTestCase installs a Session for each test, but the
 * tests in this class logs in the root user and installs an own session context for it. This
 * produces a clash. */
public class TestCompressionServletResponseWrapper extends TestCase {

	private static final String ADDITIONAL_HEADER = "additionalHeader";

	private static final String ADDITIONAL_HEADER_VALUE = "headerValue";

	private static final String ADDITIONAL_HEADER_FILTERED_SUFFIX = "Filtered";

	private static final String ADDITIONAL_HEADER_FILTERED_VALUE = ADDITIONAL_HEADER_VALUE
		+ ADDITIONAL_HEADER_FILTERED_SUFFIX;

	private static final String CNT_PARAM = "cnt";

	private static final String TEST_PAGE_PATH = "/test-page";

	private static final String TEST_INCLUDE_PATH = "/test-include";

	private static final String TEST_PAGE_FILTERED_PATH = "/filtered/test-page";

	private static final String TEST_INCLUDE_FILTERED_PATH = "/filtered/test-include";

	private ServletRunner _servletRunner;

	private ServletUnitClient _client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_servletRunner =
			new ServletRunner(
				TestCompressionServletResponseWrapper.class.getResourceAsStream("test-web.xml"),
				"/test-app");
		Hashtable<String, String> init = new Hashtable<>();
		init.put(TopLogicServlet.ALWAYS_ZIP_PARAM, "true");

		_servletRunner.registerServlet(TEST_PAGE_PATH, TestPageServlet.class.getName(), init);
		_servletRunner.registerServlet(TEST_INCLUDE_PATH, TestIncludeServlet.class.getName(), init);
		_servletRunner.registerServlet(TEST_PAGE_FILTERED_PATH, TestPageServlet.class.getName(), init);
		_servletRunner.registerServlet(TEST_INCLUDE_FILTERED_PATH, TestIncludeServlet.class.getName(), init);

		_client = _servletRunner.newClient();
	}

	@Override
	protected void tearDown() throws Exception {
		_client.clearContents();
		_client = null;

		_servletRunner.shutDown();
		_servletRunner = null;

		super.tearDown();
	}

	public void testNoCompress() throws MalformedURLException, IOException, ServletException {
		WebResponse servletResponse = invoke(TEST_PAGE_PATH, 2);
		assertEquals(null, servletResponse.getHeaderField("Content-Encoding"));
		assertEquals(ADDITIONAL_HEADER_VALUE, servletResponse.getHeaderField(ADDITIONAL_HEADER));
		assertPlain(servletResponse, 2);
	}

	public void testCompress() throws MalformedURLException, IOException, ServletException {
		WebResponse servletResponse = invoke(TEST_PAGE_PATH, 10000);
		assertEquals("gzip", servletResponse.getHeaderField("Content-Encoding"));
		assertEquals(ADDITIONAL_HEADER_VALUE, servletResponse.getHeaderField(ADDITIONAL_HEADER));
		assertPlain(servletResponse, 10000);
	}

	public void testNoCompressInclude() throws MalformedURLException, IOException, ServletException {
		WebResponse servletResponse = invoke(TEST_INCLUDE_PATH, 2);
		assertEquals(null, servletResponse.getHeaderField("Content-Encoding"));

		// Note: null would be expected in TomCat 7.0
		assertEquals(ADDITIONAL_HEADER_VALUE, servletResponse.getHeaderField(ADDITIONAL_HEADER));

		assertPlain(servletResponse, 2);
	}

	public void testCompressInclude() throws MalformedURLException, IOException, ServletException {
		WebResponse servletResponse = invoke(TEST_INCLUDE_PATH, 10000);
		assertEquals("gzip", servletResponse.getHeaderField("Content-Encoding"));

		// Note: null would be expected in TomCat 7.0
		assertEquals(ADDITIONAL_HEADER_VALUE, servletResponse.getHeaderField(ADDITIONAL_HEADER));

		assertPlain(servletResponse, 10000);
	}

	public void testNoCompressFiltered() throws MalformedURLException, IOException, ServletException {
		WebResponse servletResponse = invoke(TEST_PAGE_FILTERED_PATH, 2);
		assertEquals(null, servletResponse.getHeaderField("Content-Encoding"));
		assertEquals(ADDITIONAL_HEADER_FILTERED_VALUE, servletResponse.getHeaderField(ADDITIONAL_HEADER));
		assertPlain(servletResponse, 2);
	}

	public void testCompressFiltered() throws MalformedURLException, IOException, ServletException {
		WebResponse servletResponse = invoke(TEST_PAGE_FILTERED_PATH, 10000);
		assertEquals("gzip", servletResponse.getHeaderField("Content-Encoding"));
		assertEquals(ADDITIONAL_HEADER_FILTERED_VALUE, servletResponse.getHeaderField(ADDITIONAL_HEADER));
		assertPlain(servletResponse, 10000);
	}

	public void testNoCompressIncludeFiltered() throws MalformedURLException, IOException, ServletException {
		WebResponse servletResponse = invoke(TEST_INCLUDE_FILTERED_PATH, 2);
		assertEquals(null, servletResponse.getHeaderField("Content-Encoding"));

		// Note: null would be expected in TomCat 7.0
		assertEquals(ADDITIONAL_HEADER_FILTERED_VALUE, servletResponse.getHeaderField(ADDITIONAL_HEADER));

		assertPlain(servletResponse, 2);
	}

	public void testCompressIncludeFiltered() throws MalformedURLException, IOException, ServletException {
		WebResponse servletResponse = invoke(TEST_INCLUDE_FILTERED_PATH, 10000);
		assertEquals("gzip", servletResponse.getHeaderField("Content-Encoding"));

		// Note: null would be expected in TomCat 7.0
		assertEquals(ADDITIONAL_HEADER_FILTERED_VALUE, servletResponse.getHeaderField(ADDITIONAL_HEADER));

		assertPlain(servletResponse, 10000);
	}

	private void assertPlain(WebResponse servletResponse, int cnt) throws IOException, UnsupportedEncodingException {
		assertEquals("text/plain", servletResponse.getContentType());
		assertEquals(200, servletResponse.getResponseCode());
		String content = StreamUtilities.readAllFromStream(servletResponse.getInputStream(), "utf-8");
		assertTrue(content, content.startsWith("test"));
		assertEquals(cnt * "test".length(), content.length());
	}

	private WebResponse invoke(String path, int cnt) throws IOException, MalformedURLException, ServletException {
		InvocationContext invocation =
			_client.newInvocation("http://localhost/test-app" + path + "?" + CNT_PARAM + "=" + cnt);
		HttpServletRequest request = invocation.getRequest();
		HttpServletResponse response = invocation.getResponse();

		loginUser(request, response);

		invocation.service();

		WebResponse servletResponse = invocation.getServletResponse();
		return servletResponse;
	}

	private void loginUser(final HttpServletRequest request, final HttpServletResponse response) {
		ThreadContextManager.inSystemInteraction(TestCompressionServletResponseWrapper.class,
			new InContext() {
				@Override
				public void inContext() {
					Person user = PersonManager.getManager().getRoot();
					SessionService.getInstance().loginUser(request, response, user);
				}
			});
	}

	public static final class TestPageServlet extends TopLogicServlet {

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setContentType("text/plain");
			resp.setCharacterEncoding("utf-8");

			// This setting is not possible (at least in TomCat 7.0), if the response is included.
			// In HttpUnit, setting a header is possible (even after producing content). Therefore,
			// Ticket #14837 cannot be reproduced with HttpUnit.
			resp.setHeader(ADDITIONAL_HEADER, ADDITIONAL_HEADER_VALUE);

			PrintWriter writer = resp.getWriter();
			int cnt = Integer.parseInt(req.getParameter(CNT_PARAM));
			for (int n = 0; n < cnt; n++) {
				writer.append("test");
			}
		}
	}

	public static final class TestIncludeServlet extends TopLogicServlet {

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setContentType("text/plain");
			resp.setCharacterEncoding("utf-8");

			int origCnt = Integer.parseInt(req.getParameter(CNT_PARAM));
			PrintWriter writer = resp.getWriter();
			writer.append("test");
			RequestDispatcher dispatcher = req.getRequestDispatcher(
				TEST_PAGE_PATH + "?" + CNT_PARAM + "=" + (origCnt - 1) + "&" + "included" + "=" + "true");
			dispatcher.include(req, resp);
		}
	}

	/**
	 * {@link Filter} wrapping the {@link ServletResponse}.
	 */
	public static final class TestFilter implements Filter {

		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
			// Ignore.
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
				ServletException {
			chain.doFilter(request, wrap(response));
		}

		private HttpServletResponseWrapper wrap(ServletResponse response) {
			return new HttpServletResponseWrapper((HttpServletResponse) response) {

				// Could do some processing here, e.g.:

				@Override
				public void setHeader(String name, String value) {
					if (ADDITIONAL_HEADER.equals(name)) {
						value = value + ADDITIONAL_HEADER_FILTERED_SUFFIX;
					}
					super.setHeader(name, value);
				}

			};
		}

		@Override
		public void destroy() {
			// Ignore.
		}

	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
					ServiceTestSetup.createSetup(
						TestCompressionServletResponseWrapper.class,
				SessionService.Module.INSTANCE,
				CachePolicy.Module.INSTANCE));
	}
}
