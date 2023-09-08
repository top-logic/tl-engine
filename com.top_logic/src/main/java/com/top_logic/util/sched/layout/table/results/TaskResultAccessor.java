/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.table.results;

import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;
import com.top_logic.util.sched.task.result.TaskResultWrapper;

/**
 * Accessor for the {@link TaskResult} object.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TaskResultAccessor extends ReadOnlyAccessor<TaskResult> {

	/** Accessing instance to this class. */
    public static final TaskResultAccessor INSTANCE = new TaskResultAccessor();

	/** The name of the {@link Task} of this {@link TaskResult}. */
	public static final String TASK_NAME = "taskName";

	/** The {@link ResultType} of the {@link TaskResult}. */
	public static final String RESULT = "result";

	/** The message of the {@link TaskResult last result}. */
    public static final String MESSAGE = "message";
    
	/** The start date of the execution of a {@link TaskResult}. */
    public static final String START_DATE = "startDate";

	/** The end date of the execution of a {@link TaskResult}. */
    public static final String END_DATE = "endDate";
    
	/** The execution duration of a {@link TaskResult}. */
    public static final String DURATION = "duration";

	/** The optional exception of the {@link TaskResult last result}. */
    public static final String EXCEPTION = "exception";

	/** The cluster node on which this {@link TaskResult} was produced. */
	public static final String CLUSTER_NODE = "clusterNode";

	/** The link to the log file, if any. */
	public static final String LOG_FILE = "logFile";

	/** The warnings of the {@link TaskResult}. */
	public static final String WARNINGS = "warnings";

	public TaskResultAccessor() {
		// TODO Auto-generated constructor stub
	}

    @Override
	public Object getValue(TaskResult theResult, String aProperty) {
		if ((theResult instanceof TaskResultWrapper) && !((TaskResultWrapper) theResult).tValid()) {
			if (TASK_NAME.equals(aProperty)) {
				return Resources.getInstance().getString(I18NConstants.RESULT_INVALID);
			}
			else if (MESSAGE.equals(aProperty)) {
				return I18NConstants.RESULT_INVALID;
			}
			return null;
		}
    	if (theResult != null) {
			if (TASK_NAME.equals(aProperty)) {
				return theResult.getTask().getName();
			}
			else if (MESSAGE.equals(aProperty)) {
	            return theResult.getMessage();
	        }
			else if (RESULT.equals(aProperty)) {
				return theResult.getResultType();
			}
	        else if (START_DATE.equals(aProperty)) {
	        	return theResult.getStartDate();
	        }
	        else if (END_DATE.equals(aProperty)) {
	        	return theResult.getEndDate();
	        }
	        else if (DURATION.equals(aProperty)) {
	        	return theResult.getDuration();
	        }
	        else if (EXCEPTION.equals(aProperty)) {
	        	return theResult.getExceptionDump();
	        }
			else if (CLUSTER_NODE.equals(aProperty)) {
				return theResult.getClusterNode();
			}
			else if (LOG_FILE.equals(aProperty)) {
				return theResult.getLogFile();
			}
			else if (WARNINGS.equals(aProperty)) {
				return theResult.getWarnings();
			}
    	}

    	return null;
    }
}

