/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.io.TestingDataStream;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.MemoryBinaryData;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Test the {@link MemoryBinaryData}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
@SuppressWarnings("javadoc")
public class TestMemoryBinaryData extends BasicTestCase {

    public TestMemoryBinaryData(String name) {
        super(name);
    }
    
	public void testRange() throws IOException {
		BinaryData data = BinaryDataFactory.createBinaryData(new byte[] { 0, 1, 2, 3 }, 1, 2);
		try (InputStream in = data.getStream()) {
			assertEquals(1, in.read());
			assertEquals(2, in.read());
			assertTrue(in.read() < 0);
		}
	}

	public void testOutOfRange() {
		try {
			BinaryDataFactory.createBinaryData(new byte[] { 0, 1, 2, 3 }, 1, 4);
		} catch (IllegalArgumentException ex) {
			assertContains("Invalid range", ex.getMessage());
		}
		try {
			BinaryDataFactory.createBinaryData(new byte[] { 0, 1, 2, 3 }, 1, -1);
		} catch (IllegalArgumentException ex) {
			assertContains("Invalid range", ex.getMessage());
		}
	}

    public void testNullSize() throws IOException {
		BinaryData mbd1 = BinaryDataFactory.createBinaryData(new byte[0]);
		BinaryData mbd2 = BinaryDataFactory.createMemoryBinaryData(new TestingDataStream(0), 0);
    	assertEquals(0, mbd1.getSize());
		try (InputStream in1 = new TestingDataStream(0)) {
			try (InputStream in2 = mbd1.getStream()) {
				assertTrue(FileUtilities.equalsStreamContents(in1, in2));
			}
		}
    	assertEquals(mbd1, mbd2);
    }
    
    public void testChunkedConstruction() throws IOException {
		BinaryData mbd1 = BinaryDataFactory.createMemoryBinaryData(
			new TestingDataStream(BinaryDataFactory.MAX_MEMORY_SIZE), BinaryDataFactory.MAX_MEMORY_SIZE);
		try (TestingDataStream testStream = new TestingDataStream(BinaryDataFactory.MAX_MEMORY_SIZE)) {
			try (InputStream in2 = mbd1.getStream()) {
				assertTrue(FileUtilities.equalsStreamContents(testStream, in2));
			}
		}
    }
    
    /**
	 * Test the main aspects of the {@link BinaryData}.
	 */
    public void testMain() throws  IOException {
        Random rand = new Random(42);
        
        byte[] data = new byte[BinaryDataFactory.MAX_MEMORY_SIZE];
        rand.nextBytes(data);
        
		BinaryData mbd1 = BinaryDataFactory.createBinaryData(data);
        data = new byte[BinaryDataFactory.MAX_MEMORY_SIZE];
        rand.nextBytes(data);
		BinaryData mbd2 = BinaryDataFactory.createBinaryData(data);
		File thisFile = testFixture();
		BinaryData mbd3;
		try (FileInputStream content = new FileInputStream(thisFile)) {
			mbd3 = BinaryDataFactory.createMemoryBinaryData(content, thisFile.length());
		}
        
		BinaryData otherBD = BinaryDataFactory.createBinaryData(thisFile);

        long thisLen = thisFile.length();
        assertEquals(thisLen     , mbd3.getSize());
        assertEquals(mbd3, otherBD);
        assertEquals(mbd3, mbd3);
        assertFalse(mbd3.equals(mbd1));
        assertFalse(mbd2.equals(mbd3));
        
		String cachedContent;
		try (InputStream cachedStream = mbd3.getStream()) {
			cachedContent = StreamUtilities.readAllFromStream(cachedStream);
		}
		assertTrue(cachedContent.indexOf("mbd3.toString().indexOf") > 0);

		try (InputStream in1 = mbd2.getStream()) {
			try (InputStream in2 = new ByteArrayInputStream(data)) {
				assertTrue(StreamUtilities.equalsStreamContents(in1, in2));
			}
		}
    }
    
    /**
     * Provoke exceptions when using FileBasedBinaryData.
     */
    public void testException() throws  IOException {
		File thisFile = testFixture();
        assert thisFile.length() < BinaryDataFactory.MAX_MEMORY_SIZE;

        try {
            BinaryDataFactory.createMemoryBinaryData(new FileInputStream(thisFile), thisFile.length() + 1);
            fail("IOException expected");
        } catch (IOException expected) { /* expected */ }

        try {
            BinaryDataFactory.createMemoryBinaryData(new FileInputStream(thisFile), thisFile.length() >> 1);
            fail("IOException expected");
        } catch (IOException expected) { /* expected */ }
        
		BinaryData mbd = BinaryDataFactory.createMemoryBinaryData(new FileInputStream(thisFile), thisFile.length());
        File tmpFile = createNamedTestFile("TestMemoryBinaryData");
        FileUtilities.copyFile(thisFile, tmpFile);
		BinaryData tmpData = BinaryDataFactory.createBinaryData(tmpFile);
        assertTrue(tmpFile.delete()); // Break underlying file 
        
        assertFalse(tmpData.equals(mbd));
        assertFalse(mbd.equals(tmpData)); // The Exceptions logged are correct
    }

	private File testFixture() {
		return new File(ModuleLayoutConstants.SRC_TEST_DIR + "/"
			+ TestMemoryBinaryData.class.getPackageName().replace('.', '/') + "/TestMemoryBinaryData.data");
	}
    
    public static Test suite() {
		return ModuleTestSetup.setupModule(TestMemoryBinaryData.class);
    }
    
    public static void main(String[] args) {
        Logger.configureStdout(); // "INFO"
        TestRunner.run(suite());
    }


}

