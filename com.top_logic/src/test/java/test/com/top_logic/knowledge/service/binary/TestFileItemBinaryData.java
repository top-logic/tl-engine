/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.knowledge.service.binary.FileItemBinaryData;

/**
 * Test the {@link FileItemBinaryData}.
 * 
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class TestFileItemBinaryData extends BasicTestCase {

    /** 
     * Creates a new TestFileItemBinaryData.
     */
    public TestFileItemBinaryData(String name) {
        super(name);
    }

    /**
     * Test the main aspects of the {@link FileItemBinaryData}.
     */
    public void testMain() throws  IOException {
        DiskFileItemFactory dfif = new DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, 
                                       createdCleanTestDir("TestFileItemBinaryData"));  
        
		File theFile = new File(
			ModuleLayoutConstants.SRC_TEST_DIR
				+ "/test/com/top_logic/knowledge/service/binary/TestFileItemBinaryData.java");
        FileItem theItem = dfif.createItem("theFile", "text/plain", false, theFile.getName());
		try (InputStream input = new FileInputStream(theFile)) {
			try (OutputStream output = theItem.getOutputStream()) {
				StreamUtilities.copyStreamContents(input, output);
			}
		}
        
        FileItemBinaryData  theFibd = new FileItemBinaryData(theItem);
		BinaryData theFbbd = BinaryDataFactory.createBinaryData(theFile);
       
		File otherFile = new File(ModuleLayoutConstants.SRC_TEST_DIR
				+ "/test/com/top_logic/knowledge/service/binary/TestAll.java");
        FileItem otherItem = dfif.createItem("theFile", "text/plain", false, otherFile.getName());
		try (InputStream input1 = new FileInputStream(otherFile)) {
			try (OutputStream output = otherItem.getOutputStream()) {
				StreamUtilities.copyStreamContents(input1, output);
			}
		}
        FileItemBinaryData otherFibd = new FileItemBinaryData(otherItem);

		BinaryData brokenFbbd = BinaryDataFactory.createBinaryData(new File("IsNotThere.file"));
        
        long theLen = theFile.length();
        assertEquals(theLen , theFibd.getSize()); 
        assertEquals(theFibd, theFibd);
        assertEquals(theFbbd, theFibd);
        assertEquals(theFibd, theFbbd);
        
        assertFalse(theFibd.equals(otherFibd));
        assertFalse(otherFibd.equals(theFbbd));
        assertFalse(otherFibd.equals(brokenFbbd));
        assertFalse(brokenFbbd.equals(otherFibd));
        
        assertEquals(theFibd, theFibd);
        
		assertEquals(theFile.getName(), theFibd.getName());
		try (InputStream in1 = theFibd.getStream()) {
			try (InputStream in2 = new FileInputStream(theFile)) {
				assertTrue(StreamUtilities.equalsStreamContents(in1, in2));
			}
		}
        
    }
    
    /**
     * Provoke exceptions when using {@link FileItemBinaryData}.
     */
    public void testException() throws  IOException {
        DiskFileItemFactory dfif = new DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, 
                createdCleanTestDir("TestFileItemBinaryData"));  

		File theFile = new File(
			ModuleLayoutConstants.SRC_TEST_DIR
				+ "/test/com/top_logic/knowledge/service/binary/TestFileItemBinaryData.java");
        FileItem theItem = dfif.createItem("theFile", "text/plain", false, theFile.getName());
		try (InputStream input = new FileInputStream(theFile)) {
			try (OutputStream output = theItem.getOutputStream()) {
				StreamUtilities.copyStreamContents(input, output);
			}
		}
        
        FileItemBinaryData  theFibd = new FileItemBinaryData(theItem);

        File tmpFile = createNamedTestFile("TestFileItemBinaryDataE");
        FileUtilities.copyFile(theFile, tmpFile);
		BinaryData tmpData = BinaryDataFactory.createBinaryData(tmpFile);
        assertTrue(tmpFile.delete()); // Break underlying file 

        assertFalse(tmpData.equals(theFibd));
        assertFalse(theFibd.equals(tmpData)); // The Errors logged are correct.
    }
    
	/**
	 * Test suite.
	 */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(new TestSuite(TestFileItemBinaryData.class));
    }
    
    /**
     * Run tests in text UI.
     */
    public static void main(String[] args) {
        Logger.configureStdout(); // "INFO"
        TestRunner.run(suite());
    }

}

