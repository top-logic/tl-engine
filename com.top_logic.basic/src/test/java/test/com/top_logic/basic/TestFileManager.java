/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Testcase for the {@link com.top_logic.basic.FileManager}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
@SuppressWarnings("javadoc")
public class TestFileManager extends TestCase {

    /**
     * Constructor for TestFileManager.
     * 
     * @param name the fucntion to execute for testing.
     */
    public TestFileManager(String name) {
        super(name);
    }

    /**
     * Test default CTor (assuming project is current dir).
     */
    public void testFileManager() throws Exception {
		FileManager fMgr = new DefaultFileManager();
		FileManager.setInstance(fMgr);
		File f =
			fMgr.getIDEFileOrNull(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestFileManager.java");
        assertTrue(f.exists());
		f = fMgr.getIDEFileOrNull(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestNothing.strange");
        assertNull(f);
		f = fMgr.getIDEFile(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestNothing.strange");
        assertFalse(f.exists());
        
		f = fMgr.getIDEFileOrNull("file://" + ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestFileManager.java");
        assertTrue(f.exists());
		f = fMgr.getIDEFileOrNull("file://" + ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestNothing.strange");
        assertNull(f);
		f = fMgr.getIDEFileOrNull("file://" + new File(".").getAbsolutePath() + "/" + ModuleLayoutConstants.SRC_TEST_DIR
		+ "/test/com/top_logic/basic/TestNothing.strange");
        assertNull(f);
		f = fMgr.getIDEFile(
			"file://" + ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestNothing.strange");
        assertFalse(f.exists());

		try (InputStream is =
			fMgr.getStream(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestFileManager.java")) {
			assertTrue(is.available() > 0);
		}
        try {
			fMgr.getStream("/this/does/not/exist");
			fail("Expected failure.");
        } catch(FileNotFoundException ex) {
        	// expected
        }
    }

    /** 
     * test getting and Setting the instance 
     */
    public void testGetSetInstance() {
		FileManager fMgr = new DefaultFileManager();
        FileManager.setInstance(null);
        assertNotNull(FileManager.getInstance());
        FileManager.setInstance(fMgr);
        assertSame(fMgr, FileManager.getInstance());
    }
    
    /**
	 * Tests the method {@link FileManager#getFile(String)}
	 * 
	 * @throws Exception
	 *         Indicates a test-failure. Let Junit handle this.
	 */
    public void testgetFileNotNull() throws Exception {
		FileManager fileManager = new DefaultFileManager();
		FileManager.setInstance(fileManager);
		File existingFile = fileManager.getIDEFileOrNull(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestFileManager.java");
        assertTrue(existingFile.exists());
        
		assertFalse(fileManager.exists(
			ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/ThisFileDoesNotExists.java"));
    }
    
    public static Test suite() {
		return new FileManagerTestSetup(new TestSuite(TestFileManager.class));
    }

}
