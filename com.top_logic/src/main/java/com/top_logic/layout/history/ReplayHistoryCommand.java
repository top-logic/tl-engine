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
 * The {@link ReplayHistoryCommand} is used to snyc the server history with the
 * client history by updating the client side history frames from some index in
 * the history queue until the {@link HistoryQueue#getIndex() current index}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class ReplayHistoryCommand extends ControlCommand {

	// Constant are used in history.js
	/**
	 * used as parameter for this command in history.js. The value is set as
	 * value of {@link HistoryControl#TL_REPLAY_FROM_ATTR}.
	 */
	public static final String START_INDEX = "startIndex";
	public static final String COMMAND_ID = "startReplay";
	// end of Constant are used in history.js

	public static final ReplayHistoryCommand INSTANCE = new ReplayHistoryCommand();

	private ReplayHistoryCommand() {
		super(COMMAND_ID);
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		HistoryControl hControl = (HistoryControl) control;

		hControl.replayHistory((Integer) arguments.get(START_INDEX), (String) arguments.get(UpdateHistoryCommand.CALLING_FRAME));
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.REPLAY_HISTORY;
	}

}
