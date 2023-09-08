/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;
import com.top_logic.util.monitor.ApplicationMonitor;
import com.top_logic.util.monitor.MonitorComponent;
import com.top_logic.util.monitor.MonitorMessage;
import com.top_logic.util.monitor.MonitorMessage.Status;
import com.top_logic.util.monitor.MonitorResult;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * Cleans up the temporary files directory.
 * 
 * Copied from <code>com.top_logic.knowledge.gui.pos.report.CleanUpThread</code> and transformed
 * from a Thread into a Task that can be run by the Scheduler.
 * 
 * @author <a href="mailto:fma@top-logic.com">Frank Mausz</a>
 *         (com.top_logic.knowledge.gui.pos.report.CleanUpThread)
 * @author Alice Scheerer
 */
public class CleanUpTask extends TaskImpl<CleanUpTask.Config> implements MonitorComponent {

	/**
	 * Typed configuration interface for {@link CleanUpTask}
	 * 
	 * @since 5.7.3
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends TaskImpl.Config<CleanUpTask> {

		/** Name of property {@link #getCleanupPath()}. */
		String CLEANUP_PATH_NAME = "cleanup-path";

		/** Name of property {@link #getTimeout()}. */
		String TIMEOUT = "timeout";

		/**
		 * The path to the directory to clean up.
		 */
		@Name(CLEANUP_PATH_NAME)
		String getCleanupPath();

		/**
		 * @see #getCleanupPath()
		 */
		void setCleanupPath(String value);

		/**
		 * Time in milliseconds that a file must stay untouched to eligible for deletion.
		 */
		@Name(TIMEOUT)
		@Format(MillisFormat.class)
		@LongDefault(DEFAULT_TIMEOUT)
		long getTimeout();

		/**
		 * @see #getTimeout()
		 */
		void setTimeout(long value);
	}

	/** Number of milliseconds of a minute. */
	private static final int MILLIS_PER_MINUTE = 60 * 1000;

    /* default age of files to delete */
	private static final int DEFAULT_TIMEOUT = 30 * MILLIS_PER_MINUTE;

    /** Maximum age of a File before it is deleted */
	protected long _timeout;

    /** The directory to clean up */
    protected File cleanUpDir;

	/** 
	 * Creates a {@link CleanUpTask}.
	 * 
	 * @param context    The context.
	 * @param config     The configuration.
	 */
	public CleanUpTask(InstantiationContext context, Config config) {
		super(context, config);
		// no setter for static variable to avoid ugly side effects

		String cleanupPath = config.getCleanupPath();
		try {
			this.cleanUpDir = createCleanupDir(cleanupPath);
			if (this.cleanUpDir != null && !this.cleanUpDir.exists()) {
				boolean wasMade = this.cleanUpDir.mkdir();
				if (!wasMade) {
					String thePath = this.cleanUpDir.getAbsolutePath();
					context.error("Failed to mkdir(" + thePath + ")");
				}
			}
			if ((this.cleanUpDir == null) || !this.cleanUpDir.isDirectory()) {
				context.error("'" + config.getCleanupPath() + "' is not a Directory");
			}
		} catch (IOException ex) {
			context.error("Unable to create cleanup dir '" + cleanupPath + "'", ex);
		}

		_timeout = config.getTimeout();
	}

	@Override
	protected void onAttachToScheduler() {
		super.onAttachToScheduler();

		ApplicationMonitor.getInstance().registerMonitor(getName(), this);
	}

	@Override
	protected void onDetachFromScheduler() {
		ApplicationMonitor.getInstance().unregisterMonitor(getName());

		super.onDetachFromScheduler();
	}

    /** 
     * Allow subclasses to create different kinds of directories for aPath.
     * 
     * @param aPath The value extracted from the #CLEANUP_PATH_NAME property. 
     */
    protected File createCleanupDir(String aPath) throws IOException {
        return FileManager.getInstance().getIDEFile(aPath);
    }

    /** 
     * Subclasse may wish to check the validity of this file on access
     */
    public File getCleanUpDir() {
        return cleanUpDir;
    }

	@Override
	public void checkState(MonitorResult result) {
		result.addMessage(newMonitorMessage());
	}

	private MonitorMessage newMonitorMessage() {
		Status type = this.getShouldStop() ? MonitorMessage.Status.ERROR : MonitorMessage.Status.INFO;
        StringBuffer message = new StringBuffer(256);  
        
        message.append(getCleanUpDir().getAbsolutePath());
		if (lastSched != SchedulingAlgorithm.NO_SCHEDULE) {
            message.append(" last: ");
            message.append(getLogFormat().format(new Date(lastSched)));
        }
		if (nextShed != SchedulingAlgorithm.NO_SCHEDULE) {
            message.append(" next: ");
            message.append(getLogFormat().format(new Date(nextShed)));
        }
		TaskResult result = getLog().getCurrentResult();
		if (result != null) {
			Resources resources = Resources.getInstance();
			message.append(' ');
			ResKey resultTypeI18N = result.getResultType().getMessageI18N();
			message.append(resources.getString(resultTypeI18N));
			message.append(": ");
			message.append(resources.getString(result.getMessage()));
        }
        
        return new MonitorMessage(type , message.toString(), this);
	}

    /** 
     * Return a Description for the MonitorComponent
     */
    @Override
	public String getDescription() {
        return "Cleanes up old files found in " + getCleanUpDir().getAbsolutePath();
    }

    /**
     * this cleans up the reporting directory.
     */
    @Override
	public void run () { 
    	super.run();

		getLog().taskStarted();

        try {
			int    theCount = this.performCleanup();

			ResKey encodedMessage;
			if (theCount > 0) {
				encodedMessage = I18NConstants.CLEANUP_SUCCESS__COUNT.fill(theCount);

				Logger.info(Resources.getSystemInstance()
					.decodeMessageFromKeyWithEncodedArguments(encodedMessage), this);
			} else {
				encodedMessage = I18NConstants.NOTHING_TO_CLEANUP;
            }

			getLog().taskEnded(ResultType.SUCCESS, encodedMessage);
		}
        catch (Exception ex) {
			getLog().taskEnded(ResultType.ERROR, ResultType.ERROR.getMessageI18N(), ex);
            Logger.error ("Error in cleanup",ex, this);                
        }           
    }

    /** Do the actually cleanup job */
    private int performCleanup ()  {
        File[] files = getCleanUpDir().listFiles();
        if (files == null) // empty dir, thats OK
            return 0;
        int numberOfDeletedFiles = 0;
        // files must be older than fileAge minutes to be deleted
		long compareTime = System.currentTimeMillis() - _timeout;
        for(int i=0;i<files.length;i++){
            File f = files[i];
            if(f.isFile() && f.lastModified() < compareTime && acceptDeleteHook(f)){
                if (f.delete()) {
                    numberOfDeletedFiles ++;
                } else {
                    Logger.info("Failed to cleanup " + f.getAbsolutePath(), this);
                }
				if (getShouldStop())
                    break;
            }
        }
        return numberOfDeletedFiles;                        
    }

 
    /**
	 * Ignores cvs ignore files.
	 * 
	 * @param  aFile A {@link File}
	 * @return Returns <code>false</code> if the file name is '.cvsignore',
	 *         <code>true</code> otherwise.
	 * @see com.top_logic.util.sched.CleanUpTask#acceptDeleteHook(java.io.File)
	 */
	public boolean acceptDeleteHook(File aFile) {
	    return !aFile.getName().equals(".cvsignore");
	}

	/** Format for the time stamp. */
	public static DateFormat getLogFormat() {
		return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	}

	
}