/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.format;

import java.text.FieldPosition;
import java.text.ParsePosition;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.layout.form.format.MailAddressFormat;
import com.top_logic.layout.form.model.DescriptiveParsePosition;

/**
 * The TestMailAddressFormat tests the class 
 * {@link com.top_logic.layout.form.format.MailAddressFormat}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestMailAddressFormat extends BasicTestCase {

    /** 
     * This method  tests  
     * {@link com.top_logic.layout.form.format.MailAddressFormat#parseObject(String, ParsePosition)}.
     */
    public void testParseObject() {
        String validAddress           = "tdi@bos.local";
        String validAddressWithSpaces = "  tdi@bos.local ";
        String invalidAddress1        = "fsdkd@";
        String invalidAddress2        = "@bad.de";
        String invalidAddress3        = "good@good.";
        String nullAddress            = null;
        String emptyAddress           = "";
        
        
        MailAddressFormat format = new MailAddressFormat();
        DescriptiveParsePosition pos    = new DescriptiveParsePosition(0);
        assertEquals(format.parseObject(validAddress, pos),           validAddress);
        assertNull(pos.getErrorMessage());

        pos = new DescriptiveParsePosition(0);
        assertEquals(format.parseObject(validAddressWithSpaces, pos), validAddress);
        assertNull(pos.getErrorMessage());
        
        pos = new DescriptiveParsePosition(0);
        assertNull(format.parseObject(invalidAddress1, pos));
        assertTrue(pos.getIndex() < invalidAddress1.length());
        assertNotNull(pos.getErrorMessage());
        
        pos = new DescriptiveParsePosition(0);
        assertNull(format.parseObject(invalidAddress2, pos));
        assertTrue(pos.getIndex() < invalidAddress2.length());
        assertNotNull(pos.getErrorMessage());
        
        pos = new DescriptiveParsePosition(0);
        assertNull(format.parseObject(invalidAddress3, pos));
        assertTrue(pos.getIndex() < invalidAddress3.length());
        assertNotNull(pos.getErrorMessage());
        
        pos = new DescriptiveParsePosition(0);
        assertNull(format.parseObject(nullAddress, pos));
        assertNotNull(pos.getErrorMessage());

        pos = new DescriptiveParsePosition(0);
        assertNull(format.parseObject(emptyAddress, pos));
        assertNotNull(pos.getErrorMessage());
        
        try {
            format.parseObject(emptyAddress, null);
            fail();
        } catch (NullPointerException e) {
            /* expected */
        }
    }
    
    /** 
     * This method  tests  
     * {@link com.top_logic.layout.form.format.MailAddressFormat#format(Object, StringBuffer, FieldPosition)}.
     */
    public void testFormateObject() {
        MailAddressFormat format = new MailAddressFormat();
        FieldPosition     pos    = new FieldPosition(0);
        
        StringBuffer buffer  = new StringBuffer();
        String       address = "tdi@bos.local";
        format.format(address, buffer, pos);
        assertEquals(buffer.toString(), address);
        
        try {
            format.format(address, null, pos);
            fail();
        } catch (NullPointerException e) {
            /* expected */
        }
        
        try {
            format.format(pos, new StringBuffer(), null);
            fail();
        } catch (NullPointerException e) {
            /* expected */
        }
        
        try {
            format.format(Boolean.TRUE, buffer, pos);
        } catch (IllegalArgumentException e) {
            /* expected */
        }
    }
    
    /**
     * Tests {@link MailAddressFormat#checkEMailAddress(String)} with valid addresses.
     */
    public void testCheckValidAddresses() {
        assertValid("noreply@example.invalid");
        assertValid("a+b@x.online");
        assertValid("first.last@sub.example.com");
        assertValid("tld@example." + "x".repeat(63));
        assertValid("  tdi@bos.local ");
    }

    /**
     * Tests {@link MailAddressFormat#checkEMailAddress(String)} with invalid addresses.
     */
    public void testCheckInvalidAddresses() {
        assertInvalid("Max <max@firma.de>");
        assertInvalid("g: a@b.de;");
        assertInvalid("a@b.de, c@d.de");
        assertInvalid("admin@localhost");
        assertInvalid("@bad.de");
        assertInvalid("good@good.");
        assertInvalid("a b@c.de");
        assertInvalid("fsdkd@");
        assertInvalid(null);
        assertInvalid("");
        assertInvalid("   ");
    }

    private static void assertValid(String address) {
        assertTrue("Expected valid: " + address, MailAddressFormat.checkEMailAddress(address));
    }

    private static void assertInvalid(String address) {
        assertFalse("Expected invalid: " + address, MailAddressFormat.checkEMailAddress(address));
    }

    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        TestSuite theSuite = new TestSuite(TestMailAddressFormat.class);
        return TLTestSetup.createTLTestSetup(theSuite);
    }
    
    /**
     * This method can be used for direct testing.
     * 
     * @param args A string array of argument.
     */
    public static void main (String[] args) {
        Logger.configureStdout();
        junit.textui.TestRunner.run (suite());
    }
    
}

