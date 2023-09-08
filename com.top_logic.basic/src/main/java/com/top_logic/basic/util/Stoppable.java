/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

/**
 * A {@link Stoppable} can be signalled to stop the actual work.
 * 
 * <p>
 * This interface is designed to used multi-threaded.
 * </p>
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Stoppable {

	/**
	 * Causes this {@link Stoppable} to stop the actual work.
	 * 
	 * @return <code>false</code> to indicate that the {@link Stoppable} will not stop.
	 */
	boolean signalStop();

	/**
	 * Determines whether the {@link Stoppable} was requested to stop, and it has accepted the
	 * request.
	 * 
	 * @return {@link #signalStop()} was called and ad returned <code>true</code>.
	 */
	boolean shouldStop();

}

