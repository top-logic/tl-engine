/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.base;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.gui.AbstractCreateAttributedCommandHandler;
import com.top_logic.importer.ImportUtils;
import com.top_logic.importer.base.ImportPerformerFinalizer.SimpleImportPerformerFinalizer;
import com.top_logic.importer.base.ObjectProvider.SimpleObjectProvider;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.logger.PlainMessageLogger;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.tool.boundsec.assistent.EVAAssistantController.ProcessFileHandler;
import com.top_logic.util.Utils;

/**
 * Common superclass for importers.
 * 
 * <p>This class normally uses the data provided by an {@link AbstractImportParser}.</p>
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractImportPerformer<C extends AbstractImportPerformer.Config> implements ProcessFileHandler, ConfiguredInstance<AbstractImportPerformer.Config> {

	/**
	 * Configuration for a AbstractImportPerformer.
	 * 
	 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
    public interface Config extends PolymorphicConfiguration<AbstractImportPerformer<?>> {

        /** Return the command handler to be used for creating a new object. */
        String getCreateCommandHandler();

        /** Model builder to find objects already known by the system. */
        @InstanceFormat
        @InstanceDefault(SimpleObjectProvider.class)
        ObjectProvider getObjectProvider();
        
        /** Hook for a finalize function before commit will be called. */
        @InstanceFormat
        @InstanceDefault(SimpleImportPerformerFinalizer.class)
        ImportPerformerFinalizer getFinalizer();
    }

    /** Model to be used when calling {@link AbstractCreateAttributedCommandHandler#createNewObjectFromMap(Map, Wrapper)}. */
    public static final String MODEL = "model";

    /** List of values (type java.util.Map) read from excel file. */
    public static final String VALUE_MAP = "valueMap";
    
    /** Logger for messages coming up during import (must be an {@link ImportLogger}). */
    public static final String IMPORT_LOGGER = "importLogger";

    /** The expected number of steps needed for the progress */
    protected final AtomicLong _stepSize;

    /** The current step in progress reached */
    protected final AtomicLong _current;

	private boolean _isFinished;

    private final C config;

    private ImportLogger importLogger;

    /**
     * Creates a {@link AbstractImportPerformer}.
     */
	public AbstractImportPerformer(@SuppressWarnings("unused") InstantiationContext aContext, C aConfig) {
        this.config = aConfig;

		_stepSize = new AtomicLong(100);
		_current = new AtomicLong(0);
    }

    /** 
     * Import the values defined in the given map.
     * 
     * @param    someValues    The values read from the external file, must not be <code>null</code>.
     * @param    aTX           Transaction for intermediate commits.
     * @return   The result from the import.
     * @throws   Exception     If accessing one aspect of the attributed fails.
     * @see      #intermediateCommit(Transaction)
     */
    public abstract ImportResult doImport(List<Map<String, Object>> someValues, Transaction aTX) throws Exception;

    @Override
    public C getConfig() {
        return this.config;
    }

    @Override
	public long getCurrent() {
		return _current.get();
    }

    @Override
	public long getExpected() {
		return _stepSize.get();
    }

    @Override
	public String getMessage() {
        return (this.importLogger != null) ? ImportUtils.INSTANCE.getPlainMessageLogger(this.importLogger).getString() : "";
    }

    @Override
	public float getProgress() {
        return 100.0f * this.getCurrent() / this.getExpected();
    }

    @Override
	public int getRefreshSeconds() {
        return 1;
    }

    @Override
	public boolean isFinished() {
		return _isFinished;
    }

    @Override
	public boolean signalStop() {
        return false;
    }

	@Override
	public boolean shouldStop() {
		return false;
	}

    @Override
    public void processFile(Map<String, Object> someData, File aFile) throws Exception {
        this.importData(someData);
    }

    /** 
     * Convert the given data into attributed objects.
     * 
     * <p>This method will care about the commit handling and hand over
     * the real work to {@link #doImport(List, Transaction)}.</p>
     * 
     * @param    someData     The data to be imported.
     * @throws   Exception    When something went wring during importing.
     * @see      #prepareImporter(Map)
     * @see      #doImport(List, Transaction)
     */
    public void importData(Map<String, Object> someData) throws Exception {
        try {
        	List<Map<String, Object>> theValues = this.prepareImporter(someData);

        	this.addInfoMessage(I18NConstants.START_IMPORT);

            Transaction theTX = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().beginTransaction();

            try {
                ImportResult theResult = this.doImport(theValues, theTX);

                this.finalizeImport(theResult);

                theTX = theResult.getTransaction();

                this.addInfoMessage(I18NConstants.IMPORT_COMMIT);

                theTX.commit();
            }
            catch (Exception ex) {
                this.addErrorMessage(I18NConstants.IMPORT_COMMIT_FAILED_WITH_REASON, ex);

				throw ex;
            }
            finally {
                theTX.rollback();
            }
        }
        catch (Exception toLog) {
            this.addErrorMessage(I18NConstants.IMPORT_FAILED_MSG, toLog);

            throw toLog;
        }
        finally {
			_current.set(_stepSize.get());
			_isFinished = true;
        }
    }

    /** 
     * Prepare this importer by extracting the values from the given map and set the progress relevant values.
     * 
     * @param    someData   The data containing the requested information.
     * @return   The values to be imported.
     */
    protected List<Map<String, Object>> prepareImporter(Map<String, Object> someData) {
        List<Map<String, Object>> theValues = this.getValueMap(someData);

        _isFinished = false;
        _stepSize.set(theValues.size() + 2);
        _current.set(1);

        this.importLogger = this.getImportLogger(someData);

        return theValues;
    }

    /** 
     * Return the logger to be used.
     * 
     * @param    someData    Data which might contain a valid import logger.
     * @return   The logger created by {@link #createImportLogger()}, never <code>null</code>.
     */
    protected ImportLogger getImportLogger(Map<String, Object> someData) {
        Object       theLogger = someData.get(IMPORT_LOGGER);
        ImportLogger theInner  = this.createImportLogger();

        if (theLogger instanceof ImportLogger) {
            return ImportUtils.INSTANCE.merge((ImportLogger) theLogger, theInner);
        }
        else {
            return theInner;
        }
    }

    /** 
     * Create the import logger to be used during execution.
     * 
     * @return   The requested logger, never <code>null</code>.
     */
    protected ImportLogger createImportLogger() {
        return new PlainMessageLogger("\n");
    }

    /** 
     * Return the list of maps of values to be used when creating the objects. 
     * 
     * @param    someData    The map of data to get the values from, must not be <code>null</code>.
     * @return   The requested list of map of values.
     */
    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> getValueMap(Map<String, Object> someData) {
        return (List<Map<String, Object>>) someData.get(VALUE_MAP);
    }

    /**
     * Optional commit while importing.
     * 
     * <p>May be called any time during imports by sub classes. When this method
     * has been called the given transaction is invalid. This method will return
     * a new one to operate on it.</p>
     * 
     * @param    aTX    Current transaction.
     * @return   New transaction to be used from now on.
     * @throws   KnowledgeBaseException    When committing fails.
     */
    protected Transaction intermediateCommit(Transaction aTX) throws KnowledgeBaseException {
        aTX.commit();

        return KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().beginTransaction();
    }

    /**
     * Append an information message to {@link #importLogger}.
     * 
     * @param    aKey           I18N key of the message.
     * @param    someObjects    Additional objects to be appended to the message.
     */
    protected void addInfoMessage(ResKey aKey, Object... someObjects) {
        this.importLogger.info(AbstractImportPerformer.class, aKey, someObjects);
    }

    /**
     * Append an error message to {@link #importLogger}.
     * 
     * @param    aKey           I18N key of the message.
     * @param    ex             Exception occurred.
     * @param    someObjects    Additional objects to be appended to the message.
     */
    protected void addErrorMessage(ResKey aKey, Throwable ex, Object... someObjects) {
        this.importLogger.error(AbstractImportPerformer.class, aKey, ex, someObjects);
    }

    /** 
     * Write the result of the import to logger and {@link #addInfoMessage(ResKey, Object...)}.
     * 
     * @param countCreated    Number of objects created.
     * @param countUpdated    Number of objects updated.
     */
    protected void writeImportFinishedMessage(int countCreated, int countUpdated) {
        if ((countUpdated == 0) && (countCreated != countUpdated)) {
            this.addInfoMessage(I18NConstants.FINISHED_CREATED, countCreated);
        }
        else {
            countCreated = countCreated - countUpdated;

            if (countCreated > 0) {
                this.addInfoMessage(I18NConstants.FINISHED_CREATED_UPDATED, countCreated, countUpdated);
            }
            else { 
                this.addInfoMessage(I18NConstants.FINISHED_UPDATED, countUpdated);
            }
        }
    }

    private void finalizeImport(ImportResult aResult) {
        this.config.getFinalizer().finalizeImport(aResult, this.importLogger);
    }

	/**
	 * Set a value to the given attributed.
	 * 
	 * @param anObject
	 *        object to set the new value for.
	 * @param aKey
	 *        Name of the attribute to be set.
	 * @param aValue
	 *        Value to be set.
	 * @return <code>true</code> when value has been changed due to this call.
	 */
	public static boolean setValue(Wrapper anObject, String aKey, Object aValue) {
		Object theOldValue = anObject.getValue(aKey);

		anObject.setValue(aKey, aValue);

		if ((theOldValue instanceof Collection) && (aValue instanceof Collection)) {
			return !CollectionUtil.containsSame((Collection<?>) theOldValue, (Collection<?>) aValue);
		} else {
			return !Utils.equals(theOldValue, aValue);
		}
	}

    /**
     * Object performed by the {@link AbstractImportPerformer}. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class ImportResult {

        // Attributes

        private final Map<Object, Map<String, Object>> objectMap;

        private Transaction transaction;

        // Constructors

        /** 
         * Creates a {@link ImportResult}.
         */
        public ImportResult(Transaction aTX) {
            this.objectMap   = new HashMap<>();
            this.transaction = aTX;
        }
        
        // Overridden methods from Object

        @Override
        public String toString() {
            return new NameBuilder(this)
                    .add("objectMap", this.objectMap)
                    .add("transaction", this.transaction)
                    .build();
        }

        // Public methods

        /** 
         * Add the given object with its raw values to the inner map of handled objects.
         * 
         * <p>The given object is either a new created one or the one being updated.</p>
         * 
         * @param anObject
         *        The object handled by the performer.
         * @param someRawValues
         *        The raw values for the handled object.
         * @return
         *        The previous value from inner map (@see {@link Map#put(Object, Object)}).
         */
        public Map<String, Object> put(Object anObject, Map<String, Object> someRawValues) {
            return this.objectMap.put(anObject, someRawValues);
        }

        /**
         * This method returns the objectMap.
         * 
         * @return    Returns the objectMap.
         */
        public Map<Object, Map<String, Object>> getObjectMap() {
            return objectMap;
        }

        /**
         * This method returns the transaction.
         * 
         * @return    Returns the transaction.
         */
        public Transaction getTransaction() {
            return transaction;
        }

        /**
         * This method sets the transaction.
         *
         * @param    aTransaction    The transaction to set.
         */
        public void setTransaction(Transaction aTransaction) {
            this.transaction = aTransaction;
            
        }
    }
}
