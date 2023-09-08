/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.RearrangableTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.element.layout.genericimport.GenericDataImportController;
import com.top_logic.importer.ImportUtils;
import com.top_logic.importer.ImporterService;
import com.top_logic.importer.base.AbstractImportParser;
import com.top_logic.importer.base.AbstractImportPerformer;
import com.top_logic.importer.logger.FileLogger;
import com.top_logic.importer.logger.FirstLastLogger;
import com.top_logic.importer.logger.HasAnyErrorLogger;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.logger.ImportMessageLogger;
import com.top_logic.importer.logger.MultiLogger;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.boundsec.assistent.AssistentComponent.Config;
import com.top_logic.tool.boundsec.assistent.ValidatingUploadHandler.ImportMessage;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public abstract class AbstractTestImporter extends BasicTestCase {

    private HasAnyErrorLogger hasAnyErrorLogger;

    protected abstract String getFileName();

    protected abstract String getImporterName();

    protected abstract Map<String, Object> getDataMap(final List<Map<String, Object>> someData);

    // Test cases
    
    public void testEmptyFileImporterProcess() throws Exception {
        try {
            this.loadData(null, this.getParser());
            
            fail("Expected to fail on null data!");
        }
        catch (Throwable ex) {
            // Expected
        }
    }

    public void testEmptyImportData() throws Exception {
        AbstractImportPerformer theImporter = this.getPerformer();

        try {
            this.importData(null, theImporter);

            fail("Expected to fail on null data!");
        }
        catch (Exception ex) {
            // Expected
        }

        this.importData(Collections.<Map<String, Object>>emptyList(), theImporter);
    }

    public void testWrongFileImporterProcess() throws Exception {
        String theName = this.getWrongFileName();

        if (theName != null) {
            try {
				BinaryData theFile = FileManager.getInstance().getData(theName);

                this.loadData(theFile, this.getParser());

                fail("Expected to fail on wrong data!");
            }
            catch (Throwable ex) {
                // Expected
            }
        }
    }

    public void testImporterProcess() throws Exception {
        List<Map<String, Object>> theData = this.loadData(this.getDataFile(), this.getParser());

        this.importData(theData, this.getPerformer());
    }

    public void testParser() throws Exception {
        AbstractImportParser theParser = this.getParser();

        theParser.validateFile(null, null);
		theParser.validateFile(null, BinaryDataFactory.createBinaryData(new File("does_not_exist.stuff")));
		theParser.validateFile(null,
			BinaryDataFactory.createBinaryData(new File("does_not_exist" + this.getFileName())));

		theParser.validateFile(AbstractTestImporter.createAssistentComponent(), this.getDataFile());
    }

    protected final AbstractImportPerformer getPerformer() {
        return this.getImporterService().getPerformer(this.getImporterName());
    }

    protected final AbstractImportParser getParser() { 
        return this.getImporterService().getParser(this.getImporterName());
    }

    protected ImporterService getImporterService() {
        return ImporterService.getInstance();
    }
    protected String getWrongFileName() {
        return null;
    }

	protected BinaryData getDataFile() throws IOException {
		return FileManager.getInstance().getData(this.getFileName());
    }

    protected void importData(List<Map<String, Object>> someData, AbstractImportPerformer<?> anImporter) throws Exception {
        anImporter.processFile(this.getDataMap(someData), null);
    }

	protected List<Map<String, Object>> loadData(BinaryData aFile, AbstractImportParser aHandler)
			throws IOException {
		InputStream theStream = (aFile != null) ? aFile.getStream() : null;
        ImportLogger theLogger = this.createLogger();

        try {
            List<Map<String, Object>> theResult = aHandler.validateStream(theStream, theLogger);

            this.checkMessages(theLogger);

            return theResult;
        }
        finally {
            FileUtilities.close(theStream);
        }
    }

    protected ImportLogger createLogger() {
        ImportMessageLogger theLogger    = new ImportMessageLogger(new ArrayList<ImportMessage>());
        FileLogger          theFile      = new FileLogger("TestImporter");
        FirstLastLogger     theFirstLast = new FirstLastLogger();

        this.hasAnyErrorLogger = new HasAnyErrorLogger();

        return new MultiLogger(theLogger, theFile, theFirstLast, this.hasAnyErrorLogger);
    }

    protected void checkMessages(ImportLogger aLogger) {
        if (this.hasAnyErrorLogger.hasError()) {
            ImportMessageLogger theLogger = ImportUtils.INSTANCE.getImportMessageLogger(aLogger);

            this.logErrorMessages(theLogger.getImportMessages());
        }
    }

    protected void logErrorMessages(Collection<ImportMessage> someMessages) {
        Resources theRes = Resources.getInstance();

        for (ImportMessage theMessage : someMessages) {
            assertFalse(theMessage.getString(theRes), theMessage.isError());
        }
    }

    public static Test suite(Class<? extends Test> aClass) {
		Test test = new TestSuite(aClass);
		test = new EnsureImporterLogDir(test);
		test = ServiceTestSetup.createSetup(test,
			LabelProviderService.Module.INSTANCE,
			ImporterService.Module.INSTANCE,
			ModelService.Module.INSTANCE);

		return PersonManagerSetup.createPersonManagerSetup(test);
    }

    public static AssistentComponent createAssistentComponent() {
    	Config theItem = TypedConfiguration.newConfigItem(AssistentComponent.Config.class);
		com.top_logic.element.layout.genericimport.GenericDataImportController.Config theContr =
			TypedConfiguration.newConfigItem(GenericDataImportController.Config.class);

		theItem.setImplementationClass(AssistentComponent.class);
		theContr.setImplementationClass(GenericDataImportController.class);

		theItem.setController(theContr);

		return (AssistentComponent) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(theItem);
    }

	private static final class EnsureImporterLogDir extends RearrangableTestSetup {

		private static final MutableInteger SETUP_CNT = new MutableInteger();

		public EnsureImporterLogDir(Test test) {
			super(test, SETUP_CNT);
		}

		@Override
		protected void doSetUp() throws Exception {
			String logDirName = AliasManager.getInstance().replace(FileLogger.IMPORT_LOG_PATH);
			File logDir = new File(logDirName);
			if (logDir.exists()) {
				if (!logDir.isDirectory()) {
					throw new Exception("Log dir " + logDir.getCanonicalPath() + " is not a directory.");
				}
				return;
			}
			boolean success = logDir.mkdirs();
			if (!success) {
				throw new Exception("Unable to create log dir " + logDir.getCanonicalPath());
			}
		}

		@Override
		protected void doTearDown() throws Exception {
			// do not delete log dir to ensure post mortem analysis.
		}

	}
}
