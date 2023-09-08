/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Test the {@link BinaryData}.
 * 
 * @author <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class TestFileBasedBinaryData extends BasicTestCase {

    /** 
     * Creates a new TestFileBasedBinaryData.
     */
    public TestFileBasedBinaryData(String name) {
        super(name);
    }

    /**
	 * Test the main aspects of the {@link BinaryData}.
	 */
    public void testMain() throws  IOException {
		File thisFile = new File(ModuleLayoutConstants.SRC_TEST_DIR
				+ "/" + packageName() + "/TestFileBasedBinaryData.java");
		BinaryData thisFbbd = BinaryDataFactory.createBinaryData(thisFile);
		File otherFile = new File(ModuleLayoutConstants.SRC_TEST_DIR
				+ "/" + packageName() + "/TestMemoryBinaryData.data");
        
		BinaryData otherFbbd = BinaryDataFactory.createBinaryData(otherFile);
		BinaryData sameFbbd;
		try (FileInputStream data = new FileInputStream(thisFile)) {
			sameFbbd = BinaryDataFactory.createFileBasedBinaryData(data);
		}
		BinaryData otherSameBd;
		try (FileInputStream aStream = new FileInputStream(otherFile)) {
			otherSameBd = BinaryDataFactory.createMemoryBinaryData(aStream, otherFile.length());
		}
        
        long thisLen = thisFile.length();
        assertEquals(thisLen     , thisFbbd.getSize());
        assertEquals(thisFbbd, thisFbbd);
        assertEquals(thisFbbd, sameFbbd);
        assertFalse(thisFbbd.equals(otherFbbd));
        assertFalse(otherSameBd.equals(sameFbbd));
        assertTrue(thisFbbd.toString().indexOf(thisFile.getName()) > 0);
		try (InputStream in1 = otherSameBd.getStream()) {
			try (InputStream in2 = new FileInputStream(otherFile)) {
				assertTrue(StreamUtilities.equalsStreamContents(in1, in2));
			}
		}
    }
    
	private String packageName() {
		return TestFileBasedBinaryData.class.getPackageName().replace('.', '/');
	}

	/**
	 * Provoke exceptions when using FileBasedBinaryData.
	 */
    public void testException() throws  IOException {
		File noSuchFile =
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/com/top_logic/knowledge/service/binary/IsNoThere.java");
		BinaryData noFbbd = BinaryDataFactory.createBinaryData(noSuchFile);
        try {
            noFbbd.getStream();
			fail("Expected no file: " + ModuleLayoutConstants.SRC_TEST_DIR
				+ "/com/top_logic/knowledge/service/binary/IsNoThere.java");
        } catch (FileNotFoundException expected) { /* expected */ }
        
        File tmpFile = createNamedTestFile("TestFileBasedBinaryData");
		File thisFile = new File(ModuleLayoutConstants.SRC_TEST_DIR
				+ "/" + packageName() + "/TestFileBasedBinaryData.java");
		BinaryData thisFbbd = BinaryDataFactory.createBinaryData(thisFile);

        FileUtilities.copyFile(thisFile, tmpFile);
        
		BinaryData tmpData = BinaryDataFactory.createBinaryData(tmpFile);
        assertTrue(tmpFile.delete()); // Break underlying file 
        assertFalse(tmpData.equals(noFbbd));
        assertFalse(tmpData.equals(thisFbbd)); // The Error logged is correct.
    }
    
    /**
     * Break implementation of FileBasedBinaryData for testing
     */
    public static void cleanupFiles() {
        File tmpDir = Settings.getInstance().getTempDir();
        for(File f : tmpDir.listFiles(new FilenameFilter() {
            
            @Override
			public boolean accept(File dir, String name) {
                return name.startsWith("FileBasedBinaryData");
            }
        })) {
            if (!f.delete()) {
                fail("Failed to delete " + f.getName());
            }
        }
    }
    
	/**
	 * Test suite.
	 */
    public static Test suite() {
		return ModuleTestSetup
			.setupModule(ServiceTestSetup.createSetup(TestFileBasedBinaryData.class, Settings.Module.INSTANCE));
    }
    
    /**
     * Run tests in text UI.
     */
    public static void main(String[] args) {
        Logger.configureStdout(); // "INFO"
        TestRunner.run(suite());
    }

}

