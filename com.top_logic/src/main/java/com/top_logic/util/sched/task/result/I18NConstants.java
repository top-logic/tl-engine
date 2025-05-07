/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.result;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey NOT_FINISHED;

	public static ResKey SUCCESS;

	public static ResKey CANCELED;

	public static ResKey WARNING;

	public static ResKey FAILURE;

	public static ResKey ERROR;

	public static ResKey UNKNOWN;

	public static ResKey NOT_FINISHED_CHILDREN;

	public static ResKey SUCCESS_CHILDREN;

	public static ResKey CANCELED_CHILDREN;

	public static ResKey WARNING_CHILDREN;

	public static ResKey FAILURE_CHILDREN;

	public static ResKey ERROR_CHILDREN;

	public static ResKey UNKNOWN_CHILDREN;

	/**
	 * @en Set log file "{1}" for task: {0}
	 */
	public static ResKey2 SET_TASK_LOG__TASK_LOG;

	/**
	 * @en Recorded warnings for task: {0}
	 */
	public static ResKey1 RECORDED_TASK_WARNINGS__TASK;

	static {
		initConstants(I18NConstants.class);
	}

}
