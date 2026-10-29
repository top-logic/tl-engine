/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.workspace.app;

import junit.framework.TestCase;

import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of the context path of the application of a workspace,
 * {@link ApplicationWorkspaces#contextPath(String, String, String, String)}, and of the URLs built from
 * it: {@link ApplicationWorkspaces#applicationUrl(String, int, String)}, under which the control
 * application reaches the application, and {@link ApplicationWorkspaces#previewUrl(String, String, String)},
 * under which a browser opens it.
 */
public class TestContextPath extends TestCase {

	private static final String WORKSPACE_ID = "my-app-1a2b3c4d";

	private static final String ARTIFACT_ID = "my-app";

	private static final String CONTROL_CONTEXT = "/tl-factory-app";

	/**
	 * The default pattern puts the workspace and the artifact into a path of several segments
	 * below the preview route of the control application, at the root or below its context path.
	 */
	public void testDefault() {
		assertEquals("/preview/my-app-1a2b3c4d/my-app",
			ApplicationWorkspaces.contextPath(ApplicationWorkspaces.DEFAULT_CONTEXT_PATH, "", WORKSPACE_ID,
				ARTIFACT_ID));
		assertEquals("/tl-factory-app/preview/my-app-1a2b3c4d/my-app",
			ApplicationWorkspaces.contextPath(ApplicationWorkspaces.DEFAULT_CONTEXT_PATH, CONTROL_CONTEXT,
				WORKSPACE_ID, ARTIFACT_ID));
	}

	/**
	 * The context path of the control application is checked as part of the result.
	 */
	public void testInvalidControlContext() {
		assertInvalid(ApplicationWorkspaces.DEFAULT_CONTEXT_PATH, "/view");
		assertInvalid(ApplicationWorkspaces.DEFAULT_CONTEXT_PATH, "/admin");
		assertInvalid(ApplicationWorkspaces.DEFAULT_CONTEXT_PATH, "/my app");
	}

	/**
	 * Other patterns: the artifact alone, the default of a TopLogic application, and placeholders
	 * in any segment.
	 */
	public void testPatterns() {
		assertEquals(ApplicationWorkspaces.defaultContextPath(ARTIFACT_ID),
			ApplicationWorkspaces.contextPath("/" + ApplicationWorkspaces.ARTIFACT_PLACEHOLDER, CONTROL_CONTEXT,
				WORKSPACE_ID, ARTIFACT_ID));
		assertEquals("/ws/my-app-1a2b3c4d.app/v1_~x",
			ApplicationWorkspaces.contextPath("/ws/" + ApplicationWorkspaces.WORKSPACE_PLACEHOLDER + ".app/v1_~x",
				"", WORKSPACE_ID, ARTIFACT_ID));
	}

	/**
	 * A segment the client of a TopLogic application takes for the start of a route, the
	 * administration of the development server as first segment, and paths that are no context
	 * paths are rejected.
	 */
	public void testInvalid() {
		assertInvalid("/preview/{workspace}/view");
		assertInvalid("/servlet/{artifact}");
		assertInvalid("/admin/{artifact}");
		assertInvalid("{artifact}");
		assertInvalid("/");
		assertInvalid("/preview//{artifact}");
		assertInvalid("/preview/{artifact}/");
		assertInvalid("/preview/../{artifact}");
		assertInvalid("/preview/.hidden/{artifact}");
		assertInvalid("/pre view/{artifact}");
		assertInvalid("/preview?x/{artifact}");
		// "admin" is reserved only as first segment.
		assertEquals("/preview/admin/my-app",
			ApplicationWorkspaces.contextPath("/preview/admin/{artifact}", "", WORKSPACE_ID, ARTIFACT_ID));
	}

	private static void assertInvalid(String pattern) {
		assertInvalid(pattern, "");
	}

	private static void assertInvalid(String pattern, String controlContext) {
		try {
			ApplicationWorkspaces.contextPath(pattern, controlContext, WORKSPACE_ID, ARTIFACT_ID);
			fail("Accepted: " + pattern + " below " + controlContext);
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	/**
	 * The URL under which the control application reaches the application is the origin of the
	 * published port followed by the context path and a slash.
	 */
	public void testApplicationUrl() {
		assertEquals("http://127.0.0.1:20001/preview/my-app-1a2b3c4d/my-app/",
			ApplicationWorkspaces.applicationUrl("127.0.0.1", 20001, "/preview/my-app-1a2b3c4d/my-app"));
		assertEquals("http://127.0.0.1:20001/my-app/",
			ApplicationWorkspaces.applicationUrl("127.0.0.1", 20001, ApplicationWorkspaces.defaultContextPath(ARTIFACT_ID)));
		assertEquals("http://[::1]:20001/my-app/",
			ApplicationWorkspaces.applicationUrl("::1", 20001, ApplicationWorkspaces.defaultContextPath(ARTIFACT_ID)));
	}

	/**
	 * The origin of a published port, with an IPv6 address in brackets.
	 */
	public void testOrigin() {
		assertEquals("http://127.0.0.1:20001", ApplicationWorkspaces.origin("127.0.0.1", 20001));
		assertEquals("http://[::1]:20001", ApplicationWorkspaces.origin("::1", 20001));
		assertEquals("http://[::1]:20001", ApplicationWorkspaces.origin("[::1]", 20001));
	}

	/**
	 * A browser opens the application by the path-absolute URL of its context path through the
	 * preview route, if the context path is below the path of the workspace in the route.
	 */
	public void testPreviewUrl() {
		String contextPath = ApplicationWorkspaces.contextPath(ApplicationWorkspaces.DEFAULT_CONTEXT_PATH,
			CONTROL_CONTEXT, WORKSPACE_ID, ARTIFACT_ID);
		assertEquals("/tl-factory-app/preview/my-app-1a2b3c4d/my-app/",
			ApplicationWorkspaces.previewUrl(CONTROL_CONTEXT, WORKSPACE_ID, contextPath));
		assertEquals("At the root of the control application.", "/preview/my-app-1a2b3c4d/my-app/",
			ApplicationWorkspaces.previewUrl("", WORKSPACE_ID, "/preview/my-app-1a2b3c4d/my-app"));
		assertEquals("The path of the workspace in the route itself.", "/tl-factory-app/preview/my-app-1a2b3c4d/",
			ApplicationWorkspaces.previewUrl(CONTROL_CONTEXT, WORKSPACE_ID, "/tl-factory-app/preview/my-app-1a2b3c4d"));
	}

	/**
	 * An application whose context path is not below the path of its workspace in the preview
	 * route is not opened through it.
	 */
	public void testNoPreviewUrl() {
		assertNull("Without the context of the control application.",
			ApplicationWorkspaces.previewUrl(CONTROL_CONTEXT, WORKSPACE_ID, "/preview/my-app-1a2b3c4d/my-app"));
		assertNull("Served under the artifact.",
			ApplicationWorkspaces.previewUrl(CONTROL_CONTEXT, WORKSPACE_ID, ApplicationWorkspaces.defaultContextPath(ARTIFACT_ID)));
		assertNull("Below another workspace.",
			ApplicationWorkspaces.previewUrl(CONTROL_CONTEXT, WORKSPACE_ID, "/tl-factory-app/preview/other-1a2b3c4d/my-app"));
		assertNull("A workspace whose identifier starts with the one of the workspace.",
			ApplicationWorkspaces.previewUrl(CONTROL_CONTEXT, "my-app", "/tl-factory-app/preview/my-app-1a2b3c4d/my-app"));
		assertNull(ApplicationWorkspaces.previewUrl(CONTROL_CONTEXT, WORKSPACE_ID, null));
	}

}
