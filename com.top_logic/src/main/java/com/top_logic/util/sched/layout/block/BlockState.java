/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.block;

import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * The different values of how a {@link Task} {@link Scheduler#isTaskBlocked(Task) can be blocked}
 * by the {@link Scheduler}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public enum BlockState {

	/** It is not blocked, and it either has no children or none of them is blocked. */
	NOT_BLOCKED,

	/** It is blocked. */
	BLOCKED,

	/** It is not blocked, but a (direct or indirect) child is blocked. */
	CHILD_BLOCKED

}
