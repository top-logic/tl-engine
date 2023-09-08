/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.log;


/**
 * Default {@link #INSTANCE} of the {@link TaskLogFileFactory}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class DefaultTaskLogFileFactory extends TaskLogFileFactory {

	/** The singleton instance of the {@link DefaultTaskLogFileFactory}. */
	public static final DefaultTaskLogFileFactory INSTANCE = new DefaultTaskLogFileFactory();

	private DefaultTaskLogFileFactory() {
		// Reduce visibility
	}

}
