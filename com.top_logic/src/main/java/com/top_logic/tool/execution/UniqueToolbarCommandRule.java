/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Executable only if no parent component with the same toolbar has the given command.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class UniqueToolbarCommandRule implements ExecutabilityRule {

	private CommandHandler _command;

	/**
	 * Creates an {@link ExecutabilityRule} to avoid command duplicates on toolbars.
	 * 
	 * @see UniqueToolbarCommandRule
	 */
	public UniqueToolbarCommandRule(CommandHandler command) {
		_command = command;
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> values) {
		ToolBar currentToolbar = component.getToolBar();

		if (currentToolbar == null) {
			return ExecutableState.EXECUTABLE;
		}

		return isExecutable(component.getParent(), currentToolbar);
	}

	private ExecutableState isExecutable(LayoutComponent parent, ToolBar currentToolbar) {
		do {
			if (parent.getToolBar() != currentToolbar) {
				return ExecutableState.EXECUTABLE;
			}

			if (hasSameCommand(parent)) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}

			parent = parent.getParent();
		} while (parent != null);

		return ExecutableState.EXECUTABLE;
	}

	private boolean hasSameCommand(LayoutComponent component) {
		return component.getCommandById(_command.getID()) != null;
	}

}
