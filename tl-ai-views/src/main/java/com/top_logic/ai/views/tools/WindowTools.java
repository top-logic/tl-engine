/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views.tools;

import java.util.Map;

import com.top_logic.ai.views.WindowPage;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * The tools that see and move through the page the user has open while the run answers.
 */
public class WindowTools {

	private static final String NO_PAGE = "No page is open for this run.";

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
		+ " address, role and name, the view file it comes from ([view ...]), what it shows and the actions it"
		+ " offers. When the user says 'this table' or 'here', this tells you which part and which view file they"
		+ " mean.",
		mode = ToolMode.READ, group = ToolGroups.DATA_READ)
	public ToolResult whereAmI() {
		return RunPage.inWindow(WindowPage::describe).map(ToolResult::of).orElseGet(() -> ToolResult.failure(NO_PAGE));
	}

	/**
	 * Acts on the page for the user.
	 */
	@ToolMethod(description = "Do something on the user's page, as a click does: run an action whereAmI lists on"
		+ " the part at an address - open a navigation item (selectItem with itemId), switch a tab (selectTab with"
		+ " tabId), select a table row, press a button. Leaving a page with unsaved changes is refused.",
		mode = ToolMode.DATA_WRITE, group = ToolGroups.DATA_WRITE)
	public ToolResult act(
			@P("The address from whereAmI") String address,
			@P("The action, as whereAmI lists it") String command,
			@P(value = "The action's arguments, e.g. {\"itemId\": \"orders\"}", required = false) Map<String, Object> arguments) {
		try {
			return RunPage.inWindow(
				root -> WindowPage.act(root, address, command, arguments == null ? Map.of() : arguments))
				.map(ToolResult::of).orElseGet(() -> ToolResult.failure(NO_PAGE));
		} catch (IllegalArgumentException ex) {
			return ToolResult.failure(ex.getMessage());
		}
	}
}
