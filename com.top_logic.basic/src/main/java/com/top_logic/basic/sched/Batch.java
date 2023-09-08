/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sched;

import com.top_logic.basic.util.Stoppable;

/**
 * Batch extends Thread to allow supervision by a Scheduler.
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public interface Batch extends Runnable, Stoppable {

	/**
	 * Return value of {@link #getStarted()} until the {@link Batch} has completed starting and is
	 * running.
	 */
	public static final long STARTING = 0;

    /** Used to save the time when Batch started */
    public void started(long when);
    
    /** 
     * Signals the Batch that the System is going to shut down.
     * 
     * The default Implementation calls signalStop, 
     */
    public void signalShutdown();

	/** Must not be <code>null</code> or empty. Has to be unique. */
    public String getName();

    /**
	 * Accessor to the time when batch was started.
	 * <p>
	 * {@link #STARTING} means: The {@link Batch} is about to start. This might take some minutes,
	 * for example if it has to wait for the maintenance mode.
	 * </p>
	 */
    public long getStarted();
    
    // Nice to have:
    
    /** 
     * Indicates that Batch is not running (any more).
     */
    // boolean isFinished();
}
