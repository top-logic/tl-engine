/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.block;

import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * Whether a {@link Task} or one of its children is {@link Scheduler#isActive(Task) inactive}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public enum BlockState {

	/** It is active, and it either has no children or all of them are active. */
	NOT_BLOCKED,

	/** It is inactive. */
	BLOCKED,

	/** It is active, but a (direct or indirect) child is inactive. */
	CHILD_BLOCKED

}
