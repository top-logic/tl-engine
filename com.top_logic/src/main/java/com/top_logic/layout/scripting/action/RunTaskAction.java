/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * {@link AwaitAction} that runs a {@link Task} and waits for it successful finish.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface RunTaskAction extends AwaitAction {

	/**
	 * List of {@link Filter} that must accept the last successful {@link TaskResult} of the run
	 * task. If the result is not accepted by each {@link Filter} the task is run again.
	 */
	List<PolymorphicConfiguration<? extends Filter<? super TaskResult>>> getResultFilters();

	/**
	 * Name of the task to run.
	 */
	String getTaskName();

}

