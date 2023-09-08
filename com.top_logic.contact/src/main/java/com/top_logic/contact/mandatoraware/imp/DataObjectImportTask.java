/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.data.DOList;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.layout.progress.ProgressInfo;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
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
 * Generic Import Task for a List of DataObjects.
 *
 * TODO KHA move to TL when stable enough.
 *
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public abstract class DataObjectImportTask extends TaskImpl implements ProgressInfo, MonitorComponent, InContext {

    /** Initial (maximum) size of stringWriter. */
    protected static final int INITAL_LOGSIZE = 1024;

    /** Property name for an optional log file to be written. */
    public static final String LOG_FILE_NAME = "logFile";

    /** Total number of records being imported */
    protected int totalSize = 1;

    /** Current number of records already imported [0..totalSize] */
    protected int currentSize;

    /** Number of Records that where OK */
    protected int numberOK;

    /** Number of Records that had warnings */
    protected int numberWARN;

    /** Number of Records that had Errors */
    protected int numberERR;

    /** Set to true when Import was completed */
    protected boolean finished;

    /** Message to show to User via progressInfo */
    protected StringBuffer message = new StringBuffer(INITAL_LOGSIZE);

	/** One of {@link MonitorMessage} INFO/ERROR/FATAL (see {@link #checkState(MonitorResult)}). */
	protected Status monitorType;

    /** Base file name for logging to separate file (if configured by {@link #LOG_FILE_NAME}). */
    private String baseFileName;

    /** Log writer to be used, may be <code>null</code> if not configured by {@link #LOG_FILE_NAME}. */
    private PrintWriter logWriter;

    protected final boolean setTLContext;

    /**
     * Create a new none configured DataObjectImportTask.
     *
     * May be used by subclasses when importer is not used as task, bu8t directly.
     */
	protected DataObjectImportTask(String name) {
		super(name);
        setTLContext = true;
    }

    /**
	 * Create a new DataObjectImportTask from Properties, as used by Scheduler.
	 */
    public DataObjectImportTask(Properties prop) {
        super(prop);

        this.baseFileName = prop.getProperty(DataObjectImportTask.LOG_FILE_NAME);

        try {
	        this.setTLContext = ConfigUtil.getBooleanValue("setTLContext", prop.getProperty("setTLContext"), true);
        } catch (ConfigurationException e) {
        	throw new ConfigurationError("Invalid configuration", e);
        }
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
     * Override to return the List of DataObjects to import.
     *
     * @param aMandator The Mandator for which to import. May be <code>null</code> if not needed.
     */
    abstract protected List getImportObjects(Mandator aMandator) throws DatabaseAccessException, DataObjectException;

    /**
     * Override to validate that aMeta has all needed attributes.
     *
     * Should be called once for every different kind of MetaObject.
     *
     * @throws DataObjectException when format is not OK.
     */
    abstract protected void checkFormat(MetaObject aMeta) throws DataObjectException;

    /**
     * Import a single DataObject.
     */
    abstract protected void importItem(DataObject aDo, Mandator aMandator) throws Exception;

    // implementatio of Task

    /**
     * Setup
     */
    @Override
	public void run() {

        super.run();    // Make Scheduler happy by calling super.

		if (setTLContext) {
			TLContext.inSystemContext(getClass(), this);
		} else {
			inContext();
		}
    }

	@Override
	public void inContext() {
		startImport();
	}

    /**
     * Run on a single Mandator
     *
     * @param aMandator the Mandator
     * @param doRecurse if true recurse mandator structure
     */
	public void runOnMandator(final Mandator aMandator, final boolean doRecurse) {
    	super.run();

		if (setTLContext) {
			TLContext.inSystemContext(getClass(), new InContext() {
				@Override
				public void inContext() {
					startImport(aMandator, doRecurse);
				}
			});
		} else {
			startImport(aMandator, doRecurse);
		}
    }

	/**
	 * Start the import with
	 */
	protected abstract void startImport();

	protected void startImport(Mandator aMandator, boolean doRecurse) {
		if (this.shouldImport(aMandator)) {
			String addInfo = "";

			finished    = false;
	        monitorType = MonitorMessage.Status.INFO;
			File logFile = setupLogWriter();
			getLog().taskStarted();
			getLog().getCurrentResult().setLogFile(logFile);

	        try {
		        setupImport();
	            if (aMandator != null) {
					addInfo = "Mandator " + aMandator.getName();
	            }

				List<?> theList = getImportObjects(aMandator);
	                       totalSize      = theList.size();
	            MetaObject theMeta = null;
	            if (theList instanceof DOList) {
	                    // Special trick to support getCollectionType of DOList
	                theMeta        = ((DOList) theList).getCollectionType();
	                checkFormat(theMeta);
	            }
	            DataObject theDO = null;
				for (Iterator<?> iter = theList.iterator(); iter.hasNext() && !getShouldStop();) {
	            	try {
		                currentSize ++;
		                theDO    = (DataObject) iter.next();
		                MetaObject itemMeta = theDO.tTable();
		                if (itemMeta != theMeta) { // Check anew for every item in case DOList is lying.
		                    checkFormat(itemMeta);
		                }
		                importItem(theDO, aMandator);
	            	}
	            	catch (Exception ex) {
	            		logError("Failed to import item " + theDO, ex);
	            	}
	            }

	            importFinished(theList, aMandator);
				ResKey encodedMessage =
					I18NConstants.IMPORTED_SUCCESSFULLY__TOTAL_OK_WARN_ERROR_INFO.fill(totalSize, numberOK,
						numberWARN, numberERR, addInfo);

				getLog().taskEnded(ResultType.SUCCESS, encodedMessage);

				logInfo(
					Resources.getSystemInstance().decodeMessageFromKeyWithEncodedArguments(
						encodedMessage), null);
			} catch (Exception ex) {
				logError("Failed to run() " + addInfo, ex);
				getLog().taskEnded(ResultType.ERROR, ResultType.ERROR.getMessageI18N(), ex);
	        	monitorType = MonitorMessage.Status.ERROR;
			} finally {
	            tearDownImport();
	        }
		}

        if (doRecurse && aMandator != null) {
			for (StructuredElement theChild : aMandator.getChildren(null)) {
				this.startImport((Mandator) theChild, doRecurse);
			}
        }
	}

    // Core of DataObjectImportTask

	/**
	 * Hook that is called after all items have been passed to
	 * {@link #importItem(DataObject, Mandator)}.
	 */
    protected void importFinished(List theList, Mandator mandator) {
    	// Empty.
	}

	/**
     * Check if import should be done on this mandator
     *
     * @return true if import should be done here
     */
    protected boolean shouldImport(Mandator aMandator) {
		return true;
	}

	/**
     * Hook for subclasses do setup something before importing.
     *
     * This will call setupLogWriter() so be sure to call super.
	 * @throws Exception TODO
     */
    protected void setupImport() throws Exception {
        currentSize = 0;
        totalSize   = 1; // So ProgressInfo can show something ...
        numberOK    = 0;
        numberWARN  = 0;
        numberERR   = 0;
        message     = new StringBuffer(INITAL_LOGSIZE);
    }

    /**
	 * Set up the {@link #logWriter} if override to disable it if needed.
	 * 
	 * @return The log file. Must only be accessed via {@link #logWriter}.
	 */
	protected File setupLogWriter() {
        this.logWriter = null;

        if (!StringServices.isEmpty(this.baseFileName)) {
            String theName = this.baseFileName + "_" + DateUtil.toSortableString(new Date()) + ".txt";

            try {
                File theFile = FileManager.getInstance().getIDEFile(theName);
                this.logWriter = new PrintWriter(new FileWriter(theFile));

                Logger.info("Using log writer '" + theName + "' for import.", this);
				return theFile;
            }
            catch (IOException ex) {
                Logger.error("Unable to instanciate logWriter '" + theName + "', will be ignored", ex, this);
            }
        }
		return null;
    }

    /**
     * Hook for subclasses do tear down something after importing.
     *
     * This will set {@link #message} to the content (if not null) of the StringWriter.
     */
    protected void tearDownImport() {
        this.finished = true;

        if (this.logWriter != null) {
            try {
                this.logWriter.flush();
            }
            catch (Exception ex) {
                Logger.error("Unable to flush and close logWriter (will be ignored).", ex, this);
            }
            finally {
                try {
                    this.logWriter.close();
                }
                catch (Exception ex) {
                    // ignore this.
                }

                this.logWriter = null;
            }

            Logger.info("Finished import, log writer has been closed.", this);
        }
        // run garbage collection
        // things not persistent by now are unimportant
        System.gc();
    }

    /**
     * Log an Error to normal logger and (if configured) the logWriter.
     */
    protected void logError(String aMessage, Throwable anException) {
        String theMessage = "ERROR: " + aMessage;

        this.message.append(theMessage).append('\n');

        if (this.logWriter != null) {
            this.logToFile(theMessage, anException);
        }
        else {
            Logger.error(aMessage, anException, this);
        }
    }

    /**
     * Log a Warning to normal logger and (if configured) the logWriter.
     */
    protected void logWarn(String aMessage, Throwable anException) {
        String theMessage = "WARN: " + aMessage;

        this.message.append(theMessage).append('\n');

        if (this.logWriter != null) {
            this.logToFile(theMessage, anException);
        }
        else {
            Logger.warn(aMessage, anException, this);
        }
    }

    /**
     * Log some information to normal logger and (if configured) the logWriter.
     */
    protected void logInfo(String aMessage, Throwable anException) {
        String theMessage = "INFO: " + aMessage;

        this.message.append(theMessage).append('\n');

        if (this.logWriter != null) {
            this.logToFile(theMessage, anException);
        }
        else {
            Logger.info(aMessage, anException, this);
        }
    }

    private void logToFile(String aMessage, Throwable anExThrowable) {
        this.logWriter.print(DateUtil.getIso8601DateFormat().format(new Date()));
        this.logWriter.print(" ");
        this.logWriter.println(aMessage);
        this.logWriter.flush();
        
        if (anExThrowable != null) {
            anExThrowable.printStackTrace(this.logWriter);
        }
    }

	@Override
	public void checkState(MonitorResult result) {
		result.addMessage(newMonitorMessage());
	}

	private MonitorMessage newMonitorMessage() {
		StringBuffer theMessage = new StringBuffer(256);

        theMessage.append(getName());
		if (lastSched != SchedulingAlgorithm.NO_SCHEDULE) {
            theMessage.append(" Last: '");
            theMessage.append(DateUtil.getIso8601DateFormat().format(new Date(lastSched)));
            theMessage.append('\'');
        }
		if (nextShed != SchedulingAlgorithm.NO_SCHEDULE) {
            theMessage.append(" Next: '");
            theMessage.append(DateUtil.getIso8601DateFormat().format(new Date(nextShed)));
            theMessage.append('\'');
        }
		if (getShouldStop()) {
            theMessage.append(" shouldStop");
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

        return new MonitorMessage(monitorType , theMessage.toString(), this);
	}

	/**
	 * Describe this MonitorComponent by using the Tasks name.
	 */
    @Override
	public String getDescription() {
        return getName();
    }

    /**
     * @see ProgressInfo#isFinished()
     */
    @Override
	public boolean isFinished() {
        return finished;
    }

    /**
     * @see ProgressInfo#getCurrent()
     */
    @Override
	public long getCurrent() {
        return currentSize;
    }

    /**
     * @see ProgressInfo#getProgress()
     */
    @Override
	public float getProgress() {
        return 100.0f * currentSize / totalSize;
    }

    /**
	 * @see ProgressInfo#getExpected()
	 */
    @Override
	public long getExpected() {
        return totalSize;
    }


    /**
     * An (optional) message to show to user
     *
     * @see ProgressInfo#getMessage()
     */
    @Override
	public String getMessage() {
        if (this.message != null) {
            String theString = this.message.toString();

            this.message = new StringBuffer(INITAL_LOGSIZE);
            return theString;
        }
        return StringServices.EMPTY_STRING;
    }

    /**
     * Try using 1 second refresh (Import seems fast enough ..)
     *
     * @see ProgressInfo#getRefreshSeconds()
     */
    @Override
	public int getRefreshSeconds() {
        return MIN_REFRESH;
    }

    /**
     * Fix empty Strings by replacing them with null.
     */
    protected static String fixEmpty(String aString) {
        if (aString != null) {
            aString = aString.trim();
            if (aString.length() == 0) {
                aString = null;
            }
        }
        return aString;
    }
}
