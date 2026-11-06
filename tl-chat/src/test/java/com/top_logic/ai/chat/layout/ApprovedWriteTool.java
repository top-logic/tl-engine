/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * A write that a chat run has to ask the user for, and that changes nothing: what a test needs to
 * park a run on an approval without a model or application data. Registered in the test
 * configuration of this module.
 */
public class ApprovedWriteTool {

	/** The name the scripted model calls the tool by. */
	public static final String NAME = "approvedWrite";

	/** What the tool answers once it was allowed to run. */
	public static final String RESULT = "Written.";

	/** Creates a {@link ApprovedWriteTool}. */
	@CalledByReflection
	public ApprovedWriteTool() {
		// Nothing to set up.
	}

	/**
	 * Pretends to write.
	 *
	 * @return {@link #RESULT}.
	 */
	@ToolMethod(name = NAME, description = "Writes nothing, after the user approved it.",
		mode = ToolMode.DATA_WRITE, group = ToolGroups.DATA_WRITE, marking = AnswerMarking.NONE)
	public String approvedWrite() {
		return RESULT;
	}
}
