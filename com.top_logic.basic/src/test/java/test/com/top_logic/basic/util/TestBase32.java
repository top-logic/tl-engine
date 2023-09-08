/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;

import com.top_logic.basic.util.Base32;

/**
 * Test case for {@link Base32}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBase32 extends TestCase {
	
	private Random random;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		random = new Random(42);
	}
	
	@Override
	protected void tearDown() throws Exception {
		random = null;
		super.tearDown();
	}
	
	public void testEncodeDecode0() {
		doTest(value(0));
	}
	
	public void testEncodeDecode1() {
		doTest(value(1));
	}
	
	public void testEncodeDecodeN() {
		for (int size = 2; size < 10 * 5 * 8; size++) {
			doTest(value(size));
		}
	}

	public void testPaddingCheck1() {
		byte[] orig = new byte[1];
		char[] encoded = Base32.encodeBase32(orig);
		encoded[1] = 'B';
		try {
			Base32.decodeBase32(encoded);
			fail("Padding check failed.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	public void testPaddingCheck2() {
		byte[] orig = new byte[10];
		Arrays.fill(orig, (byte)0xFF);
		char[] encoded = Base32.encodeBase32(orig);
		int shortenSize = 2;
		char[] shortenedEncoded = new char[encoded.length - shortenSize];
		System.arraycopy(encoded, 0, shortenedEncoded, 0, encoded.length - shortenSize);
		try {
			Base32.decodeBase32(shortenedEncoded);
			fail("Padding check failed.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}
	
	private void doTest(byte[] orig) {
		char[] encoded = Base32.encodeBase32(orig);
		doCheck(orig, encoded);
		doCheck(orig, new String(encoded).toLowerCase().toCharArray());
	}

	private void doCheck(byte[] orig, char[] encoded) {
		byte[] recovered = Base32.decodeBase32(encoded);
		assertTrue(Arrays.equals(orig, recovered));
	}

	private byte[] value(int size) {
		byte[] result = new byte[size];
		random.nextBytes(result);
		return result;
	}

}
