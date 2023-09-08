/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;
import com.top_logic.util.sched.task.Task;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix MEMORY_OBSERVER = legacyPrefix("admin.sys.memoryObserver.");

	/** The task did not write an end result. */
	public static ResKey TASK_DONE_WITHOUT_RESULT;

	/** The task was still active when the system or cluster node was shut down. */
	public static ResKey TASK_ACTIVE_ON_SHUTDOWN;

	public static ResKey TASK_MARKED_AS_DONE_BY_SCHEDULER;

	/** A {@link Task} dies with an unexpected error. */
	public static ResKey UNEXPECTED_ERROR;

	public static ResKey NOTHING_TO_CLEANUP;

	public static ResKey1 CLEANUP_SUCCESS__COUNT;

	public static ResKey MEMORY_CHART_AXIS_TIME = legacyKey("admin.sys.memoryObserver.chart.labelX");

	public static ResKey MEMORY_CHART_AXIS_VALUE = legacyKey("admin.sys.memoryObserver.chart.labelY");

	public static ResKey MEMORY_CHART_TITLE = legacyKey("admin.sys.memoryObserver.chart.title");

	static {
		initConstants(I18NConstants.class);
	}
}
