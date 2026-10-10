/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import junit.framework.TestCase;

/**
 * What the chat's side pane shows of an HTML document it shows in place.
 */
public class TestPaneDocument extends TestCase {

	/** The body only: the document's own head and styles would apply to the whole page. */
	public void testOnlyTheBodyIsShown() {
		String shown = PresentationPane.inPlace(
			"<!DOCTYPE html><html><head><title>T</title><style>body { color: red; }</style></head>"
				+ "<body><div class=\"report\"><p>Text</p></div></body></html>");

		assertFalse(shown, shown.contains("<style"));
		assertFalse(shown, shown.contains("<title"));
		assertTrue(shown, shown.contains("<p>Text</p>"));
	}

	/** What is for the reader of the download is left out; the form shows it as fields. */
	public void testWhatIsForTheDownloadOnlyIsLeftOut() {
		String shown = PresentationPane.inPlace("<html><body><div class=\"report\">"
			+ "<p class=\"title download-only\"><strong>The question</strong></p>"
			+ "<p class=\"meta download-only\">Research report, 11 Oct 2026</p>"
			+ "<p>The answer.</p></div></body></html>");

		assertFalse(shown, shown.contains("The question"));
		assertFalse(shown, shown.contains("Research report"));
		assertTrue(shown, shown.contains("<p>The answer.</p>"));
	}
}
