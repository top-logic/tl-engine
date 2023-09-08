/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Settings;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.util.Zipper;

/**
 * MGA did not specify what this class may be used for.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestZipper extends BasicTestCase {

    public static final String TEST_ZIP    = "tmp/test.zip";

	public static final String TEST_FOLDER = ModuleLayoutConstants.SRC_TEST_DIR + File.separatorChar
		+ TestZipper.class.getPackage().getName().replace('.', File.separatorChar);

    public void testAddFolder() throws Exception {
		int theFiles;
		try (Zipper theZipper = new Zipper(TEST_ZIP)) {
            theFiles = theZipper.addFolder(TEST_FOLDER);
            final String name = TestZipper.class.getName();
			final String fileName = name.substring(name.lastIndexOf('.') + 1) + ".java";
			File file = new File(TEST_FOLDER, fileName);
            theZipper.addFile(file, "tlxml");
		
			file = new File(TEST_FOLDER , fileName);
			theZipper.addFile(file, "alt.xml");            
        }
        assertTrue("No files zipped!", theFiles > 0);
        
        File theFile = new File(TEST_ZIP);
        assertTrue(theFile.exists());
        assertTrue(theFile.delete());
    }

	public void testEmptyFolder() throws Exception {
		File tempDir = Settings.getInstance().getTempDir();
		File dir = new File(tempDir, "TestZipper_testEmptyFolder");
		assertTrue(dir.mkdir());
		try {
			new File(dir, "file1").createNewFile();
			new File(dir, "folder1").mkdir();
			int theFiles;
			try (Zipper theZipper = new Zipper(TEST_ZIP)) {
				theFiles = theZipper.addFolder(dir);
			}
			assertEquals(1, theFiles);

			try (ZipInputStream in = new ZipInputStream(new FileInputStream(TEST_ZIP))) {
				ZipEntry entry1 = in.getNextEntry();
				assertEquals("TestZipper_testEmptyFolder/file1", entry1.getName());
				in.closeEntry();
				ZipEntry entry2 = in.getNextEntry();
				assertEquals("TestZipper_testEmptyFolder/folder1/", entry2.getName());
				assertTrue(entry2.isDirectory());
				in.closeEntry();
				assertNull(in.getNextEntry());
			}
		} finally {
			FileUtilities.deleteR(dir);
		}
	}

    public static Test suite () {
        return TLTestSetup.createTLTestSetup(TestZipper.class);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
