/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.composite;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.util.sched.task.Task;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/** A {@link CompositeTask} dies because a child {@link Task} died. */
	public static ResKey CHILD_TASK_DIED;

	/** Warning, when a child of the {@link CompositeTask} has warnings. */
	public static ResKey1 CHILD_HAS_WARNINGS__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
