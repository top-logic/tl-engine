/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views.tools;

import com.top_logic.ai.views.WindowPage;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * The tool that sees the page the user has open while the run answers.
 */
public class WindowTools {

	/**
	 * Creates a {@link WindowTools}.
	 */
	public WindowTools() {
		// For the reflective tool factory.
	}

	/**
	 * What the user looks at.
	 */
	@ToolMethod(description = "Read the page the user has open: every part of it, indented by nesting, with its"
		+ " address, role and name, the view file it comes from ([view ...]) and what it shows. When the user says 'this table' or 'here', this tells you which part and which view file they"
		+ " mean.",
		mode = ToolMode.READ, group = ToolGroups.DATA_READ)
	public ToolResult whereAmI() {
		return RunPage.inWindow(WindowPage::describe).map(ToolResult::of).orElseGet(() -> ToolResult.failure(RunPage.NO_PAGE));
	}
}
