/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import static com.top_logic.basic.StringServices.*;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.LogMessageAction;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Logs the given message at the given log level.
 * 
 * Use for example to log the progress of your test by including this action after every major chunk
 * of tests. <br/>
 * But be aware, that the framework for scripted tests throws an Exception and marks the current
 * test as failed, if an error or an fatal is logged during its execution. <br/>
 * You might use this to warn about incomplete testcases: As the last action of the incomplete
 * testcase, log an error telling "this testcase is still incomplete" (or something alike) and they
 * will fail and remember the person executing them at their incompleteness.
 * 
 * @see LogMessageAction#setLevel(String) for valid/recognized levels.
 * 
 * @see LogMessageAction
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class LogMessageActionOp extends AbstractApplicationActionOp<LogMessageAction> {

	public LogMessageActionOp(InstantiationContext context, LogMessageAction config) {
		super(context, config);
	}

	@Override
	public Object processInternal(ActionContext context, Object argument) {
		if (levelIs("FATAL")) {
			Logger.fatal(config.getMessage(), LogMessageActionOp.class);
		} else if (levelIs("ERROR")) {
			Logger.error(config.getMessage(), LogMessageActionOp.class);
		} else if (levelIs("WARN")) {
			Logger.warn(config.getMessage(), LogMessageActionOp.class);
		} else if (levelIs("INFO")) {
			Logger.info(config.getMessage(), LogMessageActionOp.class);
		} else if (levelIs("DEBUG")) {
			Logger.debug(config.getMessage(), LogMessageActionOp.class);
		} else {
			String message = "Unknown log level: '" + config.getLevel() + "'; Message to log: " + config.getMessage();
			Logger.error(message, LogMessageActionOp.class);
		}
		return argument;
	}

	private boolean levelIs(String anotherString) {
		return nonNull(config.getLevel()).equalsIgnoreCase(anotherString);
	}

}
