/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.MultiFileManager;

/**
 * Test case for {@link MultiFileManager}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestMultiLoaderFileManager extends BasicTestCase {

	private static final String ISO_8859_1 = "ISO-8859-1";

	/**
	 * Simple case, FileManager for current directory.
	 */
    public void testSimple() throws Exception {
		File root = createdCleanTestDir("simple");
		FileManager mlfm = MultiFileManager.createMultiFileManager(root.toPath());
		FileManager.setInstance(mlfm);
        assertNotNull(mlfm.toString());
		String testPath = "does/not/exist.txt";
        File   file1     = new File(root, testPath).getCanonicalFile();
        File   file2     = mlfm.getIDEFile(testPath);
		File file3 = mlfm.getIDEFile("file://" + root.getPath() + "/" + testPath);

		assertEqualsFile(file1, file2);
		assertEqualsFile(file1, file3);
    }

    /**
     * Normal Case, use complex properties.
     */
    public void testNormal() throws Exception {
		File root = createdCleanTestDir("normal");

		String fooPath = "foo";
		String barPath = "dir/bar";
		String foobarPath = "dir/foobar";

		File base1 = mkdir(root, "webapp1");
		File base1Foo = file(base1, fooPath);
		File base1Bar = file(base1, barPath);

		File base2 = mkdir(root, "webapp2");
		File base2Foo = file(base2, fooPath);
		File base2Bar = file(base2, barPath);
		File base2Foobar = new File(base2, foobarPath).getCanonicalFile();

		File base3 = mkdir(root, "webapp3");
		File base3Foo = new File(base3, fooPath).getCanonicalFile();
		File base3Bar = new File(base3, barPath).getCanonicalFile();
		File base3Foobar = new File(base3, foobarPath).getCanonicalFile();

		assertTrue(base1Foo.exists());
		assertTrue(base1Bar.exists());
		assertFalse(base2Foobar.exists());
		assertFalse(base3Foo.exists());
		assertFalse(base3Bar.exists());

		FileManager fileManager =
			MultiFileManager.createMultiFileManager(base3.toPath(), base2.toPath(), base1.toPath());
		FileManager.setInstance(fileManager);

		assertEqualsFile(base2Foo, fileManager.getIDEFileOrNull(fooPath));
		assertEqualsFile(base2Foo, fileManager.getIDEFile(fooPath));

		assertEqualsFile(base2Bar, fileManager.getIDEFileOrNull(barPath));
		assertEqualsFile(base2Bar, fileManager.getIDEFile(barPath));

		assertEquals(null, fileManager.getIDEFileOrNull(foobarPath));
		assertEqualsFile(base3Foobar, fileManager.getIDEFile(foobarPath));
    }

	private void assertEqualsFile(File expected, File actual) throws IOException {
		assertEquals(expected, actual.getCanonicalFile());
	}

	public void testGetFiles() throws UnsupportedEncodingException, IOException {
		File root = createdCleanTestDir("getFiles");

		String fooPath = "foo";
		String barPath = "bar";

		File base1 = mkdir(root, "webapp1");
		File base1Foo = file(base1, fooPath);
		File base1Bar = file(base1, barPath);

		File base2 = mkdir(root, "webapp2");
		File base2Foo = file(base2, fooPath);

		File base3 = mkdir(root, "webapp3");
		File base3Bar = file(base3, barPath);

		FileManager fileManager =
			MultiFileManager.createMultiFileManager(base3.toPath(), base2.toPath(), base1.toPath());
		FileManager.setInstance(fileManager);

		assertEquals(list(base3Bar, base1Bar), canonical(fileManager.getFiles(barPath)));
		assertEquals(list(base2Foo, base1Foo), canonical(fileManager.getFiles(fooPath)));
		assertEquals(list(), canonical(fileManager.getFiles("doesNotExist")));
	}

	private List<File> canonical(List<File> files) {
		return files.stream().map(this::canonical).collect(Collectors.toList());
	}

	private File canonical(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private File file(File base, String name) throws UnsupportedEncodingException, IOException {
		File result = new File(base, name).getCanonicalFile();
		File parent = result.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		FileOutputStream in = new FileOutputStream(result);
		in.write("contents".getBytes(ISO_8859_1));
		in.close();
		return result;
	}

	private File mkdir(File base, String name) {
		File result = new File(base, name);
		assertTrue(result.mkdir());
		return result;
	}
    
	/**
	 * Suite of tests.
	 */
    public static Test suite () {
		Test test = new FileManagerTestSetup(new TestSuite(TestMultiLoaderFileManager.class));

		return BasicTestSetup.createBasicTestSetup(test);
    }

}
