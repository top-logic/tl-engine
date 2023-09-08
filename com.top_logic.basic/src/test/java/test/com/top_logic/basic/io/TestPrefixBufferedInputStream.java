/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.PrefixBufferedInputStream;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Test the {@link PrefixBufferedInputStream}
 * 
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">kha</a>
 */
public class TestPrefixBufferedInputStream extends TestCase {

    /** Path to a File used for Testing */
	static final String TEST_FILE =
		ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/io/TestPrefixBufferedInputStream.java";

    /** 
     * Creates a {@link TestPrefixBufferedInputStream}.
     */
    public TestPrefixBufferedInputStream(String name) {
        super(name);
    }

    /**
     * Test simple default usage.
     */
    public void testSimpleDefault() throws IOException {
        File         testFile = new File(TEST_FILE);
        StringBuffer theBuf   = new StringBuffer ((int) testFile.length());
        
        PrefixBufferedInputStream pbis = new PrefixBufferedInputStream(
                new FileInputStream(testFile));
        
        int c;
        while ((c = pbis.read()) >= 0) {
            theBuf.append((char) c);
        }
        theBuf.setLength(pbis.getSize());
        String prefix = new String(pbis.getBuffer(), 0, pbis.getSize(), "ISO-8859-1");
        assertEquals(prefix, theBuf.toString());

    }

    /**
     * Test usage without real buffering
     */
    public void testSimpleNoPrefix() throws IOException {
        File         testFile = new File(TEST_FILE);
        StringBuffer theBuf   = new StringBuffer ((int) testFile.length());
        
        PrefixBufferedInputStream pbis = new PrefixBufferedInputStream(
                new FileInputStream(testFile), 0);
        
        int c;
        while ((c = pbis.read()) >= 0) {
            theBuf.append((char) c);
        }
        theBuf.setLength(pbis.getSize());
        String prefix = new String(pbis.getBuffer(), 0, pbis.getSize(), "ISO-8859-1");
        assertEquals(prefix, theBuf.toString());

    }

    /**
     * Test usage with one byte of buffer.
     */
    public void testSimple1Prefix() throws IOException {
        File         testFile = new File(TEST_FILE);
        StringBuffer theBuf   = new StringBuffer ((int) testFile.length());
        
        PrefixBufferedInputStream pbis = new PrefixBufferedInputStream(
                new FileInputStream(testFile), 0);
        
        int c;
        while ((c = pbis.read()) >= 0) {
            theBuf.append((char) c);
        }
        theBuf.setLength(pbis.getSize());
        String prefix = new String(pbis.getBuffer(), 0, pbis.getSize(), "ISO-8859-1");
        assertEquals(prefix, theBuf.toString());

    }
    
    /**
     * Test simple usage with arrays.
     */
    public void testSimpleArray() throws IOException {
        File         testFile = new File(TEST_FILE);
        int          maxTmp   = 1024;
        byte         tmpBuf[] = new byte[maxTmp];
        StringBuffer theBuf   = new StringBuffer (
                PrefixBufferedInputStream.DEFAULT_PREFIX + maxTmp);
        
        PrefixBufferedInputStream pbis = new PrefixBufferedInputStream(
                new FileInputStream(testFile));
        
        int size;
        while ((size = pbis.read(tmpBuf)) > 0 
            && theBuf.length() < PrefixBufferedInputStream.DEFAULT_PREFIX) {
            theBuf.append(new String(tmpBuf,0, size, "ISO-8859-1"));
        }
        theBuf.setLength(pbis.getSize());
        String prefix = new String(pbis.getBuffer(), 0, pbis.getSize(), "ISO-8859-1");
        assertEquals(prefix, theBuf.toString());

    }
    
    /**
     * Test complex usage with arrays and simple reads
     */
    public void testComplexArray() throws IOException {
        File         testFile = new File(TEST_FILE);
        int          maxTmp   = 1024;
        int          testLen  = PrefixBufferedInputStream.DEFAULT_PREFIX + maxTmp;
        byte         tmpBuf[] = new byte[maxTmp];
        Random       rand     = new Random(1704);
        

        StringBuffer theBuf   = new StringBuffer (testLen + maxTmp);
        
        PrefixBufferedInputStream pbis = new PrefixBufferedInputStream(
                new FileInputStream(testFile));

        while (theBuf.length() < testLen) {
            if (rand.nextBoolean()) {
                int c;
                if ((c = pbis.read()) >= 0) {
                    theBuf.append((char) c);
                }
            } else {
                int start = rand.nextInt(maxTmp);
                int len   = rand.nextInt(maxTmp - start);
                int size;
                if ((size = pbis.read(tmpBuf, start, len)) > 0) {
                    theBuf.append(new String(tmpBuf,start, size, "ISO-8859-1"));
                }
            }
        }
        
        theBuf.setLength(pbis.getSize());
        String prefix = new String(pbis.getBuffer(), 0, pbis.getSize(), "ISO-8859-1");
        assertEquals(prefix, theBuf.toString());
    }

    /**
     * Test default usage with small input.
     */
    public void testDefaultSmall() throws IOException {
        int          maxTmp     = 1024;
        byte         tmpBuf[]   = new byte[maxTmp];
        Random       rand       = new Random(1706);
        String       testString = StringServices.getRandomString(rand, 
                PrefixBufferedInputStream.DEFAULT_PREFIX >> 1);

        
        PrefixBufferedInputStream pbis = new PrefixBufferedInputStream(
                new ByteArrayInputStream(testString.getBytes()));

        while (true) {
            if (rand.nextBoolean()) {
                if (pbis.read() < 0) {
                    break;
                }
            } else {
                int start = rand.nextInt(maxTmp);
                int len   = rand.nextInt(maxTmp - start);
                if (pbis.read(tmpBuf, start, len) < 0) {
                    break;
                }
            }
        }
        
        String prefix = new String(pbis.getBuffer(), 0, pbis.getSize(), "ISO-8859-1");
        assertEquals(prefix, testString);
    }

    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        return new TestSuite (TestPrefixBufferedInputStream.class);
        // return new TestPrefixBufferedInputStream("testComplexArray");
        
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        TestRunner.run (suite ());
    }
}

