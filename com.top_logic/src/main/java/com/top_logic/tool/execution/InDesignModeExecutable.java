/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.editor.commands.ToggleDesignModeCommand;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ExecutabilityRule} for {@link CommandHandler} that must only be visible when application
 * is in design mode.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InDesignModeExecutable implements ExecutabilityRule {

	/**
	 * Singleton {@link InDesignModeExecutable} instance.
	 */
	public static final InDesignModeExecutable INSTANCE = new InDesignModeExecutable();

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		boolean inDesignMode = ToggleDesignModeCommand.isInDesignMode(ThreadContextManager.getSubSession());
		return inDesignMode ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
	}

}

