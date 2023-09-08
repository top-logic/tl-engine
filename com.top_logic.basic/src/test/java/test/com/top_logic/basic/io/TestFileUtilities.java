/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections4.CollectionUtils;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Test class to check the {@link com.top_logic.basic.io.FileUtilities} class.
 * 
 * TODO KHA extract TestStreamUtilities
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
@SuppressWarnings("javadoc")
public class TestFileUtilities extends BasicTestCase {

    /** Counter to adjust timing */
    private static final int COUNT = 4096;

    /** temporary file containing some testData */
    private static File tmpFile;

    /** largeString containing TestData */
    private static String largeString;

    /** Prepare for the test by creating a temporary file.
     */
    @Override
	protected void setUp () throws Exception {
    	super.setUp();
    	
        tmpFile = BasicTestCase.createTestFile("TestFileUtilities", "txt");
        
        StringBuffer largeBuf = new StringBuffer(COUNT * 256);
        
        for (int i=0; i < COUNT; i++) {
            // It will NOT work when using \n\r or \r I know 
            largeBuf.append("This is a dummy Text to fill this file with some Data\n");
            largeBuf.append("0123456789 the quick brown fox jumps over the lazy dogs back.\n");
                          // Ä     Ö     Ü     ä     ö     ü     ß     ê      Á     Ò 
            largeBuf.append("\u00c4\u00d6\u00dc\u00e5\u00f6\u00fc\u00df\u00ea\u00cA\u00d2 ");
            largeBuf.append("THE QUICK BROWN FOX JUMPS OVER THE LAZY DOGS BACK:\n");
        }
        largeString = largeBuf.toString();
        // System.out.println(largeBuf.length());
    }
    
    @Override
    protected void tearDown() throws Exception {
    	tmpFile.delete();
    	largeString = null;
    	
    	super.tearDown();
    }

	public void testListFiles() throws IOException {
		File dir = BasicTestCase.createdCleanTestDir("TestFileUtilities");
		new File(dir, "contents").mkdir();

		assertEquals(set("contents"), names(FileUtilities.listFiles(dir)));
		assertEquals(set("contents"), set(FileUtilities.list(dir)));
		assertEquals(set(), names(FileUtilities.listFiles(dir, nameFilter())));
		assertEquals(set(), set(FileUtilities.list(dir, nameFilter())));
		assertEquals(set(), names(FileUtilities.listFiles(dir, fileFilter())));
	}

	public void testListFilesNotExists() {
		File doesNotExist = new File(BasicTestCase.createdCleanTestDir("TestFileUtilities"), "doesNotExist");
		assertFalse(doesNotExist.exists());

		try {
			FileUtilities.listFiles(doesNotExist);
			fail("Failure expected.");
		} catch (IOException ex) {
			assertContains("does not exist", ex.getMessage());
		}
		try {
			FileUtilities.list(doesNotExist);
			fail("Failure expected.");
		} catch (IOException ex) {
			assertContains("does not exist", ex.getMessage());
		}
		try {
			FileUtilities.listFiles(doesNotExist, nameFilter());
			fail("Failure expected.");
		} catch (IOException ex) {
			assertContains("does not exist", ex.getMessage());
		}
		assertEquals(set(), FileUtilities.listFilesSafe(doesNotExist, nameFilter()));
		try {
			FileUtilities.list(doesNotExist, nameFilter());
			fail("Failure expected.");
		} catch (IOException ex) {
			assertContains("does not exist", ex.getMessage());
		}
		try {
			FileUtilities.listFiles(doesNotExist, fileFilter());
			fail("Failure expected.");
		} catch (IOException ex) {
			assertContains("does not exist", ex.getMessage());
		}
	}

	public void testListFilesEmpty() throws IOException {
		File empty = new File(BasicTestCase.createdCleanTestDir("TestFileUtilities"), "empty");
		assertTrue(empty.mkdir());

		assertEquals(set(), names(FileUtilities.listFiles(empty)));
		assertEquals(set(), set(FileUtilities.list(empty)));
		assertEquals(set(), names(FileUtilities.listFiles(empty, nameFilter())));
		assertEquals(set(), set(FileUtilities.list(empty, nameFilter())));
		assertEquals(set(), names(FileUtilities.listFiles(empty, fileFilter())));
	}

	public void testListFilesRegularFile() throws IOException {
		File regularFile = BasicTestCase.createNamedTestFile("simpleFile");
		FileOutputStream out = new FileOutputStream(regularFile);
		out.write(42);
		out.close();
	
		try {
			FileUtilities.listFiles(regularFile);
			fail("Failure expected.");
		} catch (IOException ex) {
			assertContains("not a directory", ex.getMessage());
		}
		try {
			FileUtilities.list(regularFile);
			fail("Failure expected.");
		} catch (IOException ex) {
			assertContains("not a directory", ex.getMessage());
		}
		try {
			FileUtilities.listFiles(regularFile, nameFilter());
			fail("Failure expected.");
		} catch (IOException ex) {
			assertContains("not a directory", ex.getMessage());
		}
		try {
			FileUtilities.list(regularFile, nameFilter());
			fail("Failure expected.");
		} catch (IOException ex) {
			assertContains("not a directory", ex.getMessage());
		}
		try {
			FileUtilities.listFiles(regularFile, fileFilter());
			fail("Failure expected.");
		} catch (IOException ex) {
			assertContains("not a directory", ex.getMessage());
		}
	}

	private FilenameFilter nameFilter() {
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return false;
			}
		};
	}

	private FileFilter fileFilter() {
		return new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return false;
			}
		};
	}

	private Set<String> names(File[] files) {
		HashSet<String> result = new HashSet<>();
		for (File file : files) {
			String name = file.getName();
			if (name.startsWith(".")) {
				continue;
			}
			result.add(name);
		}
		return result;
	}

	/** Test the writeStringToFile function.
     */
    public void testWriteStringToFile () throws Exception
    {
        startTime();
        FileUtilities.writeStringToFile(largeString, tmpFile);
        logTime("writeStringToFile");
        
        String str = FileUtilities.readLinesFromFile(tmpFile);
        logTime("readLinesFromFile");
        assertEquals(largeString, str);
    }

    /** Test the readFileToString() function.
     */
    public void testReadFileToString () throws Exception
    {
        FileUtilities.writeStringToFile(largeString, tmpFile);
        
        startTime();
        String str = FileUtilities.readFileToString(tmpFile);
        logTime("readFileToString");
        assertEquals(largeString, str);
    }

    /** Test the CopyFile() functions.
     */
    public void testCopyFile () throws Exception
    {
        FileUtilities.writeStringToFile(largeString, tmpFile);
        
        File file2 = new File("tmp/TestFileUtilities2.dat");
        file2.deleteOnExit(); // Just in case
        
        long time = tmpFile.lastModified();
    
        startTime();
		InputStream input = new FileInputStream(tmpFile);
        try {
			FileUtilities.copyToFile(input, file2);
		} finally {
			input.close();
		}
        logTime("copyFile (FileInputStream, File)");

        String str = FileUtilities.readFileToString(file2);
        assertEquals(largeString, str);
        file2.delete();

        startTime();
        FileUtilities.copyFile(tmpFile, file2);
        logTime("copyFile (File, File)");
        assertEquals("Modifcation Time not copied", time, file2.lastModified());
        file2.delete();

        startTime();
        FileUtilities.copyFile(tmpFile, file2, true);
        logTime("copyFile (File, File, true)");
        assertEquals("Modifcation Time not copied", time, file2.lastModified());
        file2.delete();

        startTime();
        FileUtilities.copyFile(tmpFile, file2, false);
        logTime("copyFile (File, File, false)");
        file2.delete();
}

    /** Test the copyReaderWriter() function.
     */
    public void testCopyReaderWriter () throws Exception
    {
        FileUtilities.writeStringToFile(largeString, tmpFile);
        
        File file2 = new File("tmp/TestFileUtilities2.dat");
        file2.deleteOnExit(); // Just in case
    
        startTime();
        Reader reader = new InputStreamReader ( 
                         new FileInputStream(tmpFile), FileUtilities.ENCODING);
        try {
        	Writer writer = new OutputStreamWriter (  
        			new FileOutputStream(file2)  ,FileUtilities.ENCODING);
			try {
				FileUtilities.copyReaderWriterContents(reader, writer);
			} finally {
				writer.close();
			}
		} finally {
			reader.close();
		}
        logTime("copyReaderWriter");

        String str = FileUtilities.readFileToString(file2);
        assertEquals(largeString, str);
        file2.delete();
    }

    /** Test the extension Filer
     */
    public void testExtensions() throws Exception
    {
    	File dir = BasicTestCase.createdCleanTestDir("testExtensions");
    	
    	File a = new File(dir, "a.txt");
    	a.createNewFile();
    	File b = new File(dir, "b");
    	b.mkdir();
    	File c = new File(b, "c.txt");
    	c.createNewFile();
    	File d = new File(b, "d");
    	d.mkdir();
    	File e = new File(dir, "e.txt");
    	e.createNewFile();
    	File f = new File(dir, "f.pdf");
    	f.createNewFile();

        String list[] = FileUtilities.getFilesEndingWith(dir, ".txt");
        assertEquals(2, list.length);
		Set<String> result = toSet(Arrays.asList(list));
		assertFalse("c.txt is contained in sub directory.", result.contains("c.txt"));
		assertEquals(set("a.txt", "e.txt"), result);
    }

    /** 
     * test reading and writing from Strings.
     */
    public void testStrings () throws IOException 
    {
        File strFile = BasicTestCase.createNamedTestFile("testStringMethods.txt");
        String str   = "Blah\nBlubber\nMBA Pörsön\nBlurks";
        FileUtilities.writeStringToFile(str, strFile);
        
        List strL = FileUtilities.readWordsFromFile(strFile);
        assertEquals(5, strL.size());
        assertEquals("Blah"  , strL.get(0));
        assertEquals("Blurks", strL.get(4));
        
        strL = FileUtilities.readWordsFromFile(strFile.getPath());
        assertEquals(5, strL.size());
        assertEquals("Blubber"  , strL.get(1));
        assertEquals("Pörsön"   , strL.get(3));

        // Will not work outside Windoof ...
        final FileInputStream is = new FileInputStream(strFile);
        String str2;
        try {
			str2 = FileUtilities.readAllFromStream(is);
		} finally {
			is.close();
		}
        assertEquals(str, str2);

        str2 = FileUtilities.readLinesFromFile(strFile);
        assertEquals(str + '\n', str2);

        str2 = FileUtilities.readLinesFromFile(strFile.getPath());
        assertEquals(str + '\n', str2);
    }

    /** 
     * Test {@link  com.top_logic.basic.io.FileUtilities#copyR(File, File) }
     * 
     * and its variants. 
     */
    public void testCopyR () throws Exception
    {   
    	File tmp1 = BasicTestCase.createdCleanTestDir("tmp1");
    	
    	File a = new File(tmp1, "a.txt");
    	a.createNewFile();
    	File b = new File(tmp1, "b");
    	b.mkdir();
    	File c = new File(b, "c.txt");
    	c.createNewFile();
    	File d = new File(b, "d");
    	d.mkdir();
    	
        File tmp2 = BasicTestCase.createdCleanTestDir("tmp2");
        
        FileUtilities.enforceDirectory(tmp2.getPath());
        
        if (tmp2.exists())
            assertTrue(FileUtilities.deleteR(tmp2)); 

        FileUtilities.enforceDirectory(tmp2.getPath());
        assertTrue(tmp2.exists() && tmp2.isDirectory());
        assertTrue(FileUtilities.deleteR(tmp2)); 

		assertTrue(FileUtilities.copyR  (tmp1, tmp2)); 
        assertTrue(FileUtilities.deleteR(tmp2)); 

        assertTrue(FileUtilities.copyR  (tmp1, tmp2, true)); 
        assertTrue(FileUtilities.deleteR(tmp2)); 

        assertTrue(FileUtilities.copyR  (tmp1, tmp2, false)); 
        assertTrue(FileUtilities.deleteR(tmp2)); 

        FileOutputStream fos = new FileOutputStream(tmp2);
        Logger.configureStdout("FATAL");    // The error is quite OK
        assertFalse(FileUtilities.copyR  (tmp1, tmp2, false)); 
        Logger.configureStdout("ERROR");    // The error is quite OK
        fos.close();
        assertTrue(FileUtilities.deleteR(tmp2)); 
        
    }

    /** 
     * Test the FileUtilities.equalsFile()
     */
    public void testEqualsFile() throws Exception
    {
        // test with to different files smaller than the BufferSize
		File file1 = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/io/data/file1.dat");
		File file2 = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/io/data/file2.dat");

        assertTrue(FileUtilities.equalsFile(file1, file1));
        assertTrue(FileUtilities.equalsFile(file2, file2));

        assertFalse(FileUtilities.equalsFile(file1, file2));
        assertFalse(FileUtilities.equalsFile(file2, file1));
        
        // test with too different files with just around BUF_LEN size
        int BUF_LEN = 4096;
        String string1 = StringServices.getRandomString(BUF_LEN);
        String string2 = string1+"a";
        file1 = getTestFileWithContent("TestFile1", string1);
        file2 = getTestFileWithContent("TestFile2", string2);
        
        assertTrue(FileUtilities.equalsFile(file1, file1));
        assertTrue(FileUtilities.equalsFile(file2, file2));
        
        assertFalse(FileUtilities.equalsFile(file1, file2));
        assertFalse(FileUtilities.equalsFile(file2, file1));
        
        // one more test with even larger files
        string1 = StringServices.getRandomString(10 * BUF_LEN);
        string2 = string1+"a";
        file1 = getTestFileWithContent("TestFile1", string1);
        file2 = getTestFileWithContent("TestFile2", string2);
        
        assertTrue(FileUtilities.equalsFile(file1, file1));
        assertTrue(FileUtilities.equalsFile(file2, file2));
        
        assertFalse(FileUtilities.equalsFile(file1, file2));
        assertFalse(FileUtilities.equalsFile(file2, file1));
    }
    
    
    private File getTestFileWithContent(String aName, String aContent) throws IOException {
        
        File result = BasicTestCase.createNamedTestFile(aName);
        
        result.deleteOnExit(); // Just to be sure ....
        
        return FileUtilities.writeStringToFile(aContent, result);
    }
    
	/**
	 * Test for {@link FileUtilities#urlToFile(java.net.URL)}
	 */
	public void testUrlToFile() throws Throwable {
		File relativeFile = new File("File with spaces");
		relativeFile.createNewFile();
		File canonicalFile = relativeFile.getCanonicalFile();
		assertEquals("Converting an URL to a file failed, as an unexpected file was returned.",
			canonicalFile, FileUtilities.urlToFile(relativeFile.toURI().toURL()).getCanonicalFile());
		assertEquals("Converting an URL to a file failed, as an unexpected file was returned.",
			canonicalFile, FileUtilities.urlToFile(canonicalFile.toURI().toURL()));
		relativeFile.delete();
    }

	public void testDeleteR() throws IOException {
		File dir = createNamedTestFile("folder");
		dir.mkdir();
		File dir1 = new File(dir, "dir1");
		dir1.mkdir();
		new File(dir1, "f1").createNewFile();
		new File(dir, "dir2").mkdir();
		assertTrue(FileUtilities.deleteR(dir));
		assertFalse(dir1.exists());
		deleteTestDir();
	}

	public void testDeleteContents() throws IOException {
		File dir = createNamedTestFile("folder");
		dir.mkdir();
		File dir1 = new File(dir, "dir1");
		dir1.mkdir();
		new File(dir1, "f1").createNewFile();
		new File(dir, "dir2").mkdir();
		assertTrue(FileUtilities.deleteContents(dir));
		assertTrue(dir.exists());
		assertSame(0, dir.listFiles().length);
		deleteTestDir();
	}

	/** Tests for {@link FileUtilities#getSafeDetailedPath(File)} */
	public void testGetSafeDetailedPath() {
		assertNotNull(FileUtilities.getSafeDetailedPath((File) null));
		assertNotNull(FileUtilities.getSafeDetailedPath((Path) null));
		assertNotNull(FileUtilities.getSafeDetailedPath(new File(".")));
		assertNotNull(FileUtilities.getSafeDetailedPath(Paths.get(".")));
		assertContains("I don't exist!", FileUtilities.getSafeDetailedPath(new File("I don't exist!")));
		assertContains("I don't exist!", FileUtilities.getSafeDetailedPath(Paths.get("I don't exist!")));
	}

	public void testCheckCase() throws IOException {
		File root = BasicTestCase.createdCleanTestDir("testCheckCase");
		File dir = new File(root, "Foo/bar");
		File file = new File(dir, "baz.xml");
		dir.mkdirs();
		try (FileOutputStream out = new FileOutputStream(file)) {
			// Just touch file.
		}
		doCheck(root, "Foo/bar/baz.xml");
		doCheckFail(root, "Foo/Bar/baz.xml");
		doCheckFail(root, "Foo/bar/bAz.xml");
		doCheckFail(root, "Foo/bar/baz.XML");
	}

	private void doCheck(File root, String name) throws IOException {
		File file = new File(root, name);
		assertTrue(file.exists());
		FileUtilities.checkCase(name, file);
	}

	private void doCheckFail(File root, String name) {
		File file = new File(root, name);
		if (!file.exists()) {
			// Test is irrelevant on a case-sensitive file system.
			return;
		}
		try {
			FileUtilities.checkCase(name, file);
			fail("Must fail.");
		} catch (IOException ex) {
			// Expected.
		}
	}

	public void testReplaceCharacter() {
		assertSame("bar.xml", FileUtilities.replaceSeparator("bar.xml", '.'));
		assertSame("", FileUtilities.replaceSeparator("", '.'));
		assertEquals("Foo.Bar.baz.xml", FileUtilities.replaceSeparator("Foo/Bar/baz.xml", '.'));
		assertEquals("Foo/Bar/baz.xml", FileUtilities.replaceSeparator("Foo\\Bar\\baz.xml", '/'));
		assertEquals("Foo=Bar=baz.xml", FileUtilities.replaceSeparator("Foo/Bar\\baz.xml", '='));
		assertSame("Foo/Bar/baz.xml", FileUtilities.replaceSeparator("Foo/Bar/baz.xml", '/'));
	}

	public void testPrefixedFilenames() {
		File file = BasicTestCase.createNamedTestFile("prefixedFilename.xml");
		String prefix = "foo/bar";
		Set<String> prefixedFilenames = FileUtilities.getPrefixedFilenames(Collections.singleton(file), prefix);
		String prefixedFilename = CollectionUtils.extractSingleton(prefixedFilenames);
		assertEquals(prefix + FileUtilities.PATH_SEPARATOR + file.getName(), prefixedFilename);
	}

	public void testFilename() {
		assertEquals("c.txt", FileUtilities.getFilenameOfResource("a/b/c.txt"));
		assertEquals("a.txt", FileUtilities.getFilenameOfResource("a.txt"));
		assertEquals("b", FileUtilities.getFilenameOfResource("a/b"));
		assertEquals("b", FileUtilities.getFilenameOfResource("a/b/"));

		assertEquals("c.txt", FileUtilities.getFilename("a\\b\\c.txt", "\\"));
		assertEquals("a.txt", FileUtilities.getFilename("a.txt", "\\"));
		assertEquals("b", FileUtilities.getFilename("a\\b", "\\"));
		assertEquals("b", FileUtilities.getFilename("a\\b\\", "\\"));
	}

	public void testBasename() {
		assertEquals("c", FileUtilities.getBasename("a/b/c.txt", "/"));
		assertEquals("a", FileUtilities.getBasename("a.txt", "/"));
		assertEquals("b", FileUtilities.getBasename("a/b", "/"));
		assertEquals("b", FileUtilities.getBasename("a/b/", "/"));

		assertEquals("c", FileUtilities.getBasename("a\\b\\c.txt", "\\"));
		assertEquals("a", FileUtilities.getBasename("a.txt", "\\"));
		assertEquals("b", FileUtilities.getBasename("a\\b", "\\"));
		assertEquals("b", FileUtilities.getBasename("a\\b\\", "\\"));
	}

	public void testFileExtension() {
		assertEquals("a", FileUtilities.removeFileExtension("a.txt"));
		assertEquals("a", FileUtilities.removeFileExtension("a"));
	}

	public void testCombinedPaths() {
		Path path = Path.of("a", "b", "c");

		assertEquals("a/b/c", FileUtilities.getCombinedPath(path, "/"));
		assertEquals("a\\b\\c", FileUtilities.getCombinedPath(path, "\\"));
	}

    /** 
     * Return the suite of tests to execute.
     */
    public static Test suite () {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestFileUtilities.class));
    }

}
