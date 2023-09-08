/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.layout.scripting.runtime.action.SleepActionOp;

/**
 * Calls {@link Thread#sleep(long)} with the given number of milliseconds.
 * 
 * @see SleepActionOp
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface SleepAction extends ApplicationAction {

	/**
	 * The number of milliseconds to sleep.
	 */
	long getSleepTimeMillis();

	/**
	 * @see #getSleepTimeMillis()
	 */
	void setSleepTimeMillis(long value);

}
