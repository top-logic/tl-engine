/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views.tools;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;

/**
 * The view tools that change views log their calls with the view they change.
 */
public class TestViewToolCommitMessages extends TestCase {

	private static final String PATH = "app/start.view.xml";

	/** Writing, editing and deleting name the view; undoing a change says so. */
	public void testTheViewIsNamed() {
		assertEquals(I18NConstants.VIEW_WRITTEN__PATH.fill(PATH), message("writeView", Map.of("path", PATH)));
		assertEquals(I18NConstants.VIEW_EDITED__PATH.fill(PATH), message("editView", Map.of("path", PATH)));
		assertEquals(I18NConstants.VIEW_DELETED__PATH.fill(PATH), message("deleteView", Map.of("path", PATH)));
		assertEquals(I18NConstants.VIEW_CHANGE_REVERTED, message("revertViewChange", Map.of()));
	}

	private static Object message(String toolName, Map<String, Object> arguments) {
		for (Tool tool : AnnotatedTools.from(new ViewTools())) {
			if (tool.name().equals(toolName)) {
				return tool.commitMessage(arguments, ToolContext.EMPTY);
			}
		}
		fail("No tool " + toolName);
		return null;
	}
}
