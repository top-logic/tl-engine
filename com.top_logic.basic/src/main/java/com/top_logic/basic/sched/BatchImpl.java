/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sched;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.thread.CancelScope;

/**
 * Default implementation for Batch.
 *
 * <p>
 * Each run has a {@link CancelScope} of its own, created in {@link #started(long)}. That scope is
 * the only record of a stop request: {@link #signalStop()} cancels it, {@link #getShouldStop()}
 * reports whether it is cancelled.
 * </p>
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class BatchImpl implements Batch {

    /** Name of Batch that will be inherited by ScheduledThread. */
	private final String name;

    /** Records time when Thread was started */
	private volatile long started;

	/** The scope of the current run, replaced in {@link #started(long)}. */
	private volatile CancelScope _cancelScope = new CancelScope();

	/**
	 * @param aName
	 *        Must not be <code>null</code> or empty. Has to be unique.
	 */
    public BatchImpl(String aName) {
		this.name = StringServices.isEmpty(aName) ? getClass().getName() : aName;
    }

	/**
	 * Saves the start time and creates the {@link #getCancelScope() scope} of the run that starts.
	 */
    @Override
	public synchronized void started(long when) {
        started     = when;
		_cancelScope = new CancelScope();
    }

	@Override
	public CancelScope getCancelScope() {
		return _cancelScope;
	}

	/**
	 * Override to stop your thread a soft way.
	 *
	 * <p>
	 * The default implementation {@link CancelScope#cancel() cancels} the {@link #getCancelScope()
	 * scope} of the current run.
	 * </p>
	 *
	 * @return false to indicate that you are not willing to stop (Special wish from JCO)
	 */
    @Override
	public boolean signalStop() {
		_cancelScope.cancel();
        return true;
    }

	/**
	 * Whether this batch should stop: the {@link #getCancelScope() scope} of the current run is
	 * {@link CancelScope#isCancelled() cancelled}.
	 */
	protected boolean getShouldStop() {
		return _cancelScope.isCancelled();
	}

	@Override
	public final boolean shouldStop() {
		return getShouldStop();
	}

    /**
     * @see com.top_logic.basic.sched.Batch#signalShutdown()
     */
    @Override
	public void signalShutdown() {
		if (!signalStop()) {
			// Not an error by itself: The shutdown waits for the end of the run.
			Logger.info("Batch '" + getName() + "' does not react to the stop request on shutdown.", this);
		}
    }

    /** Accessor to the name.
     */
    @Override
	public String getName() {
        return name;
    }

	/**
	 * You cannot override this function so the Scheduler can not be fooled.
	 * 
	 * @see com.top_logic.basic.sched.Batch#getStarted()
	 */
    @Override
	public final long getStarted() {
        return started;
    }
}
