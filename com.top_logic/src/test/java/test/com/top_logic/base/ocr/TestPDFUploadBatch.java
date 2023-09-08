/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.ocr;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.ocr.PDFUploadBatch;
import com.top_logic.base.ocr.TLPDFCompress;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.dob.DataObject;
import com.top_logic.dsa.DAPropertyNames;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.objects.DCMetaData;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.TLContext;

/**
 * Testcase for {@link com.top_logic.base.ocr.PDFUploadBatch}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestPDFUploadBatch extends BasicTestCase {

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
        TLPDFCompress compress = TLPDFCompress.getInstance();
        if (!compress.isInstalled())
            return; // No need to choke on this ...
        
        try {
            // TODO TRI fix this bug please
            ThreadContext.pushSuperUser();
            Person ocrPerson = PersonManager.getManager().getPersonByName("root");
            assertTrue(ocrPerson.getKnowledgeBase().commit());
        } finally  {
            ThreadContext.popSuperUser();
        }
        
        Document        theDoc = null;
        KnowledgeBase   theKB  = KBSetup.getKnowledgeBase();
		File in = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/ocr/STR_039.pdf");
        DataAccessProxy tmpDSA = new DataAccessProxy("repository://pdf");
        if (!tmpDSA.exists()) {
            tmpDSA.getParentProxy().createContainer(tmpDSA.getName());
        }
        final FileInputStream data = new FileInputStream(in);
		String entryName;
		try {
			entryName = tmpDSA.createEntry("TestPDFUploadBatchEN.pdf", data);
		} finally {
			data.close();
		}
        DataAccessProxy entry     = new DataAccessProxy(entryName);
        try {
            theDoc = Document.createDocument("TestPDFUploadBatchEN.pdf", entryName, theKB);
            theDoc.setValue(DCMetaData.LANGUAGE, "en");
            assertEquals(entry, theDoc.getDAP());
            assertTrue(entry.unlock());
            assertTrue(theDoc.getKnowledgeBase().commit());
            
            PDFUploadBatch.addUpload(theDoc, "root");
            assertNull(TLContext.getContext());
            assertFalse (entry.lock()); // since addUpload has locked it ...
            DataObject props = entry.getProperties();
            assertEquals("root", props.getAttributeValue(DAPropertyNames.LOCKER));
            Thread.sleep(6000); // Let PDFUploadBatch do its work ...

            assertTrue (entry.lock());
            assertTrue (entry.getVersions().length == 2);
            
        } finally {
            theDoc.tDelete();
            theKB.commit();
        }
    }
    
    /** 
     * Example for scanning a German document.
     * 
     * This is a much duller example, sorry. 
     */
    public void testGerman() throws Exception {

        TLPDFCompress compress = TLPDFCompress.getInstance();
        if (!compress.isInstalled())
            return; // No need to choke on this ...
        
        try {
            // TODO TRI fix this bug please
            ThreadContext.pushSuperUser();
            Person ocrPerson = PersonManager.getManager().getPersonByName("dau");
            assertTrue(ocrPerson.getKnowledgeBase().commit());
        } finally  {
            ThreadContext.popSuperUser();
        }
        
        Document        theDoc = null;
        KnowledgeBase   theKB  = KBSetup.getKnowledgeBase();
		File in = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/ocr/V-Modell.pdf");
        DataAccessProxy tmpDSA = new DataAccessProxy("repository://pdf");
        if (!tmpDSA.exists()) {
            tmpDSA.getParentProxy().createContainer(tmpDSA.getName());
        }
        final FileInputStream data = new FileInputStream(in);
		String entryName;
		try {
			entryName = tmpDSA.createEntry("TestPDFUploadBatchDE.pdf", data);
		} finally {
			data.close();
		}
        DataAccessProxy entry     = new DataAccessProxy(entryName);
        try {
            theDoc = Document.createDocument("TestPDFUploadBatchDE.pdf", entryName, theKB);
            theDoc.setValue(DCMetaData.LANGUAGE, "de");
            assertEquals(entry, theDoc.getDAP());
            assertTrue(entry.unlock());
            assertTrue(theDoc.getKnowledgeBase().commit());
            
            PDFUploadBatch.addUpload(theDoc, "dau");
            assertNull(TLContext.getContext());
            assertFalse (entry.lock()); // since addUpload has locked it ...
            DataObject props = entry.getProperties();
            assertEquals("dau", props.getAttributeValue(DAPropertyNames.LOCKER));
            Thread.sleep(8000); // Let PDFUploadBatch do its work ...

            assertTrue (entry.lock());
            assertTrue (entry.getVersions().length == 2);
            
        } finally {
            theDoc.tDelete();
            theKB.commit();
        }
    }

    /** 
     * Example for scanning more than one docment twice.
     * 
     * This is a much duller example, sorry. 
     */
    public void testMultiple() throws Exception {

        TLPDFCompress compress = TLPDFCompress.getInstance();
        if (!compress.isInstalled())
            return; // No need to choke on this ...
        
        try {
            // TODO TRI fix this bug please
            ThreadContext.pushSuperUser();
            Person ocrPerson = PersonManager.getManager().getPersonByName("dau");
            assertTrue(ocrPerson.getKnowledgeBase().commit());
        } finally  {
            ThreadContext.popSuperUser();
        }
        
        Document        theDoc1 = null;
        Document        theDoc2 = null;
        KnowledgeBase   theKB  = KBSetup.getKnowledgeBase();
		File in1 = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/ocr/V-Modell.pdf");
		File in2 = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/ocr/V-Modell.pdf");

        DataAccessProxy tmpDSA = new DataAccessProxy("repository://pdf");
        if (!tmpDSA.exists()) {
            tmpDSA.getParentProxy().createContainer(tmpDSA.getName());
        }
		final FileInputStream data1 = new FileInputStream(in1);
		String entryName1;
		try {
			entryName1 = tmpDSA.createEntry("TestPDFUploadBatchMultipleEN.pdf", data1);
		} finally {
			data1.close();
		}
		final FileInputStream data2 = new FileInputStream(in2);
		String entryName2;
		try {
			entryName2 = tmpDSA.createEntry("TestPDFUploadBatchMultipleDE.pdf", data2);
		} finally {
			data2.close();
		}
        DataAccessProxy entry1     = new DataAccessProxy(entryName1);
        DataAccessProxy entry2     = new DataAccessProxy(entryName2);
        try {
            theDoc1 = Document.createDocument("TestPDFUploadBatchEN.pdf", entryName1, theKB);
            theDoc2 = Document.createDocument("TestPDFUploadBatchEN.pdf", entryName2, theKB);
            theDoc1.setValue(DCMetaData.LANGUAGE, "en");
            theDoc2.setValue(DCMetaData.LANGUAGE, "de");
            assertEquals(entry1, theDoc1.getDAP());
            assertEquals(entry2, theDoc2.getDAP());
            assertTrue(entry1.unlock());
            assertTrue(entry2.unlock());
            assertTrue(theKB.commit());
            
            PDFUploadBatch.addUpload(theDoc1, "dau");
            PDFUploadBatch.addUpload(theDoc2, "dau");
            assertNull(TLContext.getContext());
            assertFalse (entry1.lock()); // since addUpload has locked it ...
            DataObject props = entry1.getProperties();
            assertEquals("dau", props.getAttributeValue(DAPropertyNames.LOCKER));
                       props = entry2.getProperties();
            assertEquals("dau", props.getAttributeValue(DAPropertyNames.LOCKER));
            
            while(!entry1.lock()) {
                Thread.sleep(100); // Busy wait while PDFUploadBatch does its work ...
            }
            entry1.unlock();
            theDoc1.update(new FileInputStream(in1));
            PDFUploadBatch.addUpload(theDoc1, "dau");
            while(!entry2.lock()) {
                Thread.sleep(100); // Busy wait while PDFUploadBatch does its work ...
            }
            entry2.unlock();
            theDoc2.update(new FileInputStream(in2));
            PDFUploadBatch.addUpload(theDoc2, "dau");

            Thread.sleep(8000); // Wait while PDFUploadBatch does its work ...
            assertTrue (entry1.lock());
            assertTrue (entry2.lock());
            assertTrue (entry1.getVersions().length == 4);
            assertTrue (entry2.getVersions().length == 4);
            
        } finally {
            theDoc1.tDelete();
            theDoc2.tDelete();
            theKB.commit();
        }
    }

    /**
     * the suite of test to execute.
     */
    public static Test suite () {
		return KBSetup.getSingleKBTest(TestPDFUploadBatch.class);
    }

    /**
     * Used for direct testing.
     */
    public static void main(String[] args) {
        
        FileUtilities.deleteR(BasicTestCase.createNamedTestFile("/repository/pdf"));
        FileUtilities.deleteR(BasicTestCase.createNamedTestFile("/workarea/pdf"));
        KBSetup.setCreateTables(false);
        
        Logger.configureStdout("ERROR");
        
        TestRunner.run(suite ());
    }
}
