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
import com.top_logic.util.Utils;

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
	 * Tests encryption with long message.
	 */
	public void testLongEncryption() {
		CryptSupport cryptSupport = CryptSupport.getInstance();
		String message = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		String encoded = cryptSupport.encodeString(message);

		if (!message.equals(cryptSupport.decodeString(encoded))) {
			/* Exptected due to the known bug in ticket #11351: Encryption with long message
			 * fails. */
		} else {
			fail("Test should fail due to the known bug in ticket #11351: Encryption with long message fails.");
		}
	}

	/**
	 * Tests encryption with all available characters.
	 */
	public void testAllCharactersEncryption() {
		CryptSupport cryptSupport = CryptSupport.getInstance();
		StringBuilder builder = new StringBuilder();
		int max = Character.MAX_VALUE;
		int min = Character.MIN_VALUE;
		for (int i = min; i < max; i++) {
			builder.append((char) i);
		}
		String message = builder.toString();
		String encoded = cryptSupport.encodeString(message);
		String expected = message;
		String actual = cryptSupport.decodeString(encoded);
		if (!Utils.equals(expected, actual)) {
			/* Exptected due to the known bug in ticket #11351: Encryption with long message fails:
			 * This test has a long message, because all characters are tested. */
		} else {
			fail(
				"Test should fail due to the known bug in ticket #11351: Encryption with long message fails: This test has a long message, because all characters are tested.");
		}
		
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

