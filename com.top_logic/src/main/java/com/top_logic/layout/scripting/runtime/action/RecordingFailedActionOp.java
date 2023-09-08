/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.RecordingFailedAction;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * This {@link ApplicationAction} is recorded, when the recording of the actual
 * {@link ApplicationAction} fails. Useful for printing a warning to the tester.
 * 
 * If an {@link RecordingFailedAction} is recorded, the tester should realize the script is unusable
 * and not execute it. In this case, this class would not be necessary.
 * 
 * @see RecordingFailedAction
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class RecordingFailedActionOp extends AbstractApplicationActionOp<RecordingFailedAction> {

	/** Nothing unexpected */
	public RecordingFailedActionOp(InstantiationContext context, RecordingFailedAction config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) {
		String errorMessage = "The recording of an action failed. You have to repair the problem and record it again."
			+ " The error was: >>> " + getConfig().getMessage() + " <<< Cause:\n\n" + getConfig().getPrintedCause();
		throw new RuntimeException(errorMessage);
	}

}
