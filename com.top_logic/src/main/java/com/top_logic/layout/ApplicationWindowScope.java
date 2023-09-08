/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.concurrent.ScheduledExecutorService;

/**
 * {@link WindowScope} of the top-level (initial) application window.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ApplicationWindowScope extends WindowScope {

	/**
	 * {@link ScheduledExecutorService} to schedule actions to be executed later in the
	 * application's UI thread.
	 * 
	 * <p>
	 * Note: The service must only be used from within a command execution thread. The scheduled
	 * action are never executed in parallel, but always within the application's UI command thread.
	 * </p>
	 */
	ScheduledExecutorService getUIExecutor();

}
