/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sched;

import com.top_logic.basic.thread.CancelScope;
import com.top_logic.basic.thread.ThreadLocalBinding;

/**
 * Thread that allows access to the embedded Batch or Task.
 * 
 * The need for this class arose when I (KHA) noticed that
 * a thread can not be restarted.
 * 
 * <p>
 * The target runs in a {@link CancelScope} of its own, {@link CancelScope#bind(CancelScope) bound}
 * to this thread. {@link #signalStop()} cancels that scope, which ends a wait the target runs as an
 * interruptible section (e.g. {@link CancelScope#sleep(long)}), and never interrupts the thread at
 * any other point: the target may be reading the database, and an interrupt there closes the
 * database file.
 * </p>
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class ScheduledThread extends Thread {

    /** The batch or task executed by this Thread */
	private final Batch target;

	/** The scope the {@link #getTarget() target} runs in. */
	private final CancelScope _cancelScope = new CancelScope();

    /**
     * Create a Thread to execute the given Batch or Task.
     */
    public ScheduledThread(Batch aTarget) {
        super(aTarget, aTarget.getName());
        this.target = aTarget;
        setDaemon(true);
    }

    /**
     * The {@link Batch} executed by this thread.
     */
    public Batch getTarget () {
        return target;
    }
    
     /** Overriden to record the start time at the Batch */
     public void start(long when) {
         target.started(when);
         super.start();
     }
     
	/**
	 * Runs the {@link #getTarget() target} with its {@link CancelScope} bound to this thread.
	 */
	@Override
	public void run() {
		try (ThreadLocalBinding<CancelScope>.Handle binding = CancelScope.bind(_cancelScope)) {
			super.run();
		}
	}

    /** 
     * Overriden to notifiy programmer of correct usage. 
     * 
     * @throws UnsupportedOperationException "Use start with millis" 
     */
    @Override
	public void start() {
        throw new UnsupportedOperationException("Use start with millis");
    }

    /** 
     * Reroute this to the Target.
     * 
     * Calls {@link Batch#signalStop()} on the target and then cancels the
     * {@link CancelScope} the target runs in.
     * 
     * @return false to indicate that you are not willing to stop 
     *          (Special wish from JCO)
     */
    public boolean signalStop() {
        boolean result = target.signalStop();
		_cancelScope.cancel();
        return result; 
    }


}
