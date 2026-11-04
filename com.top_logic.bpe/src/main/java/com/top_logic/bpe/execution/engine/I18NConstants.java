/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey SELECTION_NOT_ALLOWED;

	/**
	 * @en Import failed: {0}
	 */
	public static ResKey1 ERROR_IMPORT_FAILED__DETAILS;

	/**
	 * @en Import failed.
	 */
	public static ResKey ERROR_IMPORT_FAILED;

	/**
	 * @en The file contains no BPMN workflow definition.
	 */
	public static ResKey ERROR_NO_WORKFLOW_DEFINITION;

	/**
	 * @en Imported workflows.
	 */
	public static ResKey IMPORTED_WORKFLOWS;

	/**
	 * @en Processed timer workflow tasks.
	 */
	public static ResKey PROCESSED_TIMER_WORKFLOW_TASKS;

	/**
	 * @en Abort task
	 */
	public static ResKey EDGE_OPTION_LABEL_ABORT;

	/**
	 * @en Cancel task
	 */
	public static ResKey EDGE_OPTION_LABEL_CANCEL;

	/**
	 * @en (Continue)
	 */
	public static ResKey EDGE_OPTION_NO_NAME;

	/**
	 * @en ! {0}
	 */
	public static ResKey1 IMPOSSIBLE_EDGE__LABEL;

	/**
	 * @en You are not allowed to start the workflow at "{0}".
	 */
	public static ResKey1 ERROR_START_NOT_ALLOWED__EVENT;

	/**
	 * @en The task "{0}" is no longer open.
	 */
	public static ResKey1 ERROR_TASK_NOT_ACTIVE__TASK;

	/**
	 * @en You are not allowed to complete the task "{0}".
	 */
	public static ResKey1 ERROR_NOT_ACTOR__TASK;

	/**
	 * @en The task "{0}" cannot be completed with "{1}".
	 */
	public static ResKey2 ERROR_NO_SUCH_OPTION__TASK_OPTION;

	/**
	 * @en Select how to continue after the task "{0}".
	 */
	public static ResKey1 ERROR_DECISION_REQUIRED__TASK;

	/**
	 * @en The task "{0}" cannot be completed: {1}
	 */
	public static ResKey2 ERROR_BLOCKED__TASK_ERRORS;

	/**
	 * @en The process started at "{0}" needs an object of the type "{1}".
	 */
	public static ResKey2 ERROR_NOT_OF_MODEL_TYPE__EVENT_TYPE;

	/**
	 * @en The process object given for the start at "{0}" belongs to a process already started.
	 */
	public static ResKey1 ERROR_ALREADY_STARTED__EVENT;

    static {
        initConstants(I18NConstants.class);
    }

}
