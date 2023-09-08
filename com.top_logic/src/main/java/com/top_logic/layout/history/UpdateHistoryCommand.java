/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link UpdateHistoryCommand} is executed when the history of the
 * client is replayed, i.e. when the client has forgotten the history (e.g.
 * after F5) then the history, which is known by the server, will be ported to
 * the client step by step using this command.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UpdateHistoryCommand extends ControlCommand {

	// Constant is used in history.js
	static final String COMMAND_ID = "updateHistory";
	static final String CALLING_FRAME = "callingFrame";
	// end of Constant are used in history.js

	public static final UpdateHistoryCommand INSTANCE = new UpdateHistoryCommand();

	private UpdateHistoryCommand() {
		// singleton instance
		super(COMMAND_ID);
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		HistoryControl hControl = (HistoryControl) control;

		hControl.replayEntry((String) arguments.get(CALLING_FRAME));
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.UPDATE_HISTORY;
	}

}
