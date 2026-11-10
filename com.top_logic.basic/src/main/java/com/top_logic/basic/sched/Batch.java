/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sched;

import com.top_logic.basic.thread.CancelScope;
import com.top_logic.basic.util.Stoppable;

/**
 * Batch extends Thread to allow supervision by a Scheduler.
 *
 * <p>
 * A run of a batch can be cancelled through a {@link CancelScope} (see {@link #getCancelScope()}):
 * {@link #signalStop()} cancels it. The batch sees the request when it polls
 * {@link #shouldStop()}, and a wait it runs as an interruptible section of the scope (e.g.
 * {@link CancelScope#sleep(long)}) ends at once. No other code of the run is interrupted: it may be
 * reading the database, and an interrupt there closes the database file.
 * </p>
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public interface Batch extends Runnable, Stoppable {

	/**
	 * Return value of {@link #getStarted()} until the {@link Batch} has completed starting and is
	 * running.
	 */
	public static final long STARTING = 0;

	/**
	 * Used to save the time when Batch started.
	 * 
	 * <p>
	 * Called before each run. A stop requested before belongs to an earlier run and does not stop
	 * the run that starts.
	 * </p>
	 */
    public void started(long when);

	/**
	 * The {@link CancelScope} through which the current (or, before it starts, the next) run of
	 * this batch is cancelled.
	 * 
	 * <p>
	 * Whoever runs the batch {@link CancelScope#bind(CancelScope) binds} this scope to the thread
	 * running it, so that waits of the run in {@link CancelScope#current()} end when the batch is
	 * {@link #signalStop() stopped}.
	 * </p>
	 * 
	 * @return The scope of the current run, or {@code null} if this batch is not cancelled
	 *         through a scope.
	 */
	default CancelScope getCancelScope() {
		return null;
	}

	/**
	 * Requests the current run to stop.
	 * 
	 * <p>
	 * A batch that accepts the request {@link CancelScope#cancel() cancels} its
	 * {@link #getCancelScope() scope}: {@link #shouldStop()} becomes {@code true}, and a wait of the
	 * run in that scope ends. The thread running the batch is not interrupted otherwise.
	 * </p>
	 * 
	 * @return {@code false} to indicate that the batch will not stop.
	 */
	@Override
	boolean signalStop();
    
    /** 
     * Signals the Batch that the System is going to shut down.
     * 
     * The default Implementation calls {@link #signalStop()}.
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
