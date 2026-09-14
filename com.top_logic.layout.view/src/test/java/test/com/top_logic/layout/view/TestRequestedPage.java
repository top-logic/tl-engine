/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import junit.framework.TestCase;

import com.top_logic.layout.view.ViewServlet;

/**
 * Tests that a request arriving without a session names the page it is sent to once the session
 * exists.
 *
 * @see ViewServlet#requestedPage(String, String)
 */
public class TestRequestedPage extends TestCase {

	/**
	 * The route a URL names survives the establishment of the session.
	 */
	public void testRoute() {
		assertEquals("/view/v1a2b3c/property/42",
			ViewServlet.requestedPage("/app/view/v1a2b3c/property/42", "/app"));
	}

	/**
	 * An application deployed at the root of the server names its pages without a prefix.
	 */
	public void testEmptyContextPath() {
		assertEquals("/view/v1a2b3c/property/42",
			ViewServlet.requestedPage("/view/v1a2b3c/property/42", ""));
	}

	/**
	 * A context path of several segments is cut off as a whole.
	 */
	public void testNestedContextPath() {
		assertEquals("/view/v1a2b3c/property/42",
			ViewServlet.requestedPage("/group/app/view/v1a2b3c/property/42", "/group/app"));
	}

	/**
	 * The encoding of a route segment is kept: it is what distinguishes the segment from the ones
	 * around it.
	 */
	public void testEncodedSegment() {
		assertEquals("/view/v1a2b3c/search/a%2Fb%20c",
			ViewServlet.requestedPage("/app/view/v1a2b3c/search/a%2Fb%20c", "/app"));
	}

	/**
	 * A request naming no page at all enters the view application at its root.
	 */
	public void testViewRoot() {
		assertEquals("/view/", ViewServlet.requestedPage("/app/view/", "/app"));
		assertEquals("/view", ViewServlet.requestedPage("/app/view", "/app"));
	}

	/**
	 * A request for the context itself enters the view application at its root.
	 */
	public void testContextRoot() {
		assertEquals("/view/", ViewServlet.requestedPage("/app", "/app"));
	}

	/**
	 * A context path the URI does not spell out literally enters the view application at its root.
	 */
	public void testEncodedContextPath() {
		assertEquals("/view/", ViewServlet.requestedPage("/a%20pp/view/v1a2b3c/property/42", "/a pp"));
	}

}
