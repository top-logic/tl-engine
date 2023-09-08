/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import java.util.Calendar;
import java.util.Properties;

import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Example of a {@link Task}, used for Testing
 *
 * @author    <a href="mailto:klaus halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class ExampleTask extends TaskImpl {
    
    /** Result used for signalStop, default true. */
    boolean allowStop = true;
    
    /** will be set when thread was interrupted. */
    boolean interrupted = false;

    /** Time in millis this class will sleep */
    long    snork = 100;

    /** number of time run() was called */
    int     runTimes = 0;
 
    /** number of time run() was called */
    volatile boolean currentlyRunning = false;

	private Calendar _fakedTime = null;

	private Object _fakedTimeSync = new Object();
    
    /** 
     * Allow access to superclass CTor,
     */
	public ExampleTask(String name) {
		super(name);
    }

    /** 
     * Construct a new ExampleTask to be run periodically.
     */
    public ExampleTask(String aName, 
                        int  aDaytype, int aDaymask, 
                        int  aStartHour, int aStartMinute,
                        long anInterval,           
                        int  aStopHour, int aStopMinute,
                        long aSleep) {
        super(aName,
            aDaytype,    aDaymask,
            aStartHour,  aStartMinute,
            anInterval,
            aStopHour,   aStopMinute);
        snork = aSleep;
     }

    public ExampleTask(Properties prop) {
        super(prop);
    }

    /**
     * Make super CTor acessible
     * 
     * @param aSleep time that this class will sleep in run()
     */
    public ExampleTask(String aName,
        int aDaytype, int aDaymask,
        int anHour,   int aMinute ,
        long aSleep) {
        super(aName, aDaytype, aDaymask, anHour, aMinute);
        snork = aSleep;
    }

	/** Uses a clone of the given {@link Calendar} as 'now'. null means: Use real time. */
	public void setFakedTime(Calendar now) {
		synchronized (_fakedTimeSync) {
			if (now == null) {
				_fakedTime = null;
			} else {
				_fakedTime = (Calendar) now.clone();
			}
		}
	}

    /**
     * false to indicate that you are not willing to stop 
     *          (Special wish from JCO)
     */
    @Override
	public boolean signalStopHook() {
		return allowStop;
    }
    
    /** Allow access to shouldStop */
	@Override
	public synchronized boolean getShouldStop() {
		return super.getShouldStop();
    }
    
    /**
     * Acessor for allowStop.
     */
    public void setAllowStop(boolean aAllowStop) {
        this.allowStop = aAllowStop;
    }
    
    /** Allow access to runTimes */
	public synchronized int getRunTimes() {
        return runTimes;   
    }
    
    public boolean isRunning() {
    	return currentlyRunning;
    }


    /** Just wait a bit for iterruption */
    @Override
	public void run() {
    	currentlyRunning = true;
		getLog().taskStarted();
    	try {
			synchronized (_fakedTimeSync) {
				if (_fakedTime == null) {
					super.run();
				} else {
					updateTimes(_fakedTime.getTimeInMillis());
				}
			}
    		try {
				synchronized (this) {
					runTimes++;
				}
    			Thread.sleep(snork);
    		} catch (InterruptedException e) {
    			interrupted = true;
    		}   
			getLog().taskEnded(ResultType.SUCCESS, ResultType.SUCCESS.getMessageI18N());
		} catch (Throwable ex) {
			getLog().taskEnded(ResultType.ERROR, ResultType.ERROR.getMessageI18N(), ex);
    	} finally {
    		currentlyRunning = false;
    	}
    }

}
