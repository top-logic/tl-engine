/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.active;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Task {0} already follows its configured active state.
	 * 
	 * @see Scheduler#hasActiveOverride(Task)
	 */
	public static ResKey1 ERROR_NO_ACTIVE_OVERRIDE__TASK;

	static {
		initConstants(I18NConstants.class);
	}

}
