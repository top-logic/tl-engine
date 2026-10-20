/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.htmlpages;

import java.io.File;
import java.nio.file.Files;

import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.htmlpages.ServletStubs.Response;
import com.top_logic.tl.ai.core.htmlpages.ServletStubs.Webapp;

/**
 * Tests for {@link PdfJsResourceServlet}.
 *
 * <p>
 * The servlet is driven through {@link ServletStubs}, with a web application root in the file
 * system. That covers the two cases that matter - the viewer is delivered when the application
 * deploys it, and a plain 404 comes back when it does not - without a running container.
 * </p>
 */
public class TestPdfJsResourceServlet extends BasicTestCase {

	private static final String VIEWER = "/web/viewer.html";

	private static final String VIEWER_CONTENT = "<!doctype html><html><body>pdf.js viewer</body></html>";

	private File _root;

	private Webapp _webapp;

	private PdfJsResourceServlet _servlet;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_root = Files.createTempDirectory("TestPdfJsResourceServlet").toFile();
		_webapp = new Webapp(_root);
		_webapp.add(PdfJsResourceServlet.RESOURCE_PREFIX + VIEWER, VIEWER_CONTENT);
		_webapp.add("/WEB-INF/conf/secret.xml", "<password>s3cret</password>");
		_servlet = newServlet(_webapp);
	}

	@Override
	protected void tearDown() throws Exception {
		ServletStubs.deleteRecursively(_root);
		super.tearDown();
	}

	public void testDeliversViewer() throws Exception {
		Response response = get(_servlet, VIEWER);

		assertEquals(HttpServletResponse.SC_OK, response.status());
		assertEquals(VIEWER_CONTENT, response.content());
		assertEquals("text/html", response._contentType);
		assertEquals(VIEWER_CONTENT.length(), response._contentLength);
		assertTrue("delivery must let the browser revalidate",
			response._dateHeaders.containsKey("Last-Modified"));
	}

	/**
	 * The viewer ships file types the container has no mapping for; they must still be delivered
	 * with a content type.
	 */
	public void testUnknownFileTypeIsDeliveredAsOctetStream() throws Exception {
		_webapp.add(PdfJsResourceServlet.RESOURCE_PREFIX + "/web/cmaps/Adobe-Japan1-0.bcmap", "cmap");

		Response response = get(_servlet, "/web/cmaps/Adobe-Japan1-0.bcmap");

		assertEquals(HttpServletResponse.SC_OK, response.status());
		assertEquals("cmap", response.content());
		assertEquals("application/octet-stream", response._contentType);
	}

	public void testDeliversNestedResource() throws Exception {
		_webapp.add(PdfJsResourceServlet.RESOURCE_PREFIX + "/web/locale/de/viewer.ftl", "key = value");

		Response response = get(_servlet, "/web/locale/de/viewer.ftl");

		assertEquals(HttpServletResponse.SC_OK, response.status());
		assertEquals("key = value", response.content());
	}

	/**
	 * Without {@code ext-org-mozilla-pdfjs} nothing is deployed below the prefix; the answer must
	 * be a plain 404 rather than an error page or an exception.
	 */
	public void testNotFoundWithoutViewer() throws Exception {
		File emptyRoot = Files.createTempDirectory("TestPdfJsResourceServletEmpty").toFile();
		try {
			Response response = get(newServlet(new Webapp(emptyRoot)), VIEWER);

			assertEquals(HttpServletResponse.SC_NOT_FOUND, response.status());
			assertEquals("", response.content());
		} finally {
			ServletStubs.deleteRecursively(emptyRoot);
		}
	}

	public void testUnknownResource() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND, get(_servlet, "/web/does-not-exist.js").status());
	}

	public void testDirectoryIsNotDelivered() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND, get(_servlet, "/web").status());
		assertEquals(HttpServletResponse.SC_NOT_FOUND, get(_servlet, "/web/").status());
	}

	public void testNoPathInfo() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND, get(_servlet, null).status());
		assertEquals(HttpServletResponse.SC_NOT_FOUND, get(_servlet, "/").status());
	}

	/**
	 * The servlet must never reach outside its own sub-tree, whatever the container hands it.
	 */
	public void testRejectsTraversal() throws Exception {
		String[] escapes = {
			"/../../WEB-INF/conf/secret.xml",
			"/web/../../../WEB-INF/conf/secret.xml",
			"/./web/viewer.html",
			"/web//viewer.html",
			"/web\\..\\..\\WEB-INF\\conf\\secret.xml",
		};
		for (String pathInfo : escapes) {
			assertNull("must not resolve: " + pathInfo, PdfJsResourceServlet.resourcePath(pathInfo));
			assertEquals("must not be delivered: " + pathInfo,
				HttpServletResponse.SC_NOT_FOUND, get(_servlet, pathInfo).status());
		}
	}

	public void testResourcePathStaysBelowPrefix() {
		assertEquals("/html/pdfjs/web/viewer.html", PdfJsResourceServlet.resourcePath(VIEWER));
		assertEquals("/html/pdfjs/build/pdf.mjs", PdfJsResourceServlet.resourcePath("/build/pdf.mjs"));
		assertNull(PdfJsResourceServlet.resourcePath(null));
		assertNull("a path info never lacks the leading slash",
			PdfJsResourceServlet.resourcePath("web/viewer.html"));
	}

	public void testConditionalGet() throws Exception {
		Response delivered = get(_servlet, VIEWER);
		long lastModified = delivered._dateHeaders.get("Last-Modified").longValue();

		Response unmodified = get(_servlet, VIEWER, lastModified);
		assertEquals(HttpServletResponse.SC_NOT_MODIFIED, unmodified.status());
		assertEquals("", unmodified.content());

		Response outdated = get(_servlet, VIEWER, lastModified - 60_000);
		assertEquals(HttpServletResponse.SC_OK, outdated.status());
		assertEquals(VIEWER_CONTENT, outdated.content());
	}

	private static Response get(PdfJsResourceServlet servlet, String pathInfo) throws Exception {
		return get(servlet, pathInfo, -1);
	}

	private static Response get(PdfJsResourceServlet servlet, String pathInfo, long ifModifiedSince)
			throws Exception {
		Response response = new Response();
		servlet.doGet(ServletStubs.request(pathInfo, ifModifiedSince), response.asResponse());
		return response;
	}

	private static PdfJsResourceServlet newServlet(Webapp webapp) throws Exception {
		PdfJsResourceServlet servlet = new PdfJsResourceServlet();
		servlet.init(ServletStubs.config(webapp.asContext(), "PdfJsResourceServlet"));
		return servlet;
	}

	/**
	 * Creates the test suite. The servlet needs no services; the setup only provides the thread
	 * context {@link BasicTestCase} runs its tests in.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestPdfJsResourceServlet.class)));
	}
}
