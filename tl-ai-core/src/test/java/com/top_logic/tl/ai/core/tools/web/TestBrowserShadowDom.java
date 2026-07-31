/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.tools.web.BrowserSession.DomainPolicy;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.InteractiveElement;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.PageView;

/**
 * Gated end-to-end tests that the capture/ref script pierces <em>open</em> shadow DOM: an
 * interactive element inside a web component's open shadow root (and inside nested shadow roots)
 * gets a {@code data-tl-ref} and is actually clickable through the same {@code [data-tl-ref]}
 * locator the tool uses, while a normal light-DOM page is tagged exactly as before and a
 * <em>closed</em> shadow root stays unreachable (a documented DOM limit).
 *
 * <p>
 * These launch a real Chromium (gated behind {@code -Dtl.ai.browser.e2e=true}, like the other
 * browser E2E tests) but need <b>no network</b>: the fixture HTML is loaded straight into the page
 * via a test hook, so a controlled open/nested/closed shadow-DOM document can be exercised without
 * a public site or a loopback server (which SSRF would block anyway).
 * </p>
 */
public class TestBrowserShadowDom extends TestCase {

	/** Allow-any policy: no navigation happens (content is injected), so the policy is irrelevant. */
	private static final DomainPolicy ALLOW_ANY = host -> true;

	private static final int TIMEOUT_MILLIS = 30_000;

	/** A button inside an OPEN shadow root; clicking it writes a marker into the light DOM. */
	private static final String OPEN_SHADOW_FIXTURE =
		"<!doctype html><html><body>"
			+ "<p id='status'>idle</p>"
			+ "<div id='host'></div>"
			+ "<script>"
			+ "  const root = document.getElementById('host').attachShadow({mode:'open'});"
			+ "  root.innerHTML = '<button id=\"accept\">Accept all</button>';"
			+ "  root.getElementById('accept').addEventListener('click',"
			+ "    () => { document.getElementById('status').textContent = 'accepted'; });"
			+ "</script>"
			+ "</body></html>";

	/** A button nested TWO shadow roots deep; clicking it writes a marker into the light DOM. */
	private static final String NESTED_SHADOW_FIXTURE =
		"<!doctype html><html><body>"
			+ "<p id='status'>idle</p>"
			+ "<div id='outer'></div>"
			+ "<script>"
			+ "  const outer = document.getElementById('outer').attachShadow({mode:'open'});"
			+ "  const inner = document.createElement('div');"
			+ "  outer.appendChild(inner);"
			+ "  const innerRoot = inner.attachShadow({mode:'open'});"
			+ "  innerRoot.innerHTML = '<button id=\"deep\">Deep button</button>';"
			+ "  innerRoot.getElementById('deep').addEventListener('click',"
			+ "    () => { document.getElementById('status').textContent = 'deep-clicked'; });"
			+ "</script>"
			+ "</body></html>";

	/** A plain light-DOM page: a button that must be tagged exactly as before. */
	private static final String LIGHT_DOM_FIXTURE =
		"<!doctype html><html><body>"
			+ "<button id='plain'>Plain button</button>"
			+ "</body></html>";

	/** A button inside a CLOSED shadow root, which no DOM traversal can reach. */
	private static final String CLOSED_SHADOW_FIXTURE =
		"<!doctype html><html><body>"
			+ "<button id='plain'>Plain button</button>"
			+ "<div id='host'></div>"
			+ "<script>"
			+ "  const root = document.getElementById('host').attachShadow({mode:'closed'});"
			+ "  root.innerHTML = '<button id=\"hidden\">Closed button</button>';"
			+ "</script>"
			+ "</body></html>";

	/**
	 * A button in an open shadow root gets a ref, and clicking that ref through the real
	 * {@code [data-tl-ref]} locator activates it (the light-DOM marker flips).
	 */
	public void testOpenShadowButtonTaggedAndClickable() {
		if (!e2eEnabled()) {
			return;
		}
		BrowserSession session = BrowserSession.open("shadow-open", TIMEOUT_MILLIS, ALLOW_ANY);
		try {
			PageView view = session.loadHtmlForTest(OPEN_SHADOW_FIXTURE);
			String ref = refWithName(view.elements(), "Accept all");
			assertNotNull("open-shadow button was tagged with a ref (querySelectorAll would miss it)", ref);

			PageView afterClick = session.activateRefForTest(ref);
			assertTrue("clicking the shadow-DOM ref activated the button (marker flipped in light DOM)",
				afterClick.html().contains("accepted"));
		} finally {
			session.close();
		}
	}

	/** A button two shadow roots deep is reached by the recursive descent and is clickable. */
	public void testNestedShadowButtonTaggedAndClickable() {
		if (!e2eEnabled()) {
			return;
		}
		BrowserSession session = BrowserSession.open("shadow-nested", TIMEOUT_MILLIS, ALLOW_ANY);
		try {
			PageView view = session.loadHtmlForTest(NESTED_SHADOW_FIXTURE);
			String ref = refWithName(view.elements(), "Deep button");
			assertNotNull("nested-shadow button was tagged via recursive shadow-root descent", ref);

			PageView afterClick = session.activateRefForTest(ref);
			assertTrue("clicking the nested-shadow ref activated the deep button",
				afterClick.html().contains("deep-clicked"));
		} finally {
			session.close();
		}
	}

	/** A normal light-DOM page is tagged identically - a plain button still gets a ref. */
	public void testLightDomStillTaggedIdentically() {
		if (!e2eEnabled()) {
			return;
		}
		BrowserSession session = BrowserSession.open("shadow-light", TIMEOUT_MILLIS, ALLOW_ANY);
		try {
			PageView view = session.loadHtmlForTest(LIGHT_DOM_FIXTURE);
			String ref = refWithName(view.elements(), "Plain button");
			assertNotNull("plain light-DOM button is tagged just like before", ref);
			assertTrue("light-DOM refs are still generation-tagged (s<gen>e<index>)", ref.startsWith("s"));
		} finally {
			session.close();
		}
	}

	/**
	 * A closed shadow root is opaque to DOM traversal: its button gets no ref (documented limit -
	 * future vision fallback), while a sibling light-DOM button is still tagged.
	 */
	public void testClosedShadowButtonRemainsUnreachable() {
		if (!e2eEnabled()) {
			return;
		}
		BrowserSession session = BrowserSession.open("shadow-closed", TIMEOUT_MILLIS, ALLOW_ANY);
		try {
			PageView view = session.loadHtmlForTest(CLOSED_SHADOW_FIXTURE);
			assertNull("a button in a CLOSED shadow root cannot be tagged by any DOM approach",
				refWithName(view.elements(), "Closed button"));
			assertNotNull("the sibling light-DOM button is still tagged",
				refWithName(view.elements(), "Plain button"));
		} finally {
			session.close();
		}
	}

	private static String refWithName(List<InteractiveElement> elements, String name) {
		for (InteractiveElement element : elements) {
			if (name.equals(element.name())) {
				return element.ref();
			}
		}
		return null;
	}

	private static boolean e2eEnabled() {
		return Boolean.getBoolean("tl.ai.browser.e2e");
	}
}
