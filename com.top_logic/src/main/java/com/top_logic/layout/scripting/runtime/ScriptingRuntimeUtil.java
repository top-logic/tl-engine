/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime;

import com.top_logic.basic.Logger;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ActionWriter;
import com.top_logic.util.TLContext;

/**
 * The name says everything relevant.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptingRuntimeUtil {

	/**
	 * The instance of the {@link ScriptingRuntimeUtil}. This is not a singleton, as (potential)
	 * subclasses can create further instances.
	 */
	public static final ScriptingRuntimeUtil INSTANCE = new ScriptingRuntimeUtil();

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the {@link #INSTANCE}
	 * constant directly.
	 */
	protected ScriptingRuntimeUtil() {
		// See JavaDoc above.
	}

	/**
	 * Logs that the given action is replayed. Log-Level: Debug
	 */
	public void logActionExecution(ApplicationAction action) {
		String userName = TLContext.getContext().getCurrentUserName();
		String logMessage = "Replaying as user '" + userName + "': " + ActionWriter.INSTANCE.writeAction(action, false);
		Logger.debug(logMessage, ScriptingRuntimeUtil.class);
	}
}
