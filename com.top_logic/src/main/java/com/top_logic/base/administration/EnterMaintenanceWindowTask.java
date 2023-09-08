/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.administration;

import java.util.Properties;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.util.sched.task.impl.TaskImpl;

/**
 * The EnterMaintenanceWindowTask waits a specified time and enters the maintenance window
 * mode afterwards.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class EnterMaintenanceWindowTask<C extends EnterMaintenanceWindowTask.Config<?>> extends TaskImpl<C> {

    /** The name of this Task. */
    public static final String TASK_NAME = "EnterMaintenanceWindowTask";

	/** Default delay (see {@link #delay}) */
	public static final long DEFAULT_DELAY = 5 * 60 * 1000;

    /** The time (in milliseconds) to wait before entering the maintenance window. */
	private long delay = DEFAULT_DELAY;


	/**
	 * Configuration interface for {@link EnterMaintenanceWindowTask}.
	 */
	public interface Config<I extends EnterMaintenanceWindowTask<?>> extends TaskImpl.Config<I> {

		/**
		 * The time (in milliseconds) to wait before entering the maintenance window.
		 */
		@LongDefault(DEFAULT_DELAY)
		long getDelay();
	}


    /**
     * Creates a new EnterMaintenanceWindowTask with a delay of five minutes.
     */
    public EnterMaintenanceWindowTask() {
        super(TASK_NAME);
        setRunOnStartup(false);

    }

    /**
     * Creates a new EnterMaintenanceWindowTask with the given delay.
     */
    public EnterMaintenanceWindowTask(long delay) {
        super(TASK_NAME);
        this.delay = (delay < 0 ? 0 : delay);
        setRunOnStartup(false);
    }

    /**
     * Creates a new EnterMaintenanceWindowTask with the given properties.
     *
     * @param aProp
     *        the properties configuring the task
     */
    public EnterMaintenanceWindowTask(Properties aProp) {
        super(aProp);
        setRunOnStartup(false);
        try {
            long theDelay = Long.parseLong(aProp.getProperty("delay", "0"));
            this.delay = (theDelay < 0 ? 0 : theDelay);
        }
        catch (NumberFormatException e) {
            Logger.warn("'" + aProp.getProperty("delay") + "' for property 'delay' is not a valid long.", e, this);
        }
    }


	/**
	 * Creates a new {@link EnterMaintenanceWindowTask}.
	 */
	public EnterMaintenanceWindowTask(InstantiationContext context, C config) {
		super(context, config);
		this.delay = config.getDelay();
	}


    /**
     * Gets the delay of this task.
     *
     * @return the delay as given within the constructor
     */
    public long getDelay() {
        return delay;
    }


    @Override
    public void run() {
        super.run(); // as wished by super class
        ThreadContext.inSystemContext(EnterMaintenanceWindowTask.class, new Computation<Void>() {
            @Override
			public Void run() {
                MaintenanceWindowManager.getInstance().enterMaintenanceWindow(getDelay());
                return null;
            }
        });
    }

}
