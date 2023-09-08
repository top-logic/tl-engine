/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sched;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;

/**
 * Default implementation for Batch.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class BatchImpl implements Batch {

    /** Name of Batch that will be inherited by ScheduledThread. */
	private final String name;

    /** Records time when Thread was started */
	private volatile long started;

	private boolean shouldStop;

	/**
	 * @param aName
	 *        Must not be <code>null</code> or empty. Has to be unique.
	 */
    public BatchImpl(String aName) {
		this.name = StringServices.isEmpty(aName) ? getClass().getName() : aName;
    }

    /** Overridden to save the start time */
    @Override
	public synchronized void started(long when) {
        started     = when;
        shouldStop  = false;
    }

    /** Override to stop your thread a soft way.
     *
     * The default Implementation sets <code>shouldStop</code> to true.
     *
     * @return false to indicate that you are not willing to stop
     *          (Special wish from JCO)
     */
    @Override
	public synchronized boolean signalStop() {
        shouldStop = true;
        return true;
    }

	/**
	 * Signals that this Thread should stop.
	 * 
	 * (Failure to do so will result in a hard stop by the Scheduler !).
	 */
	protected synchronized boolean getShouldStop() {
		return shouldStop;
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
            Logger.error("Tread is not willing to stop on shutdown", this);
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
