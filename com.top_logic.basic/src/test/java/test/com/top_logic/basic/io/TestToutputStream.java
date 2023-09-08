/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.io.TOutputStream;

/**
 * Testcase for TOutputStream.
 * 
 * The testcase opens an TOutputStream, writes some bytes into and 
 * looks if both files contain the same data as written on the 
 * TOutputStream. 
 *  
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class TestToutputStream extends BasicTestCase {
    
    /** Array for testing */
    public static final byte BYTES[] = {    (byte) 0x00, 
                                            (byte) 0x23,
                                            (byte) 0x42,
                                            (byte) 0xAA,
                                            (byte) 0xFF};

    /** filename of first output-file */
    public static final String FILE_NAME_1 = "tmpTOutputStreamTest1.dat";
    
    /** filename of second output-file */
    public static final String FILE_NAME_2 = "tmpTOutputStreamTest2.dat";

    /**
     * Constructor for TOutputStreamTest.
     */
    public TestToutputStream(String arg0) {
        super(arg0);
    }
    
    /** 
     * Cleanup after an (eventually failed) test.
     */
    @Override
	protected void tearDown() throws Exception {
        
        File f;
        
        f = BasicTestCase.createNamedTestFile(FILE_NAME_1);
        if (f.exists() && !f.delete())
            System.err.println("Warning: File " + FILE_NAME_1 + " could not be deleted.");
        
        f = BasicTestCase.createNamedTestFile(FILE_NAME_2);
        if (f.exists() && !f.delete())
            System.err.println("Warning: File " + FILE_NAME_2 + " could not be deleted.");
        
        super.tearDown();
    }

    /**
     * Main test-case.
     */
    public void testMain() throws IOException {
        
        OutputStream out1  = null;
        OutputStream out2  = null;
        TOutputStream tout = null;
        
        /* opening two FileOutputStreams */     
        out1 = new FileOutputStream(BasicTestCase.createNamedTestFile(FILE_NAME_1)); 
        out2 = new FileOutputStream(BasicTestCase.createNamedTestFile(FILE_NAME_2));
        
        /* opening the TOutputStream */
        tout = new TOutputStream(out1, out2);
        
        /* write some bytes and hope it does not crash */
        tout.write(0x99);           // write one Byte
        tout.write(BYTES);          // write a byte-array
        tout.flush();
        tout.write(BYTES, 1, 2);
        
        /* it should close both OutputStreams too */
        tout.close();           

        /* tout should be closed now */             
        try {
            out1.write(0x23);
            fail("Should be closed");           
        }
        catch (IOException expected) { /* expected */ }

        try {
            out2.write(1);
            fail("Should be closed");
        }
        catch (IOException expected) { /* expected */ }
        
        /* look now if both files contain the date was written into */
        checkFile(FILE_NAME_1);
        checkFile(FILE_NAME_2);
    }
    
    /** Check the given file to contain the bytes written in testMain() */
    protected void checkFile(String aFileName) throws IOException {

        InputStream in;
        
        in = new FileInputStream(BasicTestCase.createNamedTestFile(aFileName));
        assertEquals(0x99, in.read());
        assertEquals(0x00, in.read());
        assertEquals(0x23, in.read());
        assertEquals(0x42, in.read());
        assertEquals(0xAA, in.read());
        assertEquals(0xFF, in.read());
        assertEquals(0x23, in.read());
        assertEquals(0x42, in.read());
        assertEquals(-1  , in.read());
        in.close();                     
    }
        
    /**
     * the suite of test to execute for this test.
     */
    public static Test suite() {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestToutputStream.class)); 
    }

    /**
     * Main function for direct testing.
     */
    public static void main(String[] ignored) {
        TestRunner.run(suite());
    }   
}
