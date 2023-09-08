/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.io.EmptyInputStream;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.NullWriter;
import com.top_logic.basic.io.StreamUtilities;

/**
 * Test class to check the {@link StreamUtilities} class.
 * 
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestStreamUtilities extends BasicTestCase {

    /** Counter to adjust timing */
    private static final int COUNT = 4096;

    /** temporary file containing some testData */
    private static File tmpFile;

    /**
     * Constructor to conduct a special test.
     *
     * @param name name of the test to execute.
     */
    public TestStreamUtilities (String name) {
        super (name);
    }

    /** Prepare for the test by creating a temporary file.
     */
    @Override
	protected void setUp () throws Exception {
    	super.setUp();
        
        tmpFile = BasicTestCase.createTestFile("TestStreamUtilities", "txt");
        
        StringBuffer largeBuf = new StringBuffer(COUNT * 256);
        
        for (int i=0; i < COUNT; i++) {
            // It will NOT work when using \n\r or \r I know 
            largeBuf.append("This is a dummy Text to fill this file with some Data\n");
            largeBuf.append("0123456789 the quick brown fox jumps over the lazy dogs back.\n");
                          // Ä     Ö     Ü     ä     ö     ü     ß     ê      Á     Ò 
            largeBuf.append("\u00c4\u00d6\u00dc\u00e5\u00f6\u00fc\u00df\u00ea\u00cA\u00d2 ");
            largeBuf.append("THE QUICK BROWN FOX JUMPS OVER THE LAZY DOGS BACK:\n");
        }
        FileUtilities.writeStringToFile( largeBuf.toString(), tmpFile);
        // System.out.println(largeBuf.length());
    }
    
    @Override
    protected void tearDown() throws Exception {
    	tmpFile.delete();
    	
    	super.tearDown();
    }

    /** Special Way to compare long String */
    public static void asertLongStringEquals(String s1, String s2) {
        int l = s1.length();
        if (l != s2.length())
            fail("String length differs " + l + "," + s2.length());
        
        for (int i=0; i < l; i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            if (c1 != c2)
                fail ("Strings differ at " + i + " '" + c1 + "','" + c2 
                      + "' (" + (int) c1 + "," + (int) c2 + ")");
        }
        
    }

    // Test methodes

    /**
     * Tests {@link StreamUtilities#copyStreamContents(InputStream, java.io.OutputStream)}.
     */
    public void testCopyStreamContents() throws IOException {
    	ByteArrayOutputStream tmp = new ByteArrayOutputStream();
    	try {
    		final TestingDataStream aSource = new TestingDataStream(8000, 1001, 42);
    		try {
    			StreamUtilities.copyStreamContents(aSource, tmp);
    			assertEquals(8000, tmp.toByteArray().length);
			} finally {
				aSource.close();
			}
		} finally {
			tmp.close();
		}
    }
    
    /**
	 * Test {@link StreamUtilities#equalsStreamContents(InputStream, InputStream)} with an empty
	 * stream.
	 */
    public void testEqualsStreamEmpty() throws IOException {
    	InputStream testStream = new TestingDataStream(0);
    	assertTrue(StreamUtilities.equalsStreamContents(testStream, new TestingDataStream(0)));
    }
    
    /**
	 * Test {@link StreamUtilities#equalsStreamContents(InputStream, InputStream)} with a stream of
	 * size 1.
	 */
	public void testEqualsStreamTrivial() throws IOException {
		InputStream testStream = new TestingDataStream(1);
    	assertTrue(StreamUtilities.equalsStreamContents(testStream, new TestingDataStream(1)));
	}

	/**
	 * Test {@link StreamUtilities#equalsStreamContents(InputStream, InputStream)} with a stream of
	 * size < buffer size but with different chunking behavior.
	 */
	public void testEqualsStreamChunk() throws IOException {
		InputStream testStream = new TestingDataStream(100, 1, 42);
    	assertTrue(StreamUtilities.equalsStreamContents(testStream, new TestingDataStream(100, 9, 42)));
    }

	/**
	 * Test {@link StreamUtilities#equalsStreamContents(InputStream, InputStream)} with a stream of
	 * size > buffer size but with different chunking behavior.
	 */
	public void testEqualsStreamChunkLarge() throws IOException {
		InputStream testStream = new TestingDataStream(8000, 1001, 42);
		assertTrue(StreamUtilities.equalsStreamContents(testStream, new TestingDataStream(8000, 2001, 42)));
	}
	
	/**
	 * Test {@link StreamUtilities#equalsReaderContent(Reader, Reader)} with a various sizes and
	 * different chunking behavior.
	 */
	public void testEqualsReader() throws IOException {
		Reader firstReader;
		Reader secondReader;

		firstReader = new TestingDataReader(0);
		secondReader = new TestingDataReader(0);
		assertEqualsRead(firstReader, secondReader);

		firstReader = new TestingDataReader(1);
		secondReader = new TestingDataReader(1);
		assertEqualsRead(firstReader, secondReader);

		firstReader = new TestingDataReader(4000, 101, 42);
		secondReader = new TestingDataReader(4000, 101, 42);
		assertEqualsRead(firstReader, secondReader);

		firstReader = new TestingDataReader(4000, 101, 42);
		secondReader = new TestingDataReader(4000, 201, 42);
		assertEqualsRead(firstReader, secondReader);

		firstReader = new TestingDataReader(8000, 6001, 42);
		secondReader = new TestingDataReader(8000, 6001, 42);
		assertEqualsRead(firstReader, secondReader);

		firstReader = new TestingDataReader(8000, 101, 42);
		secondReader = new TestingDataReader(8000, 201, 42);
		assertEqualsRead(firstReader, secondReader);
	}

	private void assertEqualsRead(Reader firstReader, Reader secondReader) throws IOException {
		assertTrue(StreamUtilities.equalsReaderContent(firstReader, secondReader));
		assertEquals("Not all characters consumed.", -1, firstReader.read());
		assertEquals("Not all characters consumed.", -1, secondReader.read());
	}
	
	/**
	 * Test {@link StreamUtilities#copyReaderWriterContents(Reader, Writer)}.
	 */
	public void testCopyReaderWriterContents() throws IOException {
		StringWriter result = new StringWriter();
		try {
			final TestingDataReader aSource = new TestingDataReader(8000, 101, 42);
			try {
				StreamUtilities.copyReaderWriterContents(aSource, result);
				assertEquals(8000, result.getBuffer().length());
				assertTrue(StreamUtilities.equalsReaderContent(new StringReader(result.getBuffer().toString()), new TestingDataReader(
						8000, 201, 42)));
			} finally {
				aSource.close();
			}
		} finally {
			result.close();
		}
	}
	
    /** Test the copyReaderWriter() function.
     */
    public void testCopyReaderWriter () throws Exception
    {
        File file2 = new File("tmp/TestStreamUtilities2.dat");
        file2.deleteOnExit();
		InputStream input = new FileInputStream(tmpFile); // Just in case
        
        try {
			FileUtilities.copyToFile(input, file2);
		} finally {
			input.close();
		}

        startTime();
        Reader reader = new InputStreamReader ( 
                         new FileInputStream(tmpFile), StreamUtilities.ENCODING);
        try {
        	Writer writer = new OutputStreamWriter (  
        			new FileOutputStream(file2)  ,StreamUtilities.ENCODING);
			try {
				StreamUtilities.copyReaderWriterContents(reader, writer);
			} finally {
				writer.close();
			}
		} finally {
			reader.close();
		}
        logTime("copyReaderWriter");
    }

    /** Test the copyReaderWriter() function with a {@link NullWriter}.
     */
    public void testNullReaderWriter () throws Exception
    {
        startTime();
        Reader reader = new InputStreamReader ( 
                         new FileInputStream(tmpFile), StreamUtilities.ENCODING);
        try {
        	Writer writer = NullWriter.INSTANCE;
			try {
				StreamUtilities.copyReaderWriterContents(reader, writer);
			} finally {
				writer.close();
			}
		} finally {
			reader.close();
		}
        logTime("NullReaderWriter");
    }

    /** 
     * test reading and writing from Strings.
     */
    public void testStrings () throws IOException 
    {
        File strFile = BasicTestCase.createNamedTestFile("testStringMethods.txt");
        String str   = "Blah\nBlubber\nMBA Pörsön\nBlurks";
        FileUtilities.writeStringToFile(str, strFile);

        // Will not work outside Windoof ...
        final FileInputStream is = new FileInputStream(strFile);
        String str2;
        try {
			str2 = StreamUtilities.readAllFromStream(is);
		} finally {
			is.close();
		}
        assertEquals(str, str2);
    }

	/**
	 * Test {@link StreamUtilities#size(InputStream)}.
	 */
	public void testSize() throws IOException {
		assertEquals(0, StreamUtilities.size(EmptyInputStream.INSTANCE));
		assertEquals(15, StreamUtilities.size(new ByteArrayInputStream(new byte[15])));

		final long longSize = ((long) Integer.MAX_VALUE) + 15;
		assertEquals(longSize, StreamUtilities.size(new InputStream() {

			long l;

			@Override
			public int read() throws IOException {
				if (l >= longSize) {
					return -1;
				}
				l++;
				return 5;
			}

		}));
	}

	/**
	 * Simple test for {@link StreamUtilities#storeNormalized(java.io.OutputStream, Properties)}.
	 */
	public void testStoreNormalized() throws IOException {
		Properties props = new Properties();
		props.put("b", "b");
		props.put("a1", "ü");
		props.put("c", "ä");
		props.put("A", "ß");
		ByteArrayOutputStream actual = new ByteArrayOutputStream();
		StreamUtilities.storeNormalized(actual, props);

		Properties loaded = new Properties();
		loaded.load(new ByteArrayInputStream(actual.toByteArray()));
		assertEquals("Serialized must semantically be equal.", props, loaded);

		StringWriter expected = new StringWriter();
		try (BufferedWriter bw = new BufferedWriter(expected)) {
			bw.write("A=\\u00DF");
			bw.newLine();
			bw.write("a1=\\u00FC");
			bw.newLine();
			bw.write("b=b");
			bw.newLine();
			bw.write("c=\\u00E4");
			bw.newLine();
		}
		assertEquals("Unexpected content.", expected.toString(), new String(actual.toByteArray(), "ISO-8859-1"));
	}

    /** 
     * Return the suite of tests to execute.
     */
    public static Test suite () {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestStreamUtilities.class));
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        SHOW_TIME = true;
        junit.textui.TestRunner.run (suite ());
    }

}
