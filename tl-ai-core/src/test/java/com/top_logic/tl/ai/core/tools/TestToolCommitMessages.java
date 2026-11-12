/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.tools.authoring.AgentAuthoringTool;
import com.top_logic.tl.ai.core.tools.authoring.FrameworkWiringTool;
import com.top_logic.tl.ai.core.tools.authoring.ScriptToolAuthoringTool;
import com.top_logic.tl.ai.core.tools.config.LayoutTemplateTool;
import com.top_logic.tl.ai.core.tools.context.ApplicationContextTool;
import com.top_logic.tl.ai.core.tools.memory.UserMemoryTool;
import com.top_logic.tl.ai.core.tools.model.ModelAnnotationEditorTool;
import com.top_logic.tl.ai.core.tools.model.ModelEditorTool;
import com.top_logic.tl.ai.core.tools.model.ObjectEditTool;
import com.top_logic.tl.ai.core.tools.script.ChangeDataTool;
import com.top_logic.tl.ai.core.tools.text.TextEditTool;
import com.top_logic.tl.ai.core.tools.util.PublishHtmlPageTool;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.I18NConstants;
import com.top_logic.tl.ai.tool.api.Tool;

/**
 * Every tool of this module that writes the application's state describes its calls for the
 * revision log, and describing a call fails for no arguments - an identifier that names nothing is
 * logged as it is.
 *
 * @see Tool#commitMessage(Map, com.top_logic.tl.ai.tool.api.ToolExecutionContext)
 */
public class TestToolCommitMessages extends TestCase {

	/** Each writing tool answers a message of its own, not the generic one naming the tool. */
	public void testEveryWritingToolDescribesItsCalls() {
		int writing = 0;
		for (Object provider : List.of(new AgentAuthoringTool(), new FrameworkWiringTool(),
			new ScriptToolAuthoringTool(), new LayoutTemplateTool(), new ApplicationContextTool(),
			new UserMemoryTool(), new ModelAnnotationEditorTool(), new ModelEditorTool(), new ObjectEditTool(),
			new ChangeDataTool(), new TextEditTool(), new PublishHtmlPageTool())) {
			for (Tool tool : AnnotatedTools.from(provider)) {
				if (!tool.mode().writesApplicationState()) {
					continue;
				}
				writing++;
				ResKey message = ToolCatalog.commitMessage(tool, Map.of(), ToolContext.EMPTY);
				assertNotNull(tool.name(), message);
				assertFalse(tool.name() + " says only its name.",
					I18NConstants.TOOL_CALL__TOOL.fill(tool.name()).equals(message));
			}
		}
		assertTrue("Writing tools found: " + writing, writing >= 39);
	}
}
