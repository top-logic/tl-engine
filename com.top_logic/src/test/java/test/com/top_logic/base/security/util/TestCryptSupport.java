/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.security.util;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.security.util.CryptSupport;
import com.top_logic.basic.Logger;

/**
 * Test of the {@link com.top_logic.base.security.util.CryptSupport}.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestCryptSupport extends TestCase {

    /** The message to be used for encryption. */
    private static final String TEST_MESSAGE = "Orwell war ein Optimist!";

    /**
     * Constructor needed for framework.
     *
     * @param    aName    The name of the test.
     */
    public TestCryptSupport (String aName) {
        super (aName);
    }

    /**
     * Check, if a message is de- and encoded correct.
     */
    public void testOK () {
        String       theOrig;
        CryptSupport theSupport = CryptSupport.getInstance ();
        String       theMessage = theSupport.encodeString (TEST_MESSAGE);

        Logger.info ("encoded message: " + theMessage, this);

        assertNotNull (theMessage);
        assertTrue (!TEST_MESSAGE.equals (theMessage));

        theOrig = theSupport.decodeString (theMessage);

        Logger.info ("decoded message: " + theOrig, this);

        assertNotNull (theOrig);
        assertEquals (TEST_MESSAGE, theOrig);
    }

    /**
     * Check, if an empty message is de- and encoded correct.
     */
    public void testEmpty () {
        String       theOrig;
        CryptSupport theSupport = CryptSupport.getInstance ();
        String       theMessage = theSupport.encodeString ("");

        Logger.info ("encoded message: " + theMessage, this);

        assertNotNull (theMessage);
        assertEquals (theMessage, "");

        theOrig = theSupport.decodeString (theMessage);

        Logger.info ("decoded message: " + theOrig, this);

        assertNotNull (theMessage);
        assertEquals (theMessage, "");
    }

    /**
     * Check, if a null message is de- and encoded correct.
     */
    public void testNull () {
        String       theOrig;
        CryptSupport theSupport = CryptSupport.getInstance ();
        String       theMessage = theSupport.encodeString (null);

        Logger.info ("encoded message: " + theMessage, this);

        assertNull (theMessage);

        theOrig = theSupport.decodeString (theMessage);

        Logger.info ("decoded message: " + theOrig, this);

        assertNull (theOrig);
    }

	/**
	 * Tests encryption with a message spanning several blocks.
	 */
	public void testLongEncryption() {
		assertRoundTrip("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
	}

	/**
	 * Tests encryption of messages with a length around the block size (40 bytes for the key
	 * pair used).
	 */
	public void testBlockBoundary() {
		StringBuilder builder = new StringBuilder();
		for (int n = 0; n < 130; n++) {
			builder.append((char) ('0' + n % 10));
			assertRoundTrip(builder.toString());
		}
	}

	/**
	 * Tests encryption of messages whose first byte in UTF-8 has the highest bit set.
	 */
	public void testNonAsciiStart() {
		assertRoundTrip("\u00C4");
		assertRoundTrip("\u00C4rger");
		assertRoundTrip("\u00C4rger mit \u00DCmlauten und \u00DFonderzeichen, der deutlich l\u00E4nger als ein Block ist.");
		assertRoundTrip("\u20AC\u20AC\u20AC");
		assertRoundTrip("\uD83D\uDE00 emoji");
	}

	/**
	 * Tests encryption with all available characters.
	 */
	public void testAllCharactersEncryption() {
		StringBuilder builder = new StringBuilder();
		// Starts with character 1: The message is encrypted as a number, which cannot represent
		// leading zero bytes, therefore a leading U+0000 character is lost.
		for (int ch = Character.MIN_VALUE + 1; ch <= Character.MAX_VALUE; ch++) {
			if (Character.isSurrogate((char) ch)) {
				// A lone surrogate is no valid text and has no UTF-8 representation. Surrogate
				// pairs are tested in testNonAsciiStart().
				continue;
			}
			builder.append((char) ch);
		}
		assertRoundTrip(builder.toString());
	}

	/**
	 * Tests that single-block cipher texts are stable.
	 *
	 * <p>
	 * The expected cipher texts are computed with the key pair stored in the application
	 * ({@link com.top_logic.base.security.util.KeyStore}). Values encrypted and persisted with
	 * this key must stay decryptable and must encrypt to the identical cipher text.
	 * </p>
	 */
	public void testCompatibility() {
		assertCipherText("Orwell war ein Optimist!",
			"00b38569091a5616202f649a5f7b4e32c1534e78939f24cf4b6007dca8810aceae13bdbac0d1e3a1717d01a43e9cf790d8960db4ed535d3091fc998c07eff073648cd8f94c30cf80fd9c9643e68b2c9da9748d2a1a54");
		assertCipherText("a",
			"374c1b6a0c09b8a1046dcef2ed5e7b8c0b85278d93f0f91a945a34262c84b5c3c9c6d8773a2abcca25241c1b2f307a525a5628714de88ec4a0a32fa464e3a99d3d65d494d118bbf1443a410cb70b7501cb7765f3d3");
		assertCipherText("root1234",
			"4214d3efb2529fc28fde4ba6c6cbe80cb8bf9c1c7c234b4dab2ab1bd2bbdbbfa09c7ea5909c96d1491b75e353d3dc8e86190b25cd635d74cae75760281d9deaa03b75542bb0d6bbae3dbb88bc362e1f47b485b436c");
		assertCipherText("0123456789012345678901234567890123456789",
			"7cea7547fe344601a15d9676012bca38c5f0795a47ae28efd18ed1d8010cee9c2241137dddb5add349b947a325d6fdc2366a078c8fff1a2078a7b092352b2c1e6d0b51b719cfd2d76051174c603a6fd86d01247e59");
	}

	private static void assertCipherText(String message, String cipherText) {
		CryptSupport cryptSupport = CryptSupport.getInstance();
		assertEquals(cipherText, cryptSupport.encodeString(message));
		assertEquals(message, cryptSupport.decodeString(cipherText));
	}

	private static void assertRoundTrip(String message) {
		CryptSupport cryptSupport = CryptSupport.getInstance();
		String encoded = cryptSupport.encodeString(message);
		assertFalse(message.equals(encoded));
		assertEquals(message, cryptSupport.decodeString(encoded));
	}

    /**
     * Used for framework.
     *
     * @return    The test suite for this class.
     */
    public static Test suite () {
        return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestCryptSupport.class, CryptSupport.Module.INSTANCE));
    }

    /**
     * Main class to start test without UI.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
}

