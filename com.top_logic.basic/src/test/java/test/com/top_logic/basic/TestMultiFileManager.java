/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Testcase for the {@link com.top_logic.basic.FileManager}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
@SuppressWarnings("javadoc")
public class TestMultiFileManager extends TestCase {

    /**
     * Constructor for TestFileManager.
     * 
     * @param name the fucntion to execute for testing.
     */
    public TestMultiFileManager(String name) {
        super(name);
    }

	public void testTopPaths() throws IOException {
		FileManager fMgr =
			MultiFileManager.createMultiFileManager(
				"test/fixtures/TestFileManager/a",
				"test/fixtures/TestFileManager/b");
		FileManager.setInstance(fMgr);

		File res1 = fMgr.getIDEFileOrNull("/WEB-INF/res1");
		assertTrue(hasParentDir(res1, "a"));

		File res2 = fMgr.getIDEFileOrNull("/WEB-INF/res2");
		assertTrue(hasParentDir(res2, "b"));
	}

	public void testLookup() throws IOException {
		FileManager fMgr = MultiFileManager.createMultiFileManager(
			"test/fixtures/TestFileManager/a",
			"test/fixtures/TestFileManager/b");
		FileManager.setInstance(fMgr);
    	
		File bOnly0 = fMgr.getIDEFileOrNull("dir1/in-b-only.txt");
        File bOnly1 = fMgr.getIDEFile("dir1/in-b-only.txt");
        
		File notThere0 = fMgr.getIDEFileOrNull("dir1/not-there.txt");
        File notThere1 = fMgr.getIDEFile("dir1/not-there.txt");
        
		File root1 = fMgr.getIDEFile(".").getCanonicalFile();
        
        assertNotNull(root1);
        assertTrue(root1.getPath(), root1.getName().equals("a"));
    	
        assertNull(notThere0);
        
        assertNotNull(notThere1);
        assertTrue(notThere1.getPath(), hasParentDir(notThere1, "a"));
        
        assertTrue(bOnly0.getPath(), hasParentDir(bOnly0, "b"));
        
        assertNotNull(bOnly1);
        assertTrue(bOnly1.getPath(), bOnly1.equals(bOnly0));
		
		File fileManagerParent = fMgr.getIDEFileOrNull("..");
		
		File dirA = new File(fileManagerParent, "a");
		File dirB = new File(fileManagerParent, "b");
		
		String nameNeitherANorB = "neither-a_nor_b.test";
		String nameOnlyA = "only_a.test";
		String nameOnlyB = "only_b.test";
		String nameAAndB = "a_and_b.test";
		
		ensureNonExistence(new File(dirA, nameNeitherANorB));
		ensureNonExistence(new File(dirB, nameNeitherANorB));
		
		ensureExistence(new File(dirA, nameOnlyA));
		ensureNonExistence(new File(dirB, nameOnlyA));
		
		ensureNonExistence(new File(dirA, nameOnlyB));
		ensureExistence(new File(dirB, nameOnlyB));
		
		ensureExistence(new File(dirA, nameAAndB));
		ensureExistence(new File(dirB, nameAAndB));
		
		{
			File neitherANorB = fMgr.getIDEFileOrNull(nameNeitherANorB);
			assertNull(neitherANorB);
			
			File onlyA = fMgr.getIDEFileOrNull(nameOnlyA);
			assertNotNull(onlyA);
			assertTrue(onlyA.exists());
			assertTrue(hasParentDir(onlyA, "a"));
			
			File onlyB = fMgr.getIDEFileOrNull(nameOnlyB);
			assertNotNull(onlyB);
			assertTrue(onlyB.exists());
			assertTrue(hasParentDir(onlyB, "b"));
			
			File aAndB = fMgr.getIDEFileOrNull(nameAAndB);
			assertNotNull(aAndB);
			assertTrue(aAndB.exists());
			assertTrue(hasParentDir(aAndB, "a"));
		}
		
		{
			File neitherANorB = fMgr.getIDEFileOrNull(nameNeitherANorB);
			assertNull(neitherANorB);
			
			File onlyA = fMgr.getIDEFileOrNull(nameOnlyA);
			assertNotNull(onlyA);
			assertTrue(onlyA.exists());
			assertTrue(hasParentDir(onlyA, "a"));
			
			File onlyB = fMgr.getIDEFileOrNull(nameOnlyB);
			assertNotNull(onlyB);
			assertTrue(onlyB.exists());
			assertTrue(hasParentDir(onlyB, "b"));
			
			File aAndB = fMgr.getIDEFileOrNull(nameAAndB);
			assertNotNull(aAndB);
			assertTrue(aAndB.exists());
			assertTrue(hasParentDir(aAndB, "a"));
			
		}
		
		{
			File neitherANorB = fMgr.getIDEFile(nameNeitherANorB);
			assertNotNull(neitherANorB);
			assertFalse(neitherANorB.exists());
			
			File onlyA = fMgr.getIDEFile(nameOnlyA);
			assertNotNull(onlyA);
			assertTrue(onlyA.exists());
			assertTrue(hasParentDir(onlyA, "a"));
			
			File onlyB = fMgr.getIDEFile(nameOnlyB);
			assertNotNull(onlyB);
			assertTrue(onlyB.exists());
			assertTrue(hasParentDir(onlyB, "b"));
			
			File aAndB = fMgr.getIDEFile(nameAAndB);
			assertNotNull(aAndB);
			assertTrue(aAndB.exists());
			assertTrue(hasParentDir(aAndB, "a"));
		}
		
		{
			File neitherANorB = fMgr.getIDEFileOrNull(nameNeitherANorB);
			assertNull(neitherANorB);
			
			File onlyA = fMgr.getIDEFileOrNull(nameOnlyA);
			assertNotNull(onlyA);
			assertTrue(onlyA.exists());
			assertTrue(hasParentDir(onlyA, "a"));
			
			File onlyB = fMgr.getIDEFileOrNull(nameOnlyB);
			assertNotNull(onlyB);
			assertTrue(onlyB.exists());
			assertTrue(hasParentDir(onlyB, "b"));
			
			File aAndB = fMgr.getIDEFileOrNull(nameAAndB);
			assertNotNull(aAndB);
			assertTrue(aAndB.exists());
			assertTrue(hasParentDir(aAndB, "a"));
		}
		
		{
			File neitherANorB = fMgr.getIDEFile(nameNeitherANorB);
			assertNotNull(neitherANorB);
			assertFalse(neitherANorB.exists());
			
			File onlyA = fMgr.getIDEFile(nameOnlyA);
			assertNotNull(onlyA);
			assertTrue(onlyA.exists());
			assertTrue(hasParentDir(onlyA, "a"));
			
			File onlyB = fMgr.getIDEFile(nameOnlyB);
			assertNotNull(onlyB);
			assertTrue(onlyB.exists());
			assertTrue(hasParentDir(onlyB, "b"));
			
			File aAndB = fMgr.getIDEFile(nameAAndB);
			assertNotNull(aAndB);
			assertTrue(aAndB.exists());
			assertTrue(hasParentDir(aAndB, "a"));
		}
    }
    
	private void ensureExistence(File file) throws IOException {
		if ( ! file.exists()) {
			file.createNewFile();
		}
		assertTrue(file.exists());
		file.deleteOnExit();
	}
	
	private void ensureNonExistence(File file) {
		if (file.exists()) {
			file.delete();
		}
		assertFalse(file.exists());
	}
	
	private static boolean hasParentDir(File file, String parentName) {
		File parent = file.getParentFile();
		if (parent == null) {
			return false;
		}
		if (parent.getName().equals(parentName)) {
			return true;
		} else {
			return hasParentDir(parent, parentName);
		}
	}
    
    /**
     * Test for void FileManager(String[])
     */
    public void testFileManagerStringArray() throws Exception{
		String testFolderPath = ModuleLayoutConstants.SRC_TEST_DIR + File.separatorChar + "test";
		FileManager fMgr = MultiFileManager.createMultiFileManager(
			".",
			"./" + testFolderPath);
		FileManager.setInstance(fMgr);
		File f =
			fMgr.getIDEFileOrNull(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestFileManager.java");
        assertTrue(f.exists());
		f = fMgr.getIDEFileOrNull("com/top_logic/basic/TestFileManager.java");
        assertTrue(f.exists());
		f = fMgr.getIDEFileOrNull(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestNothing.strange");
        assertNull(f);
		f = fMgr.getIDEFileOrNull("com/top_logic/basic/TestNothing.strange");
        assertNull(f);
		f = fMgr.getIDEFile(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestNothing.strange");
        assertFalse(f.exists());
        f = fMgr.getIDEFile("com/top_logic/basic/TestNothing.strange");
        assertFalse(f.exists());
        String path = f.getPath();
		assertTrue(path + " ist not first one", path.indexOf(testFolderPath) < 0);
        
		InputStream is =
			fMgr.getStream(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestFileManager.java");
        try {
        	assertTrue(is.available() > 0);
		} finally {
			is.close();
		}
        is = fMgr.getStream("com/top_logic/basic/TestFileManager.java");
        try {
        	assertTrue(is.available() > 0);
		} finally {
			is.close();
		}
    }

    /**
     * test that the file manager behaves correctly when directories are called
     */
    public void testFileManagerDirectory () throws Exception {
		FileManager manager =
			MultiFileManager.createMultiFileManager(
				"./" + ModuleLayoutConstants.SRC_MAIN_DIR + "/com",
				"./" + ModuleLayoutConstants.SRC_TEST_DIR + "/test/com");
		FileManager.setInstance(manager);
		File file = manager.getIDEFileOrNull("top_logic/basic/TestFileManager.java");
        assertTrue(file.exists());
		file = manager.getIDEFileOrNull("top_logic/basic/TestFileManager.javac");
        assertNull (file);
		File dir = manager.getIDEFileOrNull("top_logic/basic");
        assertTrue (dir.exists());
        assertTrue (dir.isDirectory());
		dir = manager.getIDEFileOrNull("top_logics/blubber");
        assertNull (dir);
        dir = manager.getIDEFile("top_logics/blubber");
        String dirName = dir.getPath();
        // ensure that the (non existing) directory is located in the first path structure!
        assertTrue (dirName.indexOf("test") == -1);
        assertFalse(dir.exists());
        assertFalse(dir.isDirectory());
    }
    
    public static Test suite() {
		return new FileManagerTestSetup(new TestSuite(TestMultiFileManager.class));
    }

}
