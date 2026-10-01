/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.ocr;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.CreateDefaultTestPersons;

import com.top_logic.base.ocr.PDFUploadBatch;
import com.top_logic.base.ocr.TLPDFCompress;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.knowledge.objects.DCMetaData;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;

/**
 * Testcase for {@link com.top_logic.base.ocr.PDFUploadBatch}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestPDFUploadBatch extends BasicTestCase {

	/** Timeout for the batch to process a document. */
	private static final long TIMEOUT = 60000;

    /**
     * Create a new TestPDFUploadBatch for given test.
     * 
     * @param aName function to execute for testing.
     */
    public TestPDFUploadBatch(String aName) {
        super(aName);
    }

    /** 
     * Example for scanning an English document.
     * 
     * This Example was supplied by CVSion, dont blame me ;-)
     */
    public void testEnglish() throws Exception {
		doTestUpload("root", "STR_039.pdf", "TestPDFUploadBatchEN.pdf", "en");
    }
    
    /** 
     * Example for scanning a German document.
     * 
     * This is a much duller example, sorry. 
     */
    public void testGerman() throws Exception {
		doTestUpload("dau", "V-Modell.pdf", "TestPDFUploadBatchDE.pdf", "de");
    }

	private void doTestUpload(String user, String file, String documentName, String language) throws Exception {
		TLPDFCompress compress = TLPDFCompress.getInstance();
		if (!compress.isInstalled()) {
			return; // No need to choke on this ...
		}

		try {
			ThreadContext.pushSuperUser();
			Person ocrPerson = Person.byName(user);
			assertNotNull(ocrPerson);
		} finally {
			ThreadContext.popSuperUser();
		}

		KnowledgeBase theKB = KBSetup.getKnowledgeBase();
		File in = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/ocr/" + file);
		Document theDoc;
		try (Transaction tx = theKB.beginTransaction()) {
			theDoc = Document.createDocument(documentName, theKB);
			theDoc.setValue(DCMetaData.LANGUAGE, language);
			theDoc.update(BinaryDataFactory.createBinaryData(in));
			tx.commit();
		}
		try {
			assertEquals(1, theDoc.getVersionNumber());

			PDFUploadBatch.addUpload(theDoc, user);
			assertNull(TLContext.getContext());

			long timeout = System.currentTimeMillis() + TIMEOUT;
			while (theDoc.getVersionNumber() < 2 && System.currentTimeMillis() < timeout) {
				Thread.sleep(100); // Let PDFUploadBatch do its work ...
			}
			assertEquals(2, theDoc.getVersionNumber());
		} finally {
			try (Transaction tx = theKB.beginTransaction()) {
				theDoc.tDelete();
				tx.commit();
			}
		}
	}

    /**
     * the suite of test to execute.
     */
    public static Test suite () {
		Test test = new TestSuite(TestPDFUploadBatch.class);
		test = new CreateDefaultTestPersons(test);
		return PersonManagerSetup.createPersonManagerSetup(test);
    }

}
