/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeData;

/**
 * {@link TreeControl} used to display the {@link ScriptRecorderTree} {@link ApplicationAction}s.
 * 
 * <p>
 * Provides a command to resume the script execution after a page reload.
 * </p>
 * 
 * @see ResumeScriptExecutionCommand
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ScriptRecorderTreeControl extends TreeControl {

	private ScriptRecorderTree _scriptRecorderTree;

	/**
	 * Create a new {@link ScriptRecorderTreeControl}.
	 */
	public ScriptRecorderTreeControl(TreeData data, ScriptRecorderTree scriptRecorderTree) {
		super(data, createCommandMap(TREE_COMMANDS, ResumeScriptExecutionCommand.INSTANCE));

		_scriptRecorderTree = scriptRecorderTree;
	}

	/**
	 * Underlying component of this control.
	 */
	public ScriptRecorderTree getScriptRecorderTree() {
		return _scriptRecorderTree;
	}

}
