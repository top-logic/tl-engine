/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.workspace.app;

import java.util.Collections;

import junit.framework.TestCase;

import com.top_logic.factory.workspace.WorkspaceInfo;
import com.top_logic.factory.workspace.WorkspaceState;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of the context path of the application of a workspace,
 * {@link ApplicationWorkspaces#contextPath(String, String, String)}, and of the URL built from it,
 * {@link ApplicationWorkspaces#applicationUrl(WorkspaceInfo, String)}.
 */
public class TestContextPath extends TestCase {

	private static final String WORKSPACE_ID = "my-app-1a2b3c4d";

	private static final String ARTIFACT_ID = "my-app";

	/**
	 * The default pattern puts the workspace and the artifact into a path of several segments.
	 */
	public void testDefault() {
		assertEquals("/preview/my-app-1a2b3c4d/my-app",
			ApplicationWorkspaces.contextPath(ApplicationWorkspaces.DEFAULT_CONTEXT_PATH, WORKSPACE_ID, ARTIFACT_ID));
	}

	/**
	 * Other patterns: the artifact alone, the default of a TopLogic application, and placeholders
	 * in any segment.
	 */
	public void testPatterns() {
		assertEquals(ApplicationWorkspaces.defaultContextPath(ARTIFACT_ID),
			ApplicationWorkspaces.contextPath("/" + ApplicationWorkspaces.ARTIFACT_PLACEHOLDER, WORKSPACE_ID,
				ARTIFACT_ID));
		assertEquals("/ws/my-app-1a2b3c4d.app/v1_~x",
			ApplicationWorkspaces.contextPath("/ws/" + ApplicationWorkspaces.WORKSPACE_PLACEHOLDER + ".app/v1_~x",
				WORKSPACE_ID, ARTIFACT_ID));
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
			ApplicationWorkspaces.contextPath("/preview/admin/{artifact}", WORKSPACE_ID, ARTIFACT_ID));
	}

	private static void assertInvalid(String pattern) {
		try {
			ApplicationWorkspaces.contextPath(pattern, WORKSPACE_ID, ARTIFACT_ID);
			fail("Accepted: " + pattern);
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	/**
	 * The URL of the application is the preview URL of the workspace followed by the context path
	 * and a slash, with or without slash at the end of the preview URL.
	 */
	public void testApplicationUrl() {
		String contextPath = "/preview/my-app-1a2b3c4d/my-app";
		assertEquals("http://localhost:20001/preview/my-app-1a2b3c4d/my-app/",
			ApplicationWorkspaces.applicationUrl(running("http://localhost:20001/"), contextPath));
		assertEquals("http://localhost:20001/preview/my-app-1a2b3c4d/my-app/",
			ApplicationWorkspaces.applicationUrl(running("http://localhost:20001"), contextPath));
		assertEquals("http://localhost:20001/my-app/",
			ApplicationWorkspaces.applicationUrl(running("http://localhost:20001/"),
				ApplicationWorkspaces.defaultContextPath(ARTIFACT_ID)));
	}

	private static WorkspaceInfo running(String previewUrl) {
		return new WorkspaceInfo(WORKSPACE_ID, WorkspaceState.RUNNING, "container", "image", 20001, previewUrl,
			Collections.emptyList());
	}

}
