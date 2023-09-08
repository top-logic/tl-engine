/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport;

import java.util.Date;
import java.util.Properties;

import com.top_logic.base.locking.Lock;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.thread.InContext;
import com.top_logic.layout.progress.ProgressResult;
import com.top_logic.util.TLContext;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * A task for automatic data import.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class AutomaticDataImportTask<C extends AutomaticDataImportTask.Config<?>> extends TaskImpl<C>
		implements InContext {

    /** The name of this Task. */
    public static final String TASK_NAME = "Automatic DataImportTask";


    /** XML property for the {@link #importer} attribute */
    public static final String PROPERTY_IMPORTER = "importer";

    /** XML property for the {@link #abortOnWarnings} attribute */
    public static final String PROPERTY_ABORT_ON_WARNINGS = "abortOnWarnings";

    /** XML property for the {@link #abortOnErrors} attribute */
    public static final String PROPERTY_ABORT_ON_ERRORS = "abortOnErrors";


    /** Flag indicating that the import should not be run on warnings, even if possible. */
    protected boolean abortOnWarnings;

    /** Flag indicating that the import should not be run on errors, even if possible. */
    protected boolean abortOnErrors;

    /** Stores the importer. */
    protected AbstractDataImporter importer;

    
	/**
	 * Configuration interface for {@link AutomaticDataImportTask}.
	 */
	public interface Config<I extends AutomaticDataImportTask<?>> extends TaskImpl.Config<I> {

		/**
		 * The {@link AbstractDataImporter} to use by this task.
		 */
		PolymorphicConfiguration<AbstractDataImporter> getImporter();

		/**
		 * Setter for {@link #getImporter()}.
		 */
		void setImporter(PolymorphicConfiguration<AbstractDataImporter> value);

		/**
		 * Flag indicating that the import should not be run on errors, even if possible.
		 */
		@BooleanDefault(true)
		boolean getAbortOnErrors();

		/**
		 * Flag indicating that the import should not be run on warnings, even if possible.
		 */
		@BooleanDefault(false)
		boolean getAbortOnWarnings();
	}


    /**
     * Creates a new instance of this class with the dummy importer.
     */
    public AutomaticDataImportTask() {
        super(TASK_NAME);
        setRunOnStartup(false);
        abortOnWarnings = false;
        abortOnErrors = true;
        importer = DummyDataImporter.INSTANCE;
        Logger.info(TASK_NAME + " without property configuration created.", AutomaticDataImportTask.class);
    }

	/**
	 * Creates a new {@link AutomaticDataImportTask}.
	 */
	public AutomaticDataImportTask(InstantiationContext context, C config) {
		super(context, config);
		importer = context.getInstance(config.getImporter());
		abortOnErrors = config.getAbortOnErrors();
		abortOnWarnings = config.getAbortOnWarnings();
	}

    /**
     * Creates a new instance of this class with the given properties.
     *
     * @param aProp
     *        the properties of this task.
     */
    public AutomaticDataImportTask(Properties aProp) {
        super(aProp);
        setRunOnStartup(false);
        String theValue = aProp.getProperty(PROPERTY_ABORT_ON_WARNINGS);
        abortOnWarnings = theValue == null ? false : theValue.trim().equalsIgnoreCase("true");
        theValue = aProp.getProperty(PROPERTY_ABORT_ON_ERRORS);
        abortOnErrors = theValue == null ? true : theValue.trim().equalsIgnoreCase("true");

        theValue = aProp.getProperty(PROPERTY_IMPORTER);
        try {
            importer = ConfigUtil.getSingletonMandatory(AbstractDataImporter.class, PROPERTY_IMPORTER, theValue);
            Logger.info(TASK_NAME + " '" + getName() + "' created.", AutomaticDataImportTask.class);
        }
        catch (ConfigurationException ex) {
            Logger.error("Failed to get singleton instance of configured importer: " + theValue, ex, AutomaticDataImportTask.class);
        }
    }



    /**
     * This task can only be stopped on some points, not every time.
     * Therefore it tries to check the signalStop attribute, but returns <code>false</code>.
     */
    @Override
	public boolean signalStopHook() {
        return false;
    }



    /**
     * Runs the automatic data import.
     */
    @Override
	public void run() {
        super.run(); // as wished by super class
		getLog().taskStarted();

        long startTime = System.currentTimeMillis();
        Logger.info("Starting automatic data import...", AutomaticDataImportTask.class);

        try {
            if (importer.isInActiveMode()) {
                Logger.warn("Automatic data import skipped because the data importer is already in use.", AutomaticDataImportTask.class);
                logDuration(startTime);
				getLog().taskEnded(ResultType.SUCCESS, I18NConstants.DATA_IMPORT_IN_USE);
                return;
            }

			TLContext.inSystemContext(importer.getClass(), this);
			getLog().taskEnded(ResultType.SUCCESS, ResultType.SUCCESS.getMessageI18N());
        } catch (Throwable t) {
            Logger.error("Automatic data import terminated with an unexpected error.", t, AutomaticDataImportTask.class);
			getLog().taskEnded(ResultType.ERROR, I18NConstants.DATA_IMPORT_ERROR.fill(t.getMessage()), t);
        } finally {
            logDuration(startTime);
            finalizeTask();
        }
    }

	@Override
	public void inContext() {
		TLContext.pushSuperUser();
		try {
			Lock lock = importer.getNewTokenHandler();
			if (!lock.tryLock()) {
				Logger.warn("Automatic data skipped because acquiring token context failed.",
					AutomaticDataImportTask.class);
				return;
			}
			try {
				beforeImport(lock);
				runImport(lock);
				if (lock.check())
					importer.cleanUpIfNecessary();
				if (getShouldStop())
					return;
				if (lock.check()) {
					afterImport(lock);
				}
				else {
					Logger.error("Automatic data token context timed out while importer was running.",
						AutomaticDataImportTask.class);
				}
			} finally {
				lock.unlock();
			}
		} finally {
			TLContext.popSuperUser();
		}
	}


    /**
     * Does the import.
     */
	protected void runImport(Lock lock) {
        // Prepare import
		if (checkShouldStop() || !lock.renew()) {
			return;
		}
        ProgressResult theResult = importer.prepareImport();
        if (!theResult.isSuccess()) {
            Logger.error("Automatic data import could not start because of fatal errors while preparing the import.", AutomaticDataImportTask.class);
            return;
        }
        if (abortOnErrors && theResult.hasErrors()) {
            Logger.warn("Automatic data import skipped because of errors while preparing the import.", AutomaticDataImportTask.class);
            return;
        }
        if (abortOnWarnings && theResult.hasWarnings()) {
            Logger.warn("Automatic data import skipped because of warnings while preparing the import.", AutomaticDataImportTask.class);
            return;
        }
        Date theSystemDate = importer.getSystemDataTimestamp();
        Date theImportDate = (Date)theResult.getValue(AbstractDataImporter.RESULT_IMPORT_DATE);
        if (theSystemDate != null && theImportDate != null && theImportDate.before(theSystemDate)) {
            Logger.info("Automatic data import skipped because the import files are older than system data.", AutomaticDataImportTask.class);
            return;
        }
        Boolean allNewer = (Boolean)theResult.getValue(AbstractDataImporter.RESULT_IMPORT_DATA_NEWER);
        if (allNewer != null && !allNewer.booleanValue()) {
            Logger.info("Automatic data import skipped because some of the import files are older than system data.", AutomaticDataImportTask.class);
            return;
        }

        // Parse import files
		if (checkShouldStop() || !lock.renew()) {
			return;
		}
        theResult = importer.parseImport();
        if (!theResult.isSuccess()) {
            Logger.error("Automatic data import could not import data because of fatal errors in the import data files.", AutomaticDataImportTask.class);
            return;
        }
        if (abortOnErrors && theResult.hasErrors()) {
            Logger.warn("Automatic data import skipped because of errors in the import data files.", AutomaticDataImportTask.class);
            return;
        }
        if (abortOnWarnings && theResult.hasWarnings()) {
            Logger.warn("Automatic data import skipped because of warnings in the import data files.", AutomaticDataImportTask.class);
            return;
        }

        // Commit import
		if (checkShouldStop() || !lock.renew()) {
			return;
		}
        theResult = importer.commitImport();
        if (theResult.isSuccess()) {
            Logger.info("Automatic data import was completed successfully.", AutomaticDataImportTask.class);
        }
        else {
            Logger.error("Automatic data import failed.", AutomaticDataImportTask.class);
        }

    }

    /**
     * Hook for subclasses to do something before import.
     */
    protected void beforeImport(Lock aToken) {
        // nothing to do here
    }

    /**
     * Hook for subclasses to do something after import.
     */
    protected void afterImport(Lock aToken) {
        // nothing to do here
    }

    /**
     * Code to be executed as last action of this task.
     * Always called last in run() method.
     */
    protected void finalizeTask(){
        // nothing to do here
    }



    /**
     * Checks whether this task was aborted.
     *
     * @return <code>true</code>, if the task was aborted, <code>false</code> otherwise
     */
    protected boolean checkShouldStop() {
		if (getShouldStop()) {
            Logger.info("Automatic data import was aborted without committing anything.", AutomaticDataImportTask.class);
            return true;
        }
        return false;
    }

    /**
     * Logs the task runtime duration.
     *
     * @param startTime
     *        the time on which the task started
     */
    protected void logDuration(long startTime) {
        Logger.info("Automatic data import duration: " + DebugHelper.getTime(System.currentTimeMillis() - startTime), AutomaticDataImportTask.class);
    }

}
