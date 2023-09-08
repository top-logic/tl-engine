/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.administration;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;


/**
 * Timer for the MaintenanceWindowManager.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class MaintenanceWindowTimer extends Thread {

    /** The name of this Thread. */
    public static final String THREAD_NAME = "MaintenanceWindowTimer";

    /** The time after that the maintenance mode shall be entered. */
    private final long delay;

    /** Time when this timer was started. */
    private long started = 0;


    /**
     * Creates a new instance of this class.
     */
    public MaintenanceWindowTimer(long milliseconds) {
        super(THREAD_NAME);
        this.delay = milliseconds;
    }


    @Override
    public void run() {
        ThreadContext.inSystemContext(MaintenanceWindowTimer.class, new Computation<Void>() {
            @Override
			public Void run() {
                doRun();
                return null;
            }
        });
    }


    /**
     * Waits the given amount of time and enters the maintenance window mode afterwards.
     */
    public void doRun() {
		MaintenanceWindowManager mwm = MaintenanceWindowManager.getInstance();
        started = System.currentTimeMillis();
        try {
            Thread.sleep(delay);
            Person contextUser = mwm.getChangingUser();
            if (contextUser != null) {
                TLContext context = TLContext.getContext();
                if (context != null) context.setCurrentPerson(contextUser);
            }
            mwm.timeUp();
        }
        catch (InterruptedException e) {
			// Most Propably triggered by MaintenanceWindowManager.shutdown();
			mwm.forgetMe(this);
        }
    }


    /**
     * Returns the time left until the maintenance window mode will be entered.<br/>
     * Note: the value returned by this method may be inaccurate for few milliseconds due to
     * system speed and error of measurement.
     *
     * @return the time left until the maintenance window mode will be entered, if the task
     *         is running and is currently waiting for the delay; a negative value if the
     *         task wasn't started yet or if it is already finished
     */
    public long getTimeLeft() {
        return delay - (System.currentTimeMillis() - started);
    }

    /**
     * Returns the time when the maintenance window mode will be entered.<br/>
     * Note: the value returned by this method may be inaccurate for few milliseconds due to
     * system speed and error of measurement.
     *
     * @return the time when the maintenance window mode will be entered, if the task is
     *         running and is currently waiting for the delay, a negative value if the task
     *         wasn't started yet.
     */
    public long getFinishedTime() {
        return started <= 0 ? -1 : started + delay;
    }

}
