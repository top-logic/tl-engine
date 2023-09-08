/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.io.TestingDataStream;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;

/**
 * Test the {@link BinaryDataFactory}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
@SuppressWarnings("javadoc")
public class TestBinaryDataFactory extends TestCase {

    public TestBinaryDataFactory(String name) {
        super(name);
    }

	/**
	 * Test creation of empty {@link BinaryData} objects through the
	 * {@link BinaryDataFactory}.
	 */
    public void testEmpty() throws IOException {
    	BinaryData bd1 = BinaryDataFactory.createBinaryData(new TestingDataStream(0), 0);
    	assertEquals(0, bd1.getSize());
		try (InputStream in1 = new TestingDataStream(0)) {
			try (InputStream in2 = bd1.getStream()) {
				assertTrue(FileUtilities.equalsStreamContents(in1, in2));
			}
		}
    }
    
    public void testHashCode() throws IOException {
    	assertEquals(0, new TestingBinaryData(42, 0, true).hashCode());
    	assertEquals(8 | 1 | 16 | (2 | 32) << 8 | (4 | 64) << 16 | (8 | 128) <<24, BinaryDataFactory.createBinaryData(new ByteArrayInputStream(new byte[] {1, 2, 4, 8, 16, 32, 64, (byte) 128}), 8).hashCode());
    }
    
    public void testChunkedLarge() throws IOException {
    	int size = 2 * BinaryDataFactory.MAX_MEMORY_SIZE;
		BinaryData binaryData = BinaryDataFactory.createBinaryData(new TestingDataStream(size, 101, 42), size);
    	assertEquals(size, binaryData.getSize());
		try (InputStream in1 = new TestingDataStream(size, size, 42)) {
			try (InputStream in2 = binaryData.getStream()) {
				assertTrue(StreamUtilities.equalsStreamContents(in1, in2));
			}
		}
    }
    
    public void testChunkedSmall() throws IOException {
    	int size = BinaryDataFactory.MAX_MEMORY_SIZE / 2;
    	BinaryData binaryData = BinaryDataFactory.createBinaryData(new TestingDataStream(size, 101, 42), size);
    	assertEquals(size, binaryData.getSize());
		try (InputStream in1 = new TestingDataStream(size, size, 42)) {
			try (InputStream in2 = binaryData.getStream()) {
				assertTrue(StreamUtilities.equalsStreamContents(in1, in2));
			}
		}
    }
    
    public void testNoMoreDataLarge() {
    	int size = 2 * BinaryDataFactory.MAX_MEMORY_SIZE;
    	try {
			BinaryDataFactory.createBinaryData(new TestingDataStream(size - 1, 101, 42), size);
			fail("End of stream exception expected.");
		} catch (IOException ex) {
			// Expected.
		}
    }
    
    public void testNoMoreDataSmall() {
    	int size = BinaryDataFactory.MAX_MEMORY_SIZE / 2;
    	try {
			BinaryDataFactory.createBinaryData(new TestingDataStream(size - 1, 101, 42), size);
			fail("End of stream exception expected.");
		} catch (IOException ex) {
			// Expected.
		}
    }
    
    /**
     * Test the main aspects of the {@link BinaryDataFactory}.
     */
    public void testMain() throws  IOException {
		Random rand = new Random(42);
        int max2 =  BinaryDataFactory.MAX_MEMORY_SIZE >> 1;
        
        BinaryData bds[] = new BinaryData[10];
        HashSet<BinaryData> bdSet = new HashSet<>(1);
        for (int i=0; i < bds.length; i++) {
            byte[] data = new byte[rand.nextInt(max2)];
            rand.nextBytes(data);
            bdSet.add(bds[i] = BinaryDataFactory.createBinaryData(new ByteArrayInputStream(data), data.length));
        }
        
        for (BinaryData bd: bds) {
            assertTrue(bdSet.contains(bd));
        }
        
    }

	/**
	 * Tests {@link BinaryDataFactory#createFileBasedBinaryData(InputStream, String, String)}.
	 */
	public void testFileBasedBinaryData() throws IOException {
		byte[] data = new byte[56];
		new Random(47).nextBytes(data);

		BinaryData bd = BinaryDataFactory.createFileBasedBinaryData(new ByteArrayInputStream(data),
			"application/myFunnyType", "bdName.dat");
		byte[] copiedData = new byte[data.length];
		try (InputStream in = bd.getStream()) {
			StreamUtilities.readFully(in, copiedData);
			assertEquals(-1, in.read());
		}
		BasicTestCase.assertEquals(data, copiedData);
		assertEquals("application/myFunnyType", bd.getContentType());
		assertEquals("bdName.dat", bd.getName());
		assertEquals(56, bd.getSize());
	}

	public void testBinaryDataFromStream() throws IOException {
		byte[] data = new byte[BinaryDataFactory.MAX_MEMORY_SIZE + 1];
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		BinaryData binaryData = BinaryDataFactory.createBinaryData(in, data.length, "text/plain", "myFile.txt");

		assertEquals(data.length, binaryData.getSize());
		assertEquals("text/plain", binaryData.getContentType());
		assertEquals("myFile.txt", binaryData.getName());
		BasicTestCase.assertEquals(data, StreamUtilities.readStreamContents(binaryData.getStream()));
	}
        
    /**
     * Test Trivial parts of {@link BinaryDataFactory}.
     */
	public void testTrivial() {
		BinaryDataFactory factory = new BinaryDataFactory();
		assertNotNull(factory);
    }
    
    public static Test suite() {
		return ModuleTestSetup
			.setupModule(ServiceTestSetup.createSetup(TestBinaryDataFactory.class, Settings.Module.INSTANCE));
    }
    
    public static void main(String[] args) {
        Logger.configureStdout(); // "INFO"
        TestRunner.run(suite());
    }

}

