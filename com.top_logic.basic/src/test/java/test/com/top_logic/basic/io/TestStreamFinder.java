/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.io.StreamFinder;

/**
 * Test class to check the {@link com.top_logic.basic.io.StreamFinder} class.
 *
 * @author  <a href=mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestStreamFinder extends TestCase {

    /** This text is part of a large temporaray file */
    public static final String SEARCH = "The text to search is here";

    /** This text is NOT part of a large temporaray file */
    public static final String NO_SEARCH = "The text to search is NOT here !";

    /** This text is part of a large temporaray file and is searched LowerCase */
    public static final String SEARCH_LC = "The Text tO seaRch is heRe";

    /** This text is NOT part of a large temporaray file  and is searched LowerCase */
    public static final String NO_SEARCH_LC = "The text to SearcH is NoT hEre !";

    /** A large, temporary file containing some testData */
    private static File tmpFile;

    /**
     * Constructor to conduct a special test.
     *
     * @param name name of the test to execute.
     */
    public TestStreamFinder (String name) {
        super (name);
    }

    /** Prepare for the test by creating a temporary file.
     */
    @Override
	protected void setUp () throws Exception {
    
        if (tmpFile != null && tmpFile.exists())  // avoid unneeded, duplicate setup
            return;
        
        tmpFile = BasicTestCase.createTestFile("TestStreamFinder", ".dat");
        tmpFile.deleteOnExit(); // Just to be sure ....
        
        FileOutputStream out = new FileOutputStream(tmpFile);
		try {
			PrintWriter pw = new PrintWriter(out);
			for (int i = 0; i < 1111; i++) {
				pw.println("This is a dummy Text to fill this file with Text to be searched");
				pw.println("0123456789 the quick brown fox jumps over the lazy dogs back.");
				pw.println("ÄÖÜäöüßêÁÒ THE QUICK BROWN FOX JUMPS OVER THE LAZY DOGS BACK:");
			}
			pw.println("[{IgnoreMe}]=^°" + SEARCH + "\\//?~*+-.,;:_");
			for (int i = 0; i < 100; i++) {
				pw.println("This is a dummy Text to fill this file with Text to be searched");
				pw.println("0123456789 the quick brown fox jumps over the lazy dogs back.");
				pw.println("ÄÖÜäöüßêÁÒ THE QUICK BROWN FOX JUMPS OVER THE LAZY DOGS BACK:");
			}
		} finally {
			out.close();
		}
    }

    // Test methodes

    /** Test using Reader variant of functions.
     */
    public void testReader () throws Exception
    {
        String target = "0123456789 aaaabb 0123456789";
    
        assertFalse(StreamFinder.isInReader(new StringReader(target), (String) null ));
        assertTrue (StreamFinder.isInReader(new StringReader(target), ""    ));
        assertTrue (StreamFinder.isInReader(new StringReader(target), "9"   ));
        assertTrue (StreamFinder.isInReader(new StringReader(target), "aaaa" ));
        assertFalse(StreamFinder.isInReader(new StringReader(target), "aaaaa"));
        assertTrue (StreamFinder.isInReader(new StringReader(target), "aaaabb" ));
        assertFalse(StreamFinder.isInReader(new StringReader(target), "aaaaabb"));
      
        assertTrue (StreamFinder.isInReader(new StringReader(target), new String[]  {"aaaabb" }));
        assertFalse(StreamFinder.isInReader(new StringReader(target), new String[]  {"aaaaabb"}));
   
        assertTrue (StreamFinder.isInReader(new StringReader(target), 
                        new String[]  { "xyz" , "aaaabb" , "uvw" } ));
        assertFalse(StreamFinder.isInReader(new StringReader(target), 
                        new String[]  { "xyz" , "aaaaabb" , "uvw" } ));
        assertFalse(StreamFinder.isInReader(new StringReader(target), 
                        new String[]  { "xyz" , null , "uvw" } ));
        assertTrue (StreamFinder.isInReader(new StringReader(target), 
                        new String[]  { "xyz" , "aaaaabb" , "" } )); // Empty String is always contained.
   }

    /** Test using LowerCase Reader variant of functions.
     */
    public void testReaderLowerCase () throws Exception
    {
        String target = "0123456789 AaAabB 0123456789";
    
        assertFalse(StreamFinder.isInReaderLowerCase(new StringReader(target), (String) null ));
        assertTrue (StreamFinder.isInReaderLowerCase(new StringReader(target), ""    ));
        assertTrue (StreamFinder.isInReaderLowerCase(new StringReader(target), "9"   ));
        assertTrue (StreamFinder.isInReaderLowerCase(new StringReader(target), "aAAa" ));
        assertFalse(StreamFinder.isInReaderLowerCase(new StringReader(target), "AaaAa"));
        assertTrue (StreamFinder.isInReaderLowerCase(new StringReader(target), "aaAaBb" ));
        assertFalse(StreamFinder.isInReaderLowerCase(new StringReader(target), "aaaAabB"));
        
        assertTrue (StreamFinder.isInReaderLowerCase(new StringReader(target), 
                                            new String[]  { "aaAaBb" }));
        assertFalse(StreamFinder.isInReaderLowerCase(new StringReader(target),  
                                            new String[]  { "aaaAabB" }));

        assertTrue (StreamFinder.isInReaderLowerCase(new StringReader(target), 
                                            new String[]  { "XyZ" , "aaAaBb"  , "uVw" } ));
        assertFalse(StreamFinder.isInReaderLowerCase(new StringReader(target), 
                                            new String[]  { "xYz" , "aaaAabB" , "UvW" } ));
        assertFalse(StreamFinder.isInReaderLowerCase(new StringReader(target), 
                                            new String[]  { "XyZ" , null   , "uVw" } ));
        assertTrue (StreamFinder.isInReaderLowerCase(new StringReader(target), 
                                            new String[]  { "xYz" , "aaaAabB" , "" } )); 
            // empty String is always contained
    }

    /** Test using Stream variant of functions.
     */
    public void testStream () throws Exception 
    {
        byte[] target = "0123456789 aaaabb 0123456789".getBytes();
    
        assertTrue(!StreamFinder.isInStream(new ByteArrayInputStream(target), (String) null ));
        assertTrue( StreamFinder.isInStream(new ByteArrayInputStream(target), ""    ));
        assertTrue( StreamFinder.isInStream(new ByteArrayInputStream(target), "9"   ));
        assertTrue( StreamFinder.isInStream(new ByteArrayInputStream(target), "aaaa" ));
        assertTrue(!StreamFinder.isInStream(new ByteArrayInputStream(target), "aaaaa"));
        assertTrue( StreamFinder.isInStream(new ByteArrayInputStream(target), "aaaabb" ));
        assertTrue(!StreamFinder.isInStream(new ByteArrayInputStream(target), "aaaaabb"));
    }

    /** Test using LowerCase Stream variant of functions.
     */
    public void testStreamLowerCase() throws Exception 
    {
        byte[] target = "0123456789 AaAabB 0123456789".getBytes();
    
        assertTrue(!StreamFinder.isInStreamLowerCase(new ByteArrayInputStream(target), (String) null  ));
        assertTrue( StreamFinder.isInStreamLowerCase(new ByteArrayInputStream(target), ""    ));
        assertTrue( StreamFinder.isInStreamLowerCase(new ByteArrayInputStream(target), "9"   ));
        assertTrue( StreamFinder.isInStreamLowerCase(new ByteArrayInputStream(target), "aAAa" ));
        assertTrue(!StreamFinder.isInStreamLowerCase(new ByteArrayInputStream(target), "AaaAa"));
        assertTrue( StreamFinder.isInStreamLowerCase(new ByteArrayInputStream(target), "aaAaBb" ));
        assertTrue(!StreamFinder.isInStreamLowerCase(new ByteArrayInputStream(target), "aaaAabB"));
    }

    /** Test using the large File via a Reader.
     */
    public void testFileReader () throws Exception
    {
		FileReader reader1 = new FileReader(tmpFile);
		try {
			assertTrue(StreamFinder.isInReader(reader1, SEARCH));
		} finally {
			reader1.close();
		}
		FileReader reader2 = new FileReader(tmpFile);
		try {
			assertTrue(!StreamFinder.isInReader(reader2, NO_SEARCH));
		} finally {
			reader2.close();
		}
    }

    /** Test using the large File via a Stream.
     */
    public void testFileStream () throws Exception
    {
        FileInputStream stream1 = new FileInputStream(tmpFile);
        try {
        	assertTrue( StreamFinder.isInStream(stream1, SEARCH    ));
		} finally {
			stream1.close();
		}
        FileInputStream stream2 = new FileInputStream(tmpFile);
        try {
        	assertTrue(!StreamFinder.isInStream(stream2, NO_SEARCH ));
		} finally {
			stream2.close();
		}
    }

    /** Test using the large LowerCase File via a Reader.
     */
    public void testFileReaderLowerCase () throws Exception
    {
		FileReader reader1 = new FileReader(tmpFile);
		try {
			assertTrue(StreamFinder.isInReaderLowerCase(reader1, SEARCH_LC));
		} finally {
			reader1.close();
		}
		FileReader reader2 = new FileReader(tmpFile);
		try {
			assertTrue(!StreamFinder.isInReaderLowerCase(reader2, NO_SEARCH_LC));
		} finally {
			reader2.close();
		}
    }

    /** Test using the large LowerCase File via a Stream.
     */
    public void testFileStreamLowerCase () throws Exception
    {
        FileInputStream stream1 = new FileInputStream(tmpFile);
		try {
			assertTrue(StreamFinder.isInStreamLowerCase(stream1, SEARCH_LC));
		} finally {
			stream1.close();
		}
		FileInputStream stream2 = new FileInputStream(tmpFile);
		try {
			assertTrue(!StreamFinder.isInStreamLowerCase(stream2, NO_SEARCH_LC));
		} finally {
			stream2.close();
		}
    }

    /** Althoug it is static it is <em>possible</em> to call the CTor.
     */
    public void testCtor () throws Exception
    {
        assertNotNull(new StreamFinder() {
            @Override
			public String toString() {
                return "StreamFinder";
            }
        }.toString());
    }

    /** Pseudo Test to delete the tmpFile (deleteOnExit() does not work ?)
     */
    public void doDeleteFile () throws Exception
    {
        tmpFile.delete();
        tmpFile = null;
    }

    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestStreamFinder.class);
        // TestSuite suite = new TestSuite()
        suite.addTest(new TestStreamFinder("doDeleteFile"));
        return suite;
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
