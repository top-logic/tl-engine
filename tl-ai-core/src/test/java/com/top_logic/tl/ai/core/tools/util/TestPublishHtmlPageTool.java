/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.util;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tl.ai.core.htmlpages.GeneratedHtmlPageService;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link PublishHtmlPageTool}.
 *
 * <p>
 * Exercises the tool-facing API that translates user-supplied string arguments
 * into a {@link GeneratedHtmlPageService#savePage} call and returns the public
 * relative URL of the freshly stored page.
 * </p>
 */
public class TestPublishHtmlPageTool extends BasicTestCase {

	private static final String VALID_HTML = "<!doctype html><html><body>hi</body></html>";

	private PublishHtmlPageTool _tool;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tool = new PublishHtmlPageTool();
	}

	/**
	 * Happy path: saving a page returns a relative URL under {@code /api/html/}
	 * whose id segment is the 22-character base64url token produced by the service.
	 */
	public void testReturnsRelativeUrlWithIdSegment() throws Exception {
		String url;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			url = _tool.publishPage("Hello", VALID_HTML);
			tx.commit();
		}
		assertNotNull("tool must return a URL", url);
		assertTrue("URL must start with /api/html/: " + url, url.startsWith("/api/html/"));
		String id = url.substring("/api/html/".length());
		int cut = indexOfAny(id, "/?#");
		if (cut >= 0) {
			id = id.substring(0, cut);
		}
		assertEquals("id segment length", 22, id.length());
	}

	/**
	 * Empty HTML payloads must be rejected before persistence is attempted.
	 */
	public void testRejectsEmptyHtml() {
		try {
			_tool.publishPage("t", "");
			fail("expected IllegalArgumentException for empty html");
		} catch (IllegalArgumentException ex) {
			assertMessageMentions(ex, "html");
		}
	}

	/**
	 * Blank titles (whitespace only) must be rejected.
	 */
	public void testRejectsBlankTitle() {
		try {
			_tool.publishPage("   ", VALID_HTML);
			fail("expected IllegalArgumentException for blank title");
		} catch (IllegalArgumentException ex) {
			assertMessageMentions(ex, "title");
		}
	}

	private static void assertMessageMentions(RuntimeException ex, String needle) {
		String msg = ex.getMessage();
		assertNotNull("exception must carry a message", msg);
		assertTrue("message must mention '" + needle + "': " + msg,
			msg.toLowerCase().contains(needle.toLowerCase()));
	}

	private static int indexOfAny(String s, String chars) {
		int best = -1;
		for (int i = 0; i < chars.length(); i++) {
			int idx = s.indexOf(chars.charAt(i));
			if (idx >= 0 && (best < 0 || idx < best)) {
				best = idx;
			}
		}
		return best;
	}

	/**
	 * Creates the test suite with required service dependencies
	 * (KB + ModelService + GeneratedHtmlPageService).
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestPublishHtmlPageTool.class),
				ModelService.Module.INSTANCE,
				GeneratedHtmlPageService.Module.INSTANCE));
	}
}
