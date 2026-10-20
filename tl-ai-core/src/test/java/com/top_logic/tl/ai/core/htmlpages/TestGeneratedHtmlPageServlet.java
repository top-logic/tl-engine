/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.htmlpages;

import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tl.ai.core.htmlpages.ServletStubs.Response;
import com.top_logic.tl.ai.core.htmlpages.model.GeneratedHtmlPage;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the document route {@code /html/{id}} delivers a stored page.
 *
 * <p>
 * The route shares its {@code /html/*} prefix with the static PDF.js viewer resources that
 * {@link PdfJsResourceServlet} carves out below {@code /html/pdfjs/*}. This test pins the document
 * route down so that carve-out - or any later one - cannot silently take it away.
 * </p>
 */
public class TestGeneratedHtmlPageServlet extends BasicTestCase {

	private static final String CONTENT = "<!doctype html><html><body>generated</body></html>";

	private GeneratedHtmlPageService _service;

	private GeneratedHtmlPageServlet _servlet;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_service = GeneratedHtmlPageService.Module.INSTANCE.getImplementationInstance();
		_servlet = new GeneratedHtmlPageServlet();
	}

	public void testDeliversStoredPage() throws Exception {
		String id = savePage();

		Response response = get("/" + id);

		assertEquals(HttpServletResponse.SC_OK, response.status());
		assertEquals(CONTENT, response.content());
		assertEquals("text/html", response._contentType);
		assertEquals("LLM-authored HTML must stay sandboxed",
			"sandbox allow-scripts allow-forms allow-modals allow-downloads",
			response._headers.get("Content-Security-Policy"));
	}

	/**
	 * A trailing path below the document id addresses the same document; the id is the whole
	 * capability.
	 */
	public void testIgnoresTrailingPath() throws Exception {
		String id = savePage();

		assertEquals(HttpServletResponse.SC_OK, get("/" + id + "/anything").status());
	}

	public void testUnknownId() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND, get("/does-not-exist").status());
	}

	public void testNoId() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND, get(null).status());
		assertEquals(HttpServletResponse.SC_NOT_FOUND, get("/").status());
	}

	/**
	 * The ids this service hands out are 22 base64url characters, so no document can ever be
	 * addressed through the prefix {@link PdfJsResourceServlet} takes over.
	 */
	public void testIdsCannotCollideWithTheViewerPrefix() throws Exception {
		String id = savePage();

		assertEquals("id length", 22, id.length());
		assertFalse("a document id can never be the viewer prefix", "pdfjs".equals(id));
	}

	private String savePage() {
		GeneratedHtmlPage page;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			page = _service.savePage("Route test", CONTENT.getBytes(StandardCharsets.UTF_8), null);
			tx.commit();
		}
		return page.getId();
	}

	private Response get(String pathInfo) throws Exception {
		Response response = new Response();
		_servlet.doGet(ServletStubs.request(pathInfo), response.asResponse());
		return response;
	}

	/**
	 * Creates the test suite with required service dependencies (KB + ModelService +
	 * GeneratedHtmlPageService).
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestGeneratedHtmlPageServlet.class),
				ModelService.Module.INSTANCE,
				GeneratedHtmlPageService.Module.INSTANCE));
	}
}
