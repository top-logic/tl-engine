/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.dispatching;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.importer.ImporterService;
import com.top_logic.importer.base.AbstractImportParser;
import com.top_logic.importer.base.AbstractImportPerformer;
import com.top_logic.importer.logger.FileLogger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.boundsec.assistent.EVAAssistantController;
import com.top_logic.tool.boundsec.assistent.ValidatingUploadHandler.ImportMessage;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Importer is able to use different importer so we need to handle that selection it in here.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DispatchingEVAAssistantController extends EVAAssistantController {

	/**
	 * Configuration for a DispatchingEVAAssistantController.
	 * 
	 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config extends EVAAssistantController.Config {

		/** List of importers provided by this controller. */
		@Format(CommaSeparatedStrings.class)
		List<String> getImporterNames();
	}

	private String importerName;

    private final List<String> importerNames;

	private AbstractImportParser _parser;

	private AbstractImportPerformer _performer;

    /** 
     * Creates a {@link DispatchingEVAAssistantController}.
     */
	public DispatchingEVAAssistantController(InstantiationContext context, Config config) {
        super(context, config);

		this.importerNames = config.getImporterNames();
    }

    @Override
    protected ImportTask createImportTask(LayoutComponent aComponent, AssistentComponent anAssistent, File aFile) {
        String                     theName     = this.getImporterName();
        AbstractImportPerformer<?> theImporter = this.getImporter();
		ImportTask theTask = new DispatchingImportTask(theName, theImporter, this.getDataMap(anAssistent), aFile, null);

        ((Selectable) aComponent).setSelected(theImporter);

        return theTask;
    }

    @Override
    public List<String> getExtensions() {
        return (this.getImporterName() != null) ? this.getParser().getExtensions() : Collections.<String>emptyList();
    }

    public AbstractImportParser getParser() {
		return _parser;
    }

    public AbstractImportPerformer getImporter() {
		return _performer;
    }

    public String getImporterName() {
        return this.importerName;
    }

    public void setImporterName(String aName) {
        this.importerName = aName;
		initParserAndPerformer();
    }

	/**
	 * inits parser and performer according to the importer name set
	 */
	private void initParserAndPerformer() {
		if (importerNames.contains(importerName)) {
			_parser = getImporterService().getParser(importerName);
			_performer = getImporterService().getPerformer(importerName);
		} else {
			_parser = null;
			_performer = null;

		}
	}

    public List<String> getImporterNames() {
        return this.importerNames;
    }

    protected ImporterService getImporterService() {
        return ImporterService.getInstance();
    }

    /**
     * Dispatching version of the import task. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public class DispatchingImportTask extends ImportTask {

        // Attributes

        /** The name of the real importer. */
        private String implName;

        /** The values stored as validation result from the EVAAssistantController. */
        private Collection<ImportMessage> values;

        private Person person;

        // Constructors

        /** 
         * Creates a {@link DispatchingImportTask}.
         */
		public DispatchingImportTask(String aName, AbstractImportPerformer<?> aHandler, Map<String, Object> someData,
				File aFile, TLContext aContext) {
            super(aHandler, someData, aFile, aContext);

            this.implName = aName;
            this.values   = CollectionUtil.dynamicCastView(ImportMessage.class, (Collection<?>) someData.get(EVAAssistantController.VALIDATION_RESULT));
            this.person   = PersonManager.getManager().getPersonByName(this.implName);

            if (this.person == null) {
				this.person = PersonManager.getManager().getCurrentPerson();
            }
        }

        // Overridden methods

        @Override
		public void inContext() {
            if (this.person == null) {
                Logger.error("User '" + this.implName + "' not found!", DispatchingEVAAssistantController.class);
            }
            else { 
				TLContextManager.inPersonContext(this.person, () -> super.inContext());
            }
        }

        @Override
        protected void processFile(Map<String, Object> someData, File aFile) throws Exception {
    		Resources          theRes      = Resources.getInstance();
    		ProcessFileHandler theImporter = this.getImporter();
    		FileLogger         theLogger   = new FileLogger(implName);
    		StopWatch          theWatch    = StopWatch.createStartedWatch();

    		try {
                someData.put(EVAAssistantController.VALIDATION_RESULT, this.values);
                someData.put(AbstractImportPerformer.IMPORT_LOGGER, theLogger);

    			for (ImportMessage theMessage : this.values) {
    				theLogger.info(this, I18NConstants.VALIDATION_MESSAGE, theMessage.getString(theRes));
    			}

    			theImporter.processFile(someData, aFile);

    			theLogger.info(this, I18NConstants.IMPORT_FINISHED, DebugHelper.getTime(theWatch.getElapsedMillis()));
    		}
    		catch (Exception ex) {
    		    theLogger.error(this, I18NConstants.IMPORT_FAILED_MSG, ex, DebugHelper.getTime(theWatch.getElapsedMillis()));
    		    throw ex;
    		}
    		finally {
    			theLogger.close();
    		}
        }
    }

    /**
     * Dummy class for importing stuff. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">mga</a>
     */
    public static final class DummyDispatchingImporter implements ProcessFileHandler {

		/**
		 * Singleton {@link DummyDispatchingImporter} instance.
		 */
		public static final DummyDispatchingImporter INSTANCE = new DummyDispatchingImporter();

		/**
		 * Creates a new {@link DummyDispatchingImporter}.
		 */
		protected DummyDispatchingImporter() {
			// singleton instance
		}

        @Override
        public int getRefreshSeconds() {
            return 0;
        }

        @Override
        public long getExpected() {
            return 0;
        }

        @Override
        public long getCurrent() {
            return 0;
        }

        @Override
        public float getProgress() {
            return 0;
        }

        @Override
        public String getMessage() {
            return null;
        }

        @Override
        public boolean isFinished() {
            return false;
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
        }
    }
}

