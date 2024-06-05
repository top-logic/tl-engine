/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.thread.InContext;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.genericimport.GenericDataImportConfiguration.UpdateException;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericConverter;
import com.top_logic.element.genericimport.interfaces.GenericCreateHandler;
import com.top_logic.element.genericimport.interfaces.GenericDataImportConfigurationAware;
import com.top_logic.element.genericimport.interfaces.GenericImporter;
import com.top_logic.element.genericimport.interfaces.GenericTypeResolver;
import com.top_logic.element.genericimport.interfaces.GenericUpdateHandler;
import com.top_logic.element.genericimport.interfaces.GenericValidator;
import com.top_logic.element.genericimport.interfaces.GenericValidator.ValidationResult;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.knowledge.objects.CreateException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.progress.ProgressInfo;
import com.top_logic.util.TLContext;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.schedule.legacy.LegacySchedulesCommon;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GenericDataImportTask extends TaskImpl implements ProgressInfo, GenericDataImportConfigurationAware,
		InContext {

    public static final String CONFIG_FILE = "configFile";

    private static final char NEWLINE = '\n';

    private KnowledgeBase knowledgeBase;

    private GenericDataImportConfiguration config;
    private LoggerProgressInfo log;
    /**
     * variables for ProgressInfo
     */
    private long expected = 1;

    private long updated;
    private long created;
    private long errors;
    private long invalid;
    private long ignored;

    private List plainDataObjects;

    /**
     * Creates a {@link GenericDataImportTask}.
     *
     */
    public GenericDataImportTask(String aName) {
        super(aName, LegacySchedulesCommon.ONCE, 0, 0, 0);
    }

    /**
     * Creates a {@link GenericDataImportTask}.
     */
    public GenericDataImportTask(Properties aProp) {
        super(aProp);

        String theFilename = aProp.getProperty(CONFIG_FILE);
        if (StringServices.isEmpty(theFilename)) {
            throw new IllegalArgumentException("Property " + CONFIG_FILE + " not configured!");
        }

        try {
            this.setImportConfiguration(GenericDataImportConfiguration.readConfiguration(theFilename), null);
        } catch (IOException ioex) {
            Logger.error("Failed to read configuration from file '"+theFilename + "'", ioex, this);
            throw new RuntimeException(ioex);
        }
    }

    /**
     * This method returns the commitInterval.
     *
     * @return    Returns the commitInterval.
     */
    public int getCommitInterval() {
        return this.config.getCommitInterval();
    }

    /**
     * This method returns the doCommit.
     *
     * @return    Returns the doCommit.
     */
    public boolean isDoCommit() {
        return this.config.isDoCommit();
    }

    /**
     * This method returns the importer.
     *
     * @return    Returns the importer.
     */
    private GenericImporter getImporter() {
        return this.config.getImporter();
    }

    /**
     * This method returns the cache.
     *
     * @return    Returns the cache.
     */
    private GenericCache getCache() {
        return this.config.getCache();
    }

    /**
     * This method returns the doUpdate.
     *
     * @return    Returns the doUpdate.
     */
    public boolean isDoUpdate() {
        return this.config.isDoUpdate();
    }

    /**
     * This method returns the doCreate.
     *
     * @return    Returns the doCreate.
     */
    public boolean isDoCreate() {
        return this.config.isDoCreate();
    }

    private String getForeignKeyAttribute(String aType) {
        return this.config.getForeignKey(aType);
    }

    private GenericConverter getConverter(String aType) {
        return this.config.getConverter(aType);
    }

    private GenericValidator getValidator(String aType) {
        return this.config.getValidator(aType);
    }

    private GenericUpdateHandler getUpdateHandler(String aType) {
        return this.config.getUpdateHandler(aType);
    }

    private GenericCreateHandler getCreateHandler(String aType) {
        return this.config.getCreateHandler(aType);
    }

    private GenericTypeResolver getTypeResolver() {
        return this.config.getTypeResolver();
    }

    @Override
	public GenericDataImportConfiguration getImportConfiguration() {
        return this.config;
    }

    @Override
	public boolean setImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType) {
        try {
            if (this.checkImportConfiguration(aConfig)) {
                this.config = aConfig;
                this.log    = this.config.getLogger();
                return true;
            }
        } catch (Exception ex) {
            Logger.error("Invalid configuration", ex, this.getClass());
        }
        return false;
    }

    public boolean checkImportConfiguration(GenericDataImportConfiguration aConfig) throws Exception {
        return true;
    }

    @Override
	public void run() {
        super.run();
        init();
        this.doImport();
    }

    public void init() {
        this.updated  = 0;
        this.created  = 0;
        this.errors   = 0;
        this.invalid  = 0;
        this.ignored  = 0;

        this.knowledgeBase = PersistencyLayer.getKnowledgeBase();
    }

    /**
     * This method runs the import. If this is a 2-phase process using synthetic
     * IDs this performs phase 1 of the import populating simple types only and
     * writing the objects to the data base.<br/>
     * Note: Phase 2 is then needed to resolve foreign key references. Otherwise
     * calling this method is enough.
     * HINT: this does the same as {@link GenericDataImportHelper}, but runs the import
     * functions in one single loop
     */
    public final void doImport() {
		TLContext.inSystemContext(getClass(), this);
	}

	@Override
	public void inContext() {
        try {
            plainDataObjects = this.getImporter().getPlainObjects();

            this.expected = plainDataObjects.size();
            this.resetLogger(this.expected);

            GenericCache theCache = this.getCache();
            GenericTypeResolver theTypeResolver = this.getTypeResolver();

            theCache.reload();

            boolean doCommit = this.isDoCommit();
            boolean doCreate = this.isDoCreate();
            boolean doUpdate = this.isDoUpdate();
            int     commitInterval = this.getCommitInterval();

            logInfo("Starting import.");
            logInfo("Save changes: " + doCommit + ".");
            logInfo("Create new objects: " + doCreate + ".");
            logInfo("Update existing objects: " + doUpdate + ".");
            logInfo("Found " + this.expected + " datasets to import.");

            // FIRST STEP
            // create business objects populated with only simple types
            for (int i=0; i<this.expected; i++) {
                GenericValueMap thePlainDO = (GenericValueMap) plainDataObjects.get(i);
                this.increaseProgress();
                try {
                    GenericValueMap theDO = this.getConverter(theTypeResolver.resolveType(thePlainDO)).convert(thePlainDO, theCache);
                    String theType   = theDO.getType();

                    ValidationResult theResult = this.getValidator(theType).validateSimpleTypes(theDO, theCache);
                    String           theFKey   = this.getForeignKeyAttribute(theType);
                    String           mappedKey = this.config.getMappingForColumn(theType, theFKey).getAttributeName();

                    if (theResult.isValid()) {
                        try {
                            Object theKey = theDO.getAttributeValue(theFKey);

                            Object theBO = theCache.get(theType, theKey);

                            if (doCreate && theBO == null) {
                                try {
                                    theBO = this.getCreateHandler(theType).createBusinessObject(theDO, mappedKey);
                                    if (theBO != null) {
                                        theCache.add(theType, theKey, theBO);
                                        this.logInfo(i + ": Created " + theType + " / " + theKey + ": " + theBO);
                                        this.created++;
                                    }
                                    else {
                                        this.logError(i + ":  Created object for " + theType + " / " + theKey + " is null!");
                                        this.errors++;
                                    }
                                } catch (CreateException cex) {
                                    this.logError(i + ":  Unable to create object for " + theType + " / " + theKey + "!", cex);
                                    this.errors++;
                                }
                            }
                            else if (doUpdate && theBO != null){
                                try {
                                    theBO = this.getUpdateHandler(theType).updateBusinessObject(theBO, theDO, mappedKey);
                                    if (theBO != null) {
                                        theCache.add(theType, theKey, theBO);
                                        this.logInfo(i + ": Modified " + theType + " / " + theKey + ": " + theBO);
                                        this.updated++;
                                    }
                                    else {
                                        this.logError(i + ": Updated object for " + theType + " / " + theKey + " is null!");
                                        this.errors++;
                                    }
                                } catch (UpdateException uex) {
                                    this.logError(i + ": Unable to update object for " + theType + " / " + theKey + "!", uex);
                                    this.errors++;
                                }
                            }
                            else {
                                String theReason = doCreate ? doUpdate ? ": Unknown reason, see previous messages." : ": " : ": No object found.";
                                this.logInfo(i + ": Ignored " + theType + " / " + theKey + theReason);
                                this.ignored++;
                            }
                        } catch (NoSuchAttributeException nsax) {
                            this.logError(i + ": Attribute '" + theFKey +"' not found for dataset " + i, nsax);
                            this.errors++;
                        }
                    }
                    else {
                        this.logWarn(i + ": invalid dataset");
                        StringWriter theOut  = new StringWriter();
                        PrintWriter  theOutW = new PrintWriter(theOut);
                        ValidationResult.printErrorMessages(theOutW, theResult);
                        theOutW.flush();
                        this.logWarn(theOut.toString());
                        this.invalid++;
                    }
                } catch (Exception ex) {
                    this.logError(i + ": Unable to process data object: " + thePlainDO, ex);
                    this.errors++;
                }

                if (doCommit && (i % commitInterval == 0)) {
                    if(! knowledgeBase.commit()) {
                        this.logError(i + ": Commit failed around object nr. " + i + " / " + thePlainDO);
                    }
                    else {
                        this.logInfo(i + ": Commiting " + i + "/" + this.expected);
                    }
                }
            }
            if (doCommit) {
                if(! knowledgeBase.commit()) {
                    this.logError("Last commit failed");
                }
                else {
                    this.logInfo("Last commit succeeded");
                }
            }

        } catch (Exception ex) {
            this.logError("FATAL: Import failed", ex);
        } finally {
            this.setFinished(); // TODO this must ALWAYS be called at the END of the operation
        }
    }

    private void resetLogger(long expected) {
        this.log.reset(expected);
    }

    private void increaseProgress() {
        this.log.increaseProgress();
    }

    private void logError(String aMessage, Exception anException) {
        this.log.error(aMessage + NEWLINE, anException, this.getClass());
    }

    private void logError(String aMessage) {
        this.logError(aMessage, null);
    }

    private void logInfo(String aMessage) {
        this.log.info(aMessage + NEWLINE, this.getClass());
    }

    private void logWarn(String aMessage) {
        this.logError(aMessage, null);
    }

    private void setFinished() {

        logInfo("Import result:");
        logInfo("Objects expected:  " + expected);
        logInfo("-----------------------------");
        logInfo("Objects created:   " + created);
        logInfo("Objects updated:   " + updated);
        logInfo("Objects ignored:   " + ignored);
        logInfo("-----------------------------");
        logInfo("Invalid datasets:  " + invalid);
        logInfo("Process errors:    " + errors);

        this.log.setFinished("Import finished.");
    }

    /**
     * @see com.top_logic.layout.progress.ProgressInfo#getCurrent()
     */
    @Override
	public long getCurrent() {
        return this.log.getCurrent();
    }

    /**
     * @see com.top_logic.layout.progress.ProgressInfo#getExpected()
     */
    @Override
	public long getExpected() {
        return this.log.getExpected();
    }

    /**
     * @see com.top_logic.layout.progress.ProgressInfo#getMessage()
     */
    @Override
	public synchronized String getMessage() {
        return this.log.getMessage();
    }

    /**
     * @see com.top_logic.layout.progress.ProgressInfo#getProgress()
     */
    @Override
	public float getProgress() {
        return this.log.getProgress();
    }

    /**
     * @see com.top_logic.layout.progress.ProgressInfo#getRefreshSeconds()
     */
    @Override
	public int getRefreshSeconds() {
        return this.log.getRefreshSeconds();
    }

    /**
     * @see com.top_logic.layout.progress.ProgressInfo#isFinished()
     */
    @Override
	public boolean isFinished() {
        return this.log.isFinished();
    }
}

