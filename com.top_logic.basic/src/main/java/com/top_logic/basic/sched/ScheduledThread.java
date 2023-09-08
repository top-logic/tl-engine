/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sched;

/**
 * Thread that allows access to the embedded Batch or Task.
 * 
 * The need for this class arose when I (KHA) noticed that
 * a thread can not be restarted.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class ScheduledThread extends Thread {

    /** The batch or task executed by this Thread */
	private final Batch target;

    /**
     * Create a Thread to execute the given Batch or Task.
     */
    public ScheduledThread(Batch aTarget) {
        super(aTarget, aTarget.getName());
        this.target = aTarget;
        setDaemon(true);
    }

    public Batch getTarget () {
        return target;
    }
    
     /** Overriden to record the start time at the Batch */
     public void start(long when) {
         target.started(when);
         super.start();
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
     * Calls target.signalStop() and than interrupts the Thread,
     * 
     * @return false to indicate that you are not willing to stop 
     *          (Special wish from JCO)
     */
    public boolean signalStop() {
        boolean result = target.signalStop();
        this.interrupt(); 
        return result; 
    }


}
