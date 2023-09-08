/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link DoubleClickCommand} is a {@link DoubleClickAction} which dispatches to some
 * command ignoring {@link Control} and index.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class DoubleClickCommand implements DoubleClickAction<Control, Object> {

	private final Command command;
	private boolean isWaitPaneRequested;

	/**
	 * Create a new {@link DoubleClickCommand}.
	 */
	public DoubleClickCommand(Command command) {
		this.command = command;
		isWaitPaneRequested = false;
	}

	@Override
	public HandlerResult handleDoubleClick(DisplayContext context, Control listControl, Object item) {
		return command.executeCommand(context);
	}

	@Override
	public boolean isWaitPaneRequested() {
		return isWaitPaneRequested;
	}

	public void setIsWaitPaneRequested(boolean isWaitPaneRequested) {
		this.isWaitPaneRequested = isWaitPaneRequested;
	}

}