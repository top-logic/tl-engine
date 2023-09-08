/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

import static java.util.Objects.*;

/**
 * Utilities for working with {@link Thread}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ThreadUtil {

	/**
	 * Creates a {@link Thread} from the given {@link Runnable}, starts it and returns it.
	 * 
	 * @param runnable
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static Thread start(Runnable runnable) {
		Thread thread = new Thread(requireNonNull(runnable));
		thread.start();
		return thread;
	}

}
