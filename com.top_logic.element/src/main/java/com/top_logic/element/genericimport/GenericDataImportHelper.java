/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.genericimport.GenericDataImportConfiguration.UpdateException;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericImporter;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.knowledge.objects.CreateException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.progress.ProgressInfo;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GenericDataImportHelper extends AbstractGenericDataImportBase implements ProgressInfo {

    private int          current;
    private int          expected;
    private boolean      finished;
    private StringBuffer message;
    
    /** Stick Flag set by {@link #signalStop()} */
    private boolean      shouldStop;

    public GenericDataImportHelper(GenericDataImportConfiguration aConfig) {
        super(new Properties());
        this.setImportConfiguration(aConfig, null);
        this.resetProgress();
    }

    private void resetProgress() {
        current  = 0;
        expected = 1;
        finished = false;
        message  = new StringBuffer(256);
    }

    public List getImportedObjects() {
        this.resetProgress();
        try {
            GenericDataImportConfiguration theConf = this.getImportConfiguration();

            GenericImporter theImporter = theConf.getImporter();
            if (theImporter instanceof Runnable) {
            	Thread theThread = new Thread(null, (Runnable) theImporter, "GenericDataImport");
            	theThread.start();
            	theThread.join();
            }
            return theImporter.getPlainObjects();
        } catch (InterruptedException daex) {
            Logger.error("Unable to import objects", daex, this.getClass());
        } finally {
            this.current++;
            this.finished = true;
        }
        return new ArrayList(1);
    }

    /**
     * This method calls {@link GenericConverter#convert(GenericValueMap, com.top_logic.element.genericimport.GenericDataImportTask.GenericCache)}
     * for each data object in the given list.
     * It returns a new list of data objects of the conversion.
     *
     * @param somePlainDOs
     * @param aCache
     * @return
     */
//    public List getConvertedObjects(List somePlainDOs) {
//
//        this.resetProgress();
//
//        GenericDataImportConfiguration theConf = this.getImportConfiguration();
//        List             theConverted   = new ArrayList(somePlainDOs.size());
//        GenericCache     theCache       = theConf.getCache();
//        GenericTypeResolver theResolver = theConf.getTypeResolver();
//
//        this.expected = somePlainDOs.size();
//        for (int i=0; i<this.expected; i++) {
//            this.current++;
//            GenericValueMap thePlainDO = (GenericValueMap) somePlainDOs.get(i);
//            try {
//                GenericValueMap theDO = theConf.getConverter(theResolver.resolveType(thePlainDO)).convertSimpleTypes(thePlainDO, theCache); // TODO TEH, FSC
//                if (theDO != null) {
//                    theConverted.add(theDO);
//                }
//            } catch (DataObjectException doex) {
//                Logger.error("Converting " + thePlainDO.getIdentifier() + " failed", doex, GenericDataImportTask.class);
//            }
//        }
//        this.finished = true;
//        return theConverted;
//    }

    /**
     * This method calls {@link GenericValidator#validate(GenericValueMap, com.top_logic.element.genericimport.GenericDataImportTask.GenericCache)}
     * for each data object in the given list.
     * It returns a new list of valid data objects.
     * {@link ValidationResult}s marked as invalid will be added to the given invalidResults list.
     *
     * @param someConvertedDOs
     * @param aCache
     * @param invalidResults
     * @return
     */
//    public List getValidatedObjects(List someConvertedDOs, List invalidResults) {
//
//        this.resetProgress();
//
//        GenericDataImportConfiguration theConf = this.getImportConfiguration();
//        List         theValidated = new ArrayList(someConvertedDOs.size());
//        GenericCache theCache     = theConf.getCache();
//
//        this.expected = someConvertedDOs.size();
//        for (int i=0; i<this.expected; i++) {
//            this.current++;
//            GenericValueMap theConvertedDO = (GenericValueMap) someConvertedDOs.get(i);
//            ValidationResult theResult = theConf.getValidator(theConvertedDO.getType()).validate(theConvertedDO, theCache);
//            if (theResult.isValid()) {
//                theValidated.add(theConvertedDO);
//            }
//            else {
//                invalidResults.add(theResult);
//            }
//        }
//        this.finished = true;
//        return theValidated;
//    }

    /**
     * This method creates or updates objects from the given valid data objects.
     */
    public void createOrUpdate(List someValidDOs, KnowledgeBase aBase) {
        this.resetProgress();

        GenericDataImportConfiguration theConf = this.getImportConfiguration();

        this.expected    = someValidDOs.size();
        boolean doCommit = theConf.isDoCommit();
        boolean doCreate = theConf.isDoCreate();
        boolean doUpdate = theConf.isDoUpdate();

        int     commitInterval = theConf.getCommitInterval();
        GenericCache theCache  = theConf.getCache();

        for (int i=0; i<this.expected; i++) {
            GenericValueMap theDO   = (GenericValueMap) someValidDOs.get(i);
            current++;
            try {
                String theType = theDO.getType();
                String theFKey = theConf.getForeignKey(theType);
                Object theKey  = theDO.getAttributeValue(theFKey);

                Object theBO = theCache.get(theType, theKey);

                if (doCreate && theBO == null) {
                    try {
                        theBO = theConf.getCreateHandler(theType).createBusinessObject(theDO, null);
                        if (theBO != null) {
                            theCache.add(theType, theKey, theBO);
                        }
                        else {
                            Logger.error("Created object is null!", GenericDataImportTask.class);
                        }
                    } catch (CreateException cex) {
                        Logger.error("Unable to create object!", cex, GenericDataImportTask.class);
                    }
                }
                else if (doUpdate && theBO != null){
                    try {
                        theBO = theConf.getUpdateHandler(theType).updateBusinessObject(theBO, theDO, null);
                        if (theBO != null) {
                            theCache.add(theType, theKey, theBO);
                        }
                        else {
                            Logger.error("Updated object is null!", GenericDataImportTask.class);
                        }
                    } catch (UpdateException uex) {
                        Logger.error("Unable to update object!", uex, GenericDataImportTask.class);
                    }
                }
            } catch (NoSuchAttributeException nsax) {
                throw new UnreachableAssertion(nsax); // should never reached until all data objects must be validated before
            }

            if (doCommit && (i % commitInterval == 0)) {
                if(! aBase.commit()) {
                    Logger.error("Commit failed around object nr. " + i + " / " + theDO, GenericDataImportTask.class);
                }
                else {
                    Logger.info("Commiting " + i + "/" + this.expected, GenericDataImportTask.class);
                }
            }
        }
        if (doCommit) {
            if(! aBase.commit()) {
                Logger.error("Last commit failed", GenericDataImportTask.class);
            }
            else {
                Logger.info("Last commit succeeded", GenericDataImportTask.class);
            }
        }
        finished = true;
    }

    @Override
	public long getCurrent() {
        return this.current;
    }

    @Override
	public long getExpected() {
        return this.expected;
    }

    @Override
	public String getMessage() {
        return this.message.toString();
    }

    @Override
	public float getProgress() {
        return this.current / this.expected * 100;
    }

    @Override
	public int getRefreshSeconds() {
        return 1;
    }

    @Override
	public boolean isFinished() {
        return this.finished;
    }
    
    /**
     * {@inheritDoc}
     * 
     * Will set {@link #shouldStop}.
     */
    @Override
	public boolean signalStop() {
        return shouldStop = true;
    }

    /**
     * Return true when {@link #signalStop()} was called.
     * 
     * In this case you should stop processing as soon as possible.
     */
	@Override
	public boolean shouldStop() {
        return shouldStop;
    }

}

