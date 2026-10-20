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

import com.top_logic.basic.AliasManager;
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
	 * Happy path with no {@code %HOST%} alias configured: saving a page returns a root-relative
	 * URL under {@code /html/} whose id segment is the 22-character base64url token produced by
	 * the service.
	 */
	public void testReturnsRelativeUrlWithIdSegment() throws Exception {
		String url;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			url = _tool.publishPage("Hello", VALID_HTML);
			tx.commit();
		}
		assertNotNull("tool must return a URL", url);
		assertTrue("URL must start with /html/: " + url, url.startsWith("/html/"));
		String id = url.substring("/html/".length());
		int cut = indexOfAny(id, "/?#");
		if (cut >= 0) {
			id = id.substring(0, cut);
		}
		assertEquals("id segment length", 22, id.length());
	}

	/**
	 * When {@code %HOST%} is configured (typical for a real deployment), the returned URL must
	 * be absolute — otherwise an LLM that receives a root-relative path may invent its own host.
	 */
	public void testPrependsHostAliasWhenConfigured() throws Exception {
		String restore = configureHost("https://app.example.test");
		try {
			String url;
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				url = _tool.publishPage("Hello", VALID_HTML);
				tx.commit();
			}
			assertNotNull("tool must return a URL", url);
			assertTrue("URL must be absolute when %HOST% is set: " + url,
				url.startsWith("https://app.example.test/html/"));
		} finally {
			restoreHost(restore);
		}
	}

	/**
	 * Trailing slashes on the configured {@code %HOST%} must not produce a double slash before
	 * {@code /html/} — callers shouldn't have to be careful about whether they ended the alias
	 * with one.
	 */
	public void testStripsTrailingSlashOnHostAlias() throws Exception {
		String restore = configureHost("https://app.example.test/");
		try {
			String url;
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				url = _tool.publishPage("Hello", VALID_HTML);
				tx.commit();
			}
			assertTrue("trailing slash must be stripped: " + url,
				url.startsWith("https://app.example.test/html/"));
		} finally {
			restoreHost(restore);
		}
	}

	/**
	 * System property backing the {@code %HOST%} alias.
	 *
	 * <p>
	 * The application config defines {@code %HOST% = ${env:tl_host}}, which {@link AliasManager}
	 * resolves via {@link com.top_logic.basic.Environment#getSystemPropertyOrEnvironmentVariable} -
	 * i.e. from a system property or environment variable named {@value}.
	 * </p>
	 */
	private static final String HOST_PROPERTY = "tl_host";

	/**
	 * Configures the {@code %HOST%} alias for the duration of a single test.
	 *
	 * <p>
	 * The alias expander of the started {@link AliasManager} service is frozen and rejects direct
	 * modification ("Can no longer be modified."), and the configured {@code %HOST%} alias would
	 * override any base alias anyway. Instead this sets the {@link #HOST_PROPERTY} system property
	 * that the alias definition reads and calls {@link AliasManager#reload()}, which re-resolves the
	 * configured aliases and rebuilds the expander from scratch - the sanctioned way to change an
	 * alias at runtime.
	 * </p>
	 *
	 * @return The previous value of the backing system property ({@code null} if unset); pass it to
	 *         {@link #restoreHost(String)} to restore the original state.
	 */
	private static String configureHost(String host) {
		String previous = System.getProperty(HOST_PROPERTY);
		System.setProperty(HOST_PROPERTY, host);
		AliasManager.getInstance().reload();
		return previous;
	}

	/**
	 * Restores the {@link #HOST_PROPERTY} system property to the value captured by
	 * {@link #configureHost(String)} and reloads the {@link AliasManager} so {@code %HOST%} reverts.
	 */
	private static void restoreHost(String previous) {
		if (previous == null) {
			System.clearProperty(HOST_PROPERTY);
		} else {
			System.setProperty(HOST_PROPERTY, previous);
		}
		AliasManager.getInstance().reload();
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
