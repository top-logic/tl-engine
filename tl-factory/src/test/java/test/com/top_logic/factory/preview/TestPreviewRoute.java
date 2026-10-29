/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.preview;

import java.net.URI;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.factory.preview.PreviewRoute;
import com.top_logic.factory.preview.PreviewTarget;
import com.top_logic.factory.preview.StoredPreviewWorkspaces;

/**
 * Test of the decisions of the preview route, {@link PreviewRoute}: the workspace a request names,
 * the URL it is forwarded to and the refused paths, and the cookies passing in either direction.
 */
public class TestPreviewRoute extends TestCase {

	private static final String CONTEXT_PATH = "/tl-factory-app/preview/my-app-1a2b3c4d/my-app";

	private static final PreviewTarget TARGET = new PreviewTarget(CONTEXT_PATH, "http://127.0.0.1:20001");

	public void testWorkspaceId() {
		assertEquals("my-app-1a2b3c4d", PreviewRoute.workspaceId("/my-app-1a2b3c4d/my-app/x"));
		assertEquals("my-app-1a2b3c4d", PreviewRoute.workspaceId("/my-app-1a2b3c4d"));
		assertNull(PreviewRoute.workspaceId(null));
		assertNull(PreviewRoute.workspaceId("/"));
		assertNull(PreviewRoute.workspaceId("//x"));
		assertNull(PreviewRoute.workspaceId("x/y"));
	}

	/**
	 * Path and query are forwarded unchanged to the origin of the application.
	 */
	public void testTarget() {
		assertEquals(URI.create("http://127.0.0.1:20001" + CONTEXT_PATH + "/"),
			PreviewRoute.target(TARGET, CONTEXT_PATH + "/", null));
		assertEquals(URI.create("http://127.0.0.1:20001" + CONTEXT_PATH),
			PreviewRoute.target(TARGET, CONTEXT_PATH, null));
		assertEquals(URI.create("http://127.0.0.1:20001" + CONTEXT_PATH + "/style/a%20b.css?v=1&x=%2F"),
			PreviewRoute.target(TARGET, CONTEXT_PATH + "/style/a%20b.css", "v=1&x=%2F"));
		assertEquals(URI.create("http://127.0.0.1:20001" + CONTEXT_PATH + "/servlet/LayoutServlet/a;b"),
			PreviewRoute.target(TARGET, CONTEXT_PATH + "/servlet/LayoutServlet/a;b", null));
	}

	/**
	 * Only paths below the context path of the application are forwarded: no other path of the
	 * workspace, no path of the control application, and no path a server could resolve to one of
	 * those.
	 */
	public void testRefusedPaths() {
		assertRefused("/tl-factory-app/preview/my-app-1a2b3c4d/other");
		assertRefused("/tl-factory-app/preview/my-app-1a2b3c4d/my-apps/x");
		assertRefused("/tl-factory-app/preview/my-app-1a2b3c4d");
		assertRefused("/tl-factory-app/admin/stop");
		assertRefused("/admin/stop");
		assertRefused(CONTEXT_PATH + "/../../other-ws/my-app/");
		assertRefused(CONTEXT_PATH + "/x/%2e%2e/%2E%2E/y");
		assertRefused(CONTEXT_PATH + "/..;/..;/x");
		assertRefused(CONTEXT_PATH + "/./x");
		assertRefused(CONTEXT_PATH + "/x\\..\\y");
		assertRefused(CONTEXT_PATH + "/x%5C..%5Cy");
		assertRefused(CONTEXT_PATH + "/a b");
		assertRefused("/tl-factory-app/preview/my-app-1a2b3c4d/my%2Dapp/x");
		assertRefused(null);
	}

	private static void assertRefused(String requestUri) {
		assertNull(requestUri, PreviewRoute.target(TARGET, requestUri, null));
	}

	/**
	 * The session cookie of the control application is the one whose value is the session of the
	 * request; a session cookie of the same name of the application passes, as do all other
	 * cookies.
	 */
	public void testForwardedCookies() {
		assertEquals("JSESSIONID=node0ws.node0; theme=dark",
			PreviewRoute.forwardedCookies(List.of("JSESSIONID=node0ws.node0; JSESSIONID=node0control.node0; theme=dark"),
				"JSESSIONID", "node0control.node0"));
		assertEquals("Several headers, case of the name, quoted value.", "a=1; b=2",
			PreviewRoute.forwardedCookies(List.of("a=1", "jsessionid=\"node0control\"; b=2"), "JSESSIONID",
				"node0control"));
		assertNull(PreviewRoute.forwardedCookies(List.of("JSESSIONID=c"), "JSESSIONID", "c"));
		assertNull(PreviewRoute.forwardedCookies(List.of(), "JSESSIONID", "c"));
		assertEquals("Without a session, nothing is dropped.", "JSESSIONID=c",
			PreviewRoute.forwardedCookies(List.of("JSESSIONID=c"), "JSESSIONID", null));
		assertEquals("Another session cookie name.", "JSESSIONID=c",
			PreviewRoute.forwardedCookies(List.of("JSESSIONID=c; CONTROL=c"), "CONTROL", "c"));
	}

	/**
	 * A cookie of the application passes only if it is confined to its context path.
	 */
	public void testConfinedCookies() {
		assertTrue(PreviewRoute.isConfinedCookie("JSESSIONID=node0ws.node0; Path=" + CONTEXT_PATH + "; HttpOnly",
			CONTEXT_PATH));
		assertTrue(PreviewRoute.isConfinedCookie("a=1; Path=" + CONTEXT_PATH + "/", CONTEXT_PATH));
		assertTrue(PreviewRoute.isConfinedCookie("a=1; Path=" + CONTEXT_PATH + "/servlet; SameSite=Lax",
			CONTEXT_PATH));
		assertTrue("Without path, the path of the request.", PreviewRoute.isConfinedCookie("a=1", CONTEXT_PATH));
		assertTrue(PreviewRoute.isConfinedCookie(
			"a=1; Expires=Wed, 21 Oct 2037 07:28:00 GMT; Max-Age=60; Path=" + CONTEXT_PATH, CONTEXT_PATH));

		assertFalse("The session cookie of the control application.",
			PreviewRoute.isConfinedCookie("JSESSIONID=x; Path=/tl-factory-app", CONTEXT_PATH));
		assertFalse(PreviewRoute.isConfinedCookie("a=1; Path=/", CONTEXT_PATH));
		assertFalse("Another workspace.",
			PreviewRoute.isConfinedCookie("a=1; Path=/tl-factory-app/preview/other/my-app", CONTEXT_PATH));
		assertFalse(PreviewRoute.isConfinedCookie("a=1; Path=" + CONTEXT_PATH + "s", CONTEXT_PATH));
		assertFalse(PreviewRoute.isConfinedCookie("a=1; Domain=example.com; Path=" + CONTEXT_PATH, CONTEXT_PATH));
		assertFalse(PreviewRoute.isConfinedCookie("", CONTEXT_PATH));
	}

	public void testOrigin() {
		assertEquals("http://127.0.0.1:20001", StoredPreviewWorkspaces.origin("127.0.0.1", 20001));
		assertEquals("http://[::1]:20001", StoredPreviewWorkspaces.origin("::1", 20001));
		assertEquals("http://[::1]:20001", StoredPreviewWorkspaces.origin("[::1]", 20001));
	}

}
