/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.logEntry;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.util.Resources;
import com.top_logic.util.monitor.ApplicationMonitor;
import com.top_logic.util.monitor.MonitorComponent;
import com.top_logic.util.monitor.MonitorMessage;
import com.top_logic.util.monitor.MonitorMessage.Status;
import com.top_logic.util.monitor.MonitorResult;
import com.top_logic.util.sched.CleanUpTask;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * This Task deletes {@link ArchiveUserDayEntry}s and referenced {@link LogEntry}s 
 * that exceed a configurable storing period.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class DayEntryCleanupTask<C extends DayEntryCleanupTask.Config<?>> extends TaskImpl<C>
		implements MonitorComponent {
	
	/**
	 * Typed configuration interface of {@link DayEntryCleanupTask}.
	 * 
	 * @since 5.7.3
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends DayEntryCleanupTask<?>> extends TaskImpl.Config<I> {
		
		/** 
		 * period of time. entries older than this will be deleted. 0	deactivates deletion.
		 */
		@StringDefault(DEFAULT_ARCHIVE_PERIOD)
		String getArchivePeriod();
	}
 
    /** default value of archive period */
    private static final String DEFAULT_ARCHIVE_PERIOD = "0M";
    /** the amount of the period, 0 means archive until the end of all times */
    private int periodAmount;
    /** the {@link Calendar} field for period calculation */
    private int periodField;
    
    /**
     * Constructor for the DayEntryCleanupTask which sets the properties.
     * 
     * @throws Exception when setup fails (Inherited form FileManager)
     */
    public DayEntryCleanupTask(Properties prop) throws Exception {
        super(prop);            
        
		String thePeriod = prop.getProperty("archivePeriod", DEFAULT_ARCHIVE_PERIOD);
		this.init(thePeriod);
    }
    
    /**
     * Init the tasks private parameters
     */
	private void init(String thePeriod) {
		Matcher theMatch = Pattern.compile("(?i)(\\d+)(\\w)").matcher(thePeriod);
        if (theMatch.find()) {
            this.periodAmount = Integer.parseInt(theMatch.group(1));
            this.periodField  = this.getCalendarField(theMatch.group(2).toUpperCase());
        }
	}

	/** 
	 * Creates a {@link DayEntryCleanupTask} based on the given configuration.
	 */
	public DayEntryCleanupTask(InstantiationContext context, C config) {
		super(context, config);
		init(config.getArchivePeriod());
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
	 * Convert a {@link String} into a {@link Calendar} field.
	 */
    private int getCalendarField(String aString) {
        
        if ("M".equals(aString)) {
            return (Calendar.MONTH); 
        }
        else if ("Y".equals(aString)) {
            return (Calendar.YEAR);
        }
        else if ("D".equals(aString)) {
            return (Calendar.DAY_OF_YEAR);
        }
        
        throw new IllegalArgumentException("'" + aString + "' cannot be mapped into a calendar field.");
    } 
    
    /**
     * This cleans up the DayUserEntries.
     */
    @Override
	public void run () { 
        super.run();
		getLog().taskStarted();
        try {
            int    theCount = this.performCleanUp();

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
            Logger.error ("Error in cleanup DayUserEntries",ex, this);                
        }
        
        System.gc();
    }
    
    protected int performCleanUp() {
    	return ThreadContext.inSystemContext(DayEntryCleanupTask.class, new Computation<Integer>() {

    	    @Override
			public Integer run() {
		        int res = 0;
				Lock tokenContext = null;
		        try{
		            // get token, we could be in a cluster           
		            // Get new context
					tokenContext =
						Lock.createLock(LockService.getInstance().getLockTimeout(), getTokenContextName());
		            
					if (tokenContext.tryLock()) {
		            // we get the token context, so we can execute the archive task
		                
		                res = cleanup();
		                tokenContext.unlock();
		            }
		            else{
		                Logger.info("Token context could not be locked for cleanup LogEntries.",this);
		            }
		            tokenContext = null;
		        }
		        finally{
		            // release token
		            if(tokenContext!=null){
		                try{
		                    tokenContext.unlock();
		                }
		                catch(Exception e){
		                    // ignore
		                }
		            }
		        }
		        return Integer.valueOf(res);
			}
    	}).intValue();
    }
    
    /**
     * Caller is responsible to check needed token contexts and for creation
     * and cleanup of TLContext
     * 
     * This method must be used to call this task embedded in another thread.
     * when using run, this must be done in an own thread
     *
     */
    public void runTaskExternally(){
        cleanup();
    }

    /** 
     * Perform the cleanup for all ArchiveUserDayEntry
     * 
     * @return number of deleted Events and LogEntries
     */
    protected int cleanup() {
        if (this.periodAmount == 0) {
            return 0;
        }

        KnowledgeBase theKB     = LogEntry.getDefaultKnowledgeBase();
        int           theResult = 0;

        try {
			Calendar theCal = CalendarUtil.createCalendar();
            theCal.add(this.periodField, -this.periodAmount);

            Set<LogEntry> theLogEntriesToDelete = new HashSet<>(20480);
            Set<LogEntry> theActiveEntries      = new HashSet<>(20480);
            
            // delete all archived entries older than the reference date
            List<Wrapper> theEntries = WrapperFactory.getWrappersByType(ArchiveUserDayEntry.KO_NAME, theKB);
            int  theSize    = theEntries.size();
            Date theRefDate = DateUtil.adjustToDayBegin(theCal);

            for (int thePos = 0; thePos < theSize; thePos++) {
                ArchiveUserDayEntry theEntry = (ArchiveUserDayEntry) theEntries.get(thePos);

                if (theRefDate.after(theEntry.getDate())) {
                    theLogEntriesToDelete.addAll(theEntry.getLogEntries());
                    
                    theEntry.tDelete();
                    theResult++;
                }
                else {
                    theActiveEntries.addAll(theEntry.getLogEntries());
                }
                
                if ((thePos%100) == 0 && !theKB.commit()) {
                    Logger.warn("Commit failed around entry " + thePos, this.getClass());
                }
            }
            
            if (!theKB.commit()) {
                Logger.warn("Last commit failed!", this.getClass());
            }
            else {
                Logger.info("Deleted " + theResult + " ArchiveUserDayEntries", this.getClass());
            }
        
            // delete all LogEntries referenced by the deleted archived entries, too
            theLogEntriesToDelete.removeAll(theActiveEntries);

            int thePos = 0;

            for (LogEntry theEntry : theLogEntriesToDelete) {
                thePos++;

                theEntry.tDelete();

                if ((thePos%100) == 0 && !theKB.commit()) {
                    Logger.warn("Commit failed around entry " + thePos, this.getClass());
                }
            }
            
            if (!theKB.commit()) {
                Logger.warn("Last commit failed!", this.getClass());
            }
            else {
                Logger.info("Deleted " + thePos + " LogEntries", this.getClass());
            }
            
            theResult += thePos;
        
        }
        catch (Exception ex) {
            Logger.error("failed to cleanup", ex, this);
        }
        return theResult;
    }

    String getTokenContextName() {
        return "DayEntryCleanupTask";
    }

	@Override
	public void checkState(MonitorResult result) {
		result.addMessage(newMonitorMessage());
	}

	private MonitorMessage newMonitorMessage() {
		Status type = this.getShouldStop() ? MonitorMessage.Status.ERROR : MonitorMessage.Status.INFO;
        StringBuffer message = new StringBuffer(256);  
        
        message.append(getName());
		if (lastSched != SchedulingAlgorithm.NO_SCHEDULE) {
            message.append(" last: ");
            message.append(CleanUpTask.getLogFormat().format(new Date(lastSched)));
        }
		if (nextShed != SchedulingAlgorithm.NO_SCHEDULE) {
            message.append(" next: ");
            message.append(CleanUpTask.getLogFormat().format(new Date(nextShed)));
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
    
	@Override
	public String getDescription() {
        return "Clean up LogEntries, removes ArchiveUserDayEntries and LogEntries that are older than a given period.";
    }
    
}
