/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.browser;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;

/**
 * The writing tools of {@link BrowserTool} describe their calls for the revision log.
 *
 * @see Tool#commitMessage(Map, com.top_logic.tl.ai.tool.api.ToolExecutionContext)
 */
public class TestBrowserToolCommitMessages extends TestCase {

	/** A screenshot with a name and a saved login are logged by what they saved. */
	public void testWritingToolsDescribeTheirCalls() {
		assertEquals(I18NConstants.SCREENSHOT_SAVED__NAME.fill("start.png"),
			message("browserScreenshot", Map.of("name", "start.png")));
		assertEquals(I18NConstants.COOKIES_SAVED__POOL.fill("intranet"),
			message("browserSaveCookies", Map.of("poolName", "intranet")));
	}

	/** A screenshot without a name is named when it is saved, so its call is logged generically. */
	public void testUnnamedScreenshotHasTheGenericMessage() {
		assertEquals(com.top_logic.tl.ai.tool.api.I18NConstants.TOOL_CALL__TOOL.fill("browserScreenshot"),
			message("browserScreenshot", Map.of()));
	}

	private static ResKey message(String toolName, Map<String, Object> arguments) {
		for (Tool tool : AnnotatedTools.from(new BrowserTool())) {
			if (tool.name().equals(toolName)) {
				assertTrue(toolName, tool.mode().writesApplicationState());
				return ToolCatalog.commitMessage(tool, arguments, ToolContext.EMPTY);
			}
		}
		throw new AssertionError("No tool " + toolName);
	}
}
