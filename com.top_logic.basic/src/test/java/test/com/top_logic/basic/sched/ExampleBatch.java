/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sched;

import com.top_logic.basic.sched.BatchImpl;

/**
 * Example of a batch, used for Testing
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class ExampleBatch extends BatchImpl {
    
    /** Result used for signalStop, default true. */
    boolean allowStop = true;
    
    /** will be set when thread was interrupted. */
    boolean interrupted = false;

    /** Time in millis this class will sleep */
    long    snork = 100;
    
	/**
	 * Create ExampleBatch with given name.
	 * 
	 * @param aName
	 *        Must not be <code>null</code> or empty. Has to be unique.
	 */
    public ExampleBatch(String aName) {
        super(aName);   
    }

	/**
	 * Create ExampleBatch with given name and sleep time.
	 * 
	 * @param aName
	 *        Must not be <code>null</code> or empty. Has to be unique.
	 */
    public ExampleBatch(String aName, long sleep) {
        super(aName);   
        snork = sleep;
    }

    /**
     * false to indicate that you are not willing to stop 
     *          (Special wish from JCO)
     */
    @Override
	public synchronized boolean signalStop() {
        return super.signalStop() && allowStop;
    }
    
    /** Allow access to shouldStop */
	@Override
	public synchronized boolean getShouldStop() {
		return super.getShouldStop();
    }
    
    /** Just wait a bit for iterruption */
    @Override
	public void run() {
        try {
            Thread.sleep(snork);
        } catch (InterruptedException e) {
            interrupted = true;
        }   
    }

}
