/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public final class I18NConstants extends I18NConstantsBase {

	public static ResKey1 MANUAL_LOCK_RELEASE_NO_LOCK_EXISTING;

	public static ResKey MANUAL_LOCK_RELEASE_COMPONENT_IS_NULL;

	public static ResKey1 MANUAL_LOCK_RELEASE_COMPONENT_IS_NO_SELECTABLE__COMPONENT;

	public static ResKey1 MANUAL_LOCK_RELEASE_SELECTION_IS_NO_TASK__SELECTION;

	public static ResKey MANUAL_LOCK_RELEASE_TASK_IS_NULL;

	public static ResKey1 MANUAL_LOCK_RELEASE_NO_LOCK_TASK_IS_NOT_GLOBAL__TASK;

	public static ResKey1 MANUAL_LOCK_RELEASE_NO_LOCK_TASK_IS_NOT_PERSISTENT__TASK;

	public static ResKey1 MANUAL_LOCK_RELEASE_TASK_END_BY_MANUAL_LOCK_RELEASE__TASK;

	public static ResKey1 MANUAL_LOCK_RELEASE_DONE__TASK;

	public static ResKey TOOLTIP_CAPTION_WARNINGS_SINGULAR;

	public static ResKey TOOLTIP_CAPTION_WARNINGS_PLURAL;

	public static ResKey SHOULD_START_MAINTENANCE_MODE_FOR_TASK;

	public static ResKey1 SCHEDULED_SUCCESSFULLY__TASK;

	public static ResKey NO_TASK_SELECTED_FOR_SCHEDULING;

	public static ResKey1 TASK_NOT_RUNNING__TASK;

	public static ResKey SCHEDULER_SUSPENDED;

	public static ResKey1 TASK_BLOCKED__TASK;

	public static ResKey1 TASK_RUNNING__TASK;

	public static ResKey1 TASK_IS_NOT_TOP_LEVEL__TASK;

	public static ResKey STOP_TASK_CONFIRM = legacyKey("tl.task.stop.confirm");

	public static ResKey TASK_NO_LOG_FILE = legacyKey("tl.task.logFile.none");

	public static ResKey LOG_FILE_NOT_AVAILABLE__FILE = legacyKey("tl.task.logFile.notAvailable");

	public static ResKey TASK_STOP_SIGNALED = legacyKey("tl.task.stop.signaled");

	static {
		initConstants(I18NConstants.class);
	}

}
