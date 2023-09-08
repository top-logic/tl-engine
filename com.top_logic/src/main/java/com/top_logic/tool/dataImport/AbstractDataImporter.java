/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport;

import java.util.Date;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.NoLock;
import com.top_logic.basic.Logger;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.progress.ProgressResult;

/**
 * The AbstractDataImporter is an abstract superclass for data importer which divides the
 * import process into the three steps preparing, parsing and committing.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public abstract class AbstractDataImporter {

    public static final String PREPARE_THREAD = "DataImporter_Preparing";
    public static final String PARSE_THREAD = "DataImporter_Parsing";
    public static final String COMMIT_THREAD = "DataImporter_Committing";

    /** Result key for unexpected error. Type: Throwable. */
    public static final String RESULT_ERROR = "RESULT_ERROR";

    /**
     * Prepare-Result key for import date. Type: Date. May be <code>null</code> to
     * indicate no date handling is required.
     */
    public static final String RESULT_IMPORT_DATE = "RESULT_IMPORT_DATE";

    /**
     * Prepare-Result key for indicator whether all import data is newer than the system data.
     * Type: Boolean.
     */
    public static final String RESULT_IMPORT_DATA_NEWER = "RESULT_IMPORT_DATA_NEWER";

    // The available status modes of the importer:
    public static final int MODE_IDLE        = 0;
    public static final int MODE_PREPARING   = 1;
    public static final int MODE_PREPARED    = 2;
    public static final int MODE_PARSING     = 3;
    public static final int MODE_PARSED      = 4;
    public static final int MODE_COMMITTING  = 5;
    public static final int MODE_COMMITTED   = 6;
    public static final int MODE_CLEANUP     = 7;


    /** Saves the mode in which the importer currently is.*/
    private int mode = MODE_IDLE;

    /** Saves the results of last preparing. */
    private ProgressResult prepareResult;

    /** Saves the results of last parsing. */
    private ProgressResult parseResult;

    /** Saves the results of last committing. */
    private ProgressResult commitResult;

    /** Saves the progress info. */
    protected final DefaultProgressInfo progress = new DefaultProgressInfo();

    /** Holds all log entries of the current progress. */
    protected LogBook log;


    /** Saves the date of the last import run (parsing or committing) since system startup. */
    private Date lastImportRun;

    /** Saves the overall result message of the last import run since system startup. */
    private String lastImportResult;

    /** Holds the messages of the last import run since system startup. */
    private StringBuffer lastImportProtocol;



    /**
     * Creates a new instance of this class.
     */
    protected AbstractDataImporter() {
        lastImportProtocol = new StringBuffer();
    }



    /**
     * Gets the progress info.
     */
    public DefaultProgressInfo getProgressInfo() {
        return progress;
    }

    /**
     * Gets the mode in which the importer currently is.
     */
    public int getMode() {
        return mode;
    }

    /**
     * Checks whether the importer is currently running and doing something.
     */
    public boolean isInActiveMode() {
        return mode == MODE_PREPARING || mode == MODE_PARSING || mode == MODE_COMMITTING || mode == MODE_CLEANUP;
    }



    /**
     * Gets the time of last importer run (parsing or committing) since system startup,
     * regardless whether successful or not.
     */
    public Date getLastImportRun() {
        return lastImportRun;
    }

    /**
     * Gets the time of last importer run (parsing or committing) since system startup,
     * regardless whether successful or not.
     */
    public String getLastImportResult() {
        return lastImportResult;
    }

    /**
     * Gets the protocol of last importer run since system startup,
     * regardless whether successful or not.
     */
    public String getLastImportProtocol() {
        return lastImportProtocol.toString();
    }

    /**
     * Gets the time stamp of the data in the system as Date.
     * May be <code>null</code> if no import was done ever.
     */
    public Date getSystemDataTimestamp() {
        return null;
    }

    /**
	 * Gets a new SimpleTokenContextHandler for locking the importer for exclusive use.
	 */
	public Lock getNewTokenHandler() {
		return new NoLock();
	}

    /**
     * Gets the resource prefix for message internationalization.
     */
	protected ResPrefix getResPrefix() {
		return I18NConstants.DATA_IMPORT;
    }



    /**
     * Checks whether the preparing can be started.
     */
    public boolean canStartPreparing() {
        try { checkCanStartPreparing(); return true; }
        catch (Exception e) { return false; }
    }

    /**
     * Checks whether the parsing can be started.
     */
    public boolean canStartParsing() {
        try { checkCanStartParsing(); return true; }
        catch (Exception e) { return false; }
    }

    /**
     * Checks whether the committing can be started.
     */
    public boolean canStartCommitting() {
        try { checkCanStartCommitting(); return true; }
        catch (Exception e) { return false; }
    }

    /**
     * Checks whether the cleanup can be started.
     */
    public boolean canStartCleanUp() {
        try { checkCanStartCleanUp(); return true; }
        catch (Exception e) { return false; }
    }

    /**
     * Checks whether a cleanup can be started and there is something to cleanUp.
     */
    public boolean needCleanUp() {
        return canStartCleanUp() && mode > MODE_PREPARED; //MODE_IDLE;
    }



    /**
     * Checks whether the preparing can be started.
     *
     * @throws IllegalStateException
     *         if preparing cannot be started at the moment
     */
    protected void checkCanStartPreparing() throws IllegalStateException {
        if (isInActiveMode()) throw new IllegalStateException("The importer is currently busy.");
    }

    /**
     * Checks whether the parsing can be started.
     *
     * @throws IllegalStateException
     *         if parsing cannot be started at the moment
     */
    protected void checkCanStartParsing() throws IllegalStateException {
        if (isInActiveMode()) throw new IllegalStateException("The importer is currently busy.");
        if (mode != MODE_PREPARED) throw new IllegalStateException("The importer is not prepared yet.");
        if (prepareResult == null || !prepareResult.isSuccess()) throw new IllegalStateException("The importer preparing failed.");
    }

    /**
     * Checks whether the committing can be started.
     *
     * @throws IllegalStateException
     *         if committing cannot be started at the moment
     */
    protected void checkCanStartCommitting() throws IllegalStateException {
        if (isInActiveMode()) throw new IllegalStateException("The importer is currently busy.");
        if (mode != MODE_PARSED) throw new IllegalStateException("The importer has not parsed the input yet.");
        if (parseResult == null || !parseResult.isSuccess()) throw new IllegalStateException("The importer parsing failed.");
    }

    /**
     * Checks whether the cleanup can be started.
     *
     * @throws IllegalStateException
     *         if cleanUp cannot be started at the moment
     */
    protected void checkCanStartCleanUp() throws IllegalStateException {
        if (isInActiveMode()) throw new IllegalStateException("The importer is currently busy.");
    }



    /**
     * Prepares the import.
     *
     * @return the result of the prepare, which can got by the getPrepareResult() method also.
     * @exception IllegalStateException if {@link #canStartPreparing()} returns <code>false</code>
     */
    public final ProgressResult prepareImport() {
        checkCanStartPreparing();
        if (mode > MODE_PREPARED) {
            cleanUpImporter();
        }
        mode = MODE_PREPARING;
        prepareResult = parseResult = commitResult = null;
        progress.reset();
        log = new LogBook(getResPrefix(), progress, this);
        try {
            doPrepareImport();
        }
        catch (Throwable t) {
            log.getResult().setValue(RESULT_ERROR, t);
            log.fatal("prepare.fatalError", t);
        }
        prepareResult = log.getResult();
        log = null;
        mode = MODE_PREPARED;
        progress.setFinished(true);
        return prepareResult;
    }

    /**
     * Parses the import data ("simulates" the import).
     *
     * @return the result of the parse, which can got by the getParseResult() method also.
     * @exception IllegalStateException if {@link #canStartParsing()} returns <code>false</code>
     */
    public final ProgressResult parseImport() {
        checkCanStartParsing();
        mode = MODE_PARSING;
        parseResult = commitResult = null;
        progress.reset();
        log = new LogBook(getResPrefix(), progress, this);
        boolean error = false;
        try {
            doParseImport();
        }
        catch (Throwable t) {
            error = true;
            log.getResult().setValue(RESULT_ERROR, t);
            log.fatal("parse.fatalError", t);
        }
        parseResult = log.getResult();
        lastImportRun = new Date();
        lastImportResult = log.getResources().getString(getResPrefix().key((error ? "result.parseFatalError" : parseResult.isSuccess() ? "result.notFinished" : "result.parseError")));
        lastImportProtocol = new StringBuffer();
        lastImportProtocol.append("\n\n\n").append(log.getResources().getString(getResPrefix().key("parseLog")));
        lastImportProtocol.append("\n\n").append(progress.getMessage());
        log = null;
        mode = MODE_PARSED;
        progress.setFinished(true);
        return parseResult;
    }

    /**
     * Commits the import (really create and manipulates objects).
     *
     * @return the result of the commit, which can got by the getCommitResult() method also.
     * @exception IllegalStateException if {@link #canStartCommitting()} returns <code>false</code>
     */
    public final ProgressResult commitImport() {
        checkCanStartCommitting();
        mode = MODE_COMMITTING;
        commitResult = null;
        progress.reset();
        log = new LogBook(getResPrefix(), progress, this);
        boolean error = false;
        try {
            doCommitImport();
        }
        catch (Throwable t) {
            error = true;
            log.getResult().setValue(RESULT_ERROR, t);
            log.fatal("commit.fatalError", t);
        }
        commitResult = log.getResult();
        lastImportRun = new Date();
        lastImportResult = log.getResources().getString(getResPrefix().key((error ? "result.fatalError" : commitResult.isSuccess() ? "result.success" : "result.error")));
        lastImportProtocol.append("\n\n\n").append(log.getResources().getString(getResPrefix().key("commitLog")));
        lastImportProtocol.append("\n\n").append(progress.getMessage());
        log = null;
        mode = MODE_COMMITTED;
        progress.setFinished(true);
        return commitResult;
    }

    /**
     * Does cleanup work after an import.
     *
     * @exception IllegalStateException if {@link #canStartCleanUp()} returns <code>false</code>
     */
    public final void cleanUpImporter() {
        checkCanStartCleanUp();
        mode = MODE_CLEANUP;
        try {
            doCleanUpImporter();
        }
        catch (Throwable t) {
            Logger.error("Failed to cleanUp importer.", t, this);
        }
        prepareResult = parseResult = commitResult = null;
        progress.reset();
        mode = MODE_IDLE;
    }

    /**
     * Does cleanup work if it is necessary.
     */
    public final void cleanUpIfNecessary() {
        if (needCleanUp()) cleanUpImporter();
    }



    /**
     * This method returns the result of the last preparing.
     *
     * @return the result of the last preparing or <code>null</code>, if the importer
     *         wasn't prepared yet or if a cleanup was done since last preparing.
     */
    public ProgressResult getPrepareResult() {
        return prepareResult;
    }

    /**
     * This method returns the result of the last parsing.
     *
     * @return the result of the last parsing or <code>null</code>, if the importer
     *         hasn't parsed yet or if a cleanup was done since last parsing.
     */
    public ProgressResult getParseResult() {
        return parseResult;
    }

    /**
     * This method returns the result of the last committing.
     *
     * @return the result of the last committing or <code>null</code>, if the importer
     *         hasn't committed yet or if a cleanup was done since last committing.
     */
    public ProgressResult getCommitResult() {
        return commitResult;
    }



    /**
     * In this method the import gets prepared, e.g. it gets checked whether all required
     * files / connections are available, etc. In addition, the date of the importer data
     * is stored in the result under the {@link #RESULT_IMPORT_DATE} key, and the
     * {@link #RESULT_IMPORT_DATA_NEWER} flag is set.
     *
     * Note: This method is only there to check whether the importer can be started, it is
     * NOT there to allocate resources already!
     */
    protected abstract void doPrepareImport();

    /**
     * In this method the import gets "simulated", e.g. it gets checked whether Import data
     * is correct, and information is collected about which objects have to be created /
     * updated / removed.
     */
    protected abstract void doParseImport();

    /**
     * In this method the import gets done. The objects collected in the doParseImport
     * method gets now really created / updated / removed and committed.
     */
    protected abstract void doCommitImport();


    /**
     * In this method cleanUp work gets done. This includes removing results of parsing and
     * committing and releasing potentially reserved resources.
     */
    protected abstract void doCleanUpImporter();

}
