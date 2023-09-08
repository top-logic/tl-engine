/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.merge;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.top_logic.base.merge.MergeMessage;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;

/**
 * Testcase for the {@link com.top_logic.base.merge.MergeMessage}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestMergeMessage extends TestCase {

    /**
     * Test some sorts of invalid usage.
     */
    public void testInvalid() {
        try {
            new MergeMessage(-1, null, MergeMessage.APPROVEABLE);
        } catch (IllegalArgumentException expectd) { /* expected */ } 
        try {
            new MergeMessage(MergeMessage.DEBUG, null, !MergeMessage.APPROVEABLE);
        } catch (IllegalArgumentException expectd) { /* expected */ } 
        try {
            new MergeMessage(MergeMessage.DEBUG, ResKey.text(""), MergeMessage.APPROVEABLE);
        } catch (IllegalArgumentException expectd) { /* expected */ } 
 
        try {
            new MergeMessage(-1, null);
        } catch (IllegalArgumentException expectd) { /* expected */ } 
        try {
            new MergeMessage(MergeMessage.DEBUG, null);
        } catch (IllegalArgumentException expectd) { /* expected */ } 
        try {
            new MergeMessage(MergeMessage.DEBUG, ResKey.text(""));
        } catch (IllegalArgumentException expectd) { /* expected */ } 
    }

    /**
     * Test default, simple usage.
     */
    public void testSimple() throws Exception {
		MergeMessage mm = new MergeMessage(MergeMessage.DEBUG, ResKey.forTest("debug.message"));

        assertNotNull(mm.toString());
        
        assertEquals(MergeMessage.DEBUG, mm.getLevel());
        assertEquals("DEBUG"           , mm.getLevelString());
		assertEquals("debug.message", mm.getMessage().toString());
        
        assertTrue (mm.isApproveable());
        assertFalse(mm.isApproved());
        
        mm.setApproved(true);
        assertTrue(mm.isApproved());

        mm.perform(null);
    }

    /**
     * Test useage with non aprovaeble usage.
     */
    public void testNonAprovaelable() throws Exception {
		MergeMessage mm =
			new MergeMessage(MergeMessage.INFO, ResKey.forTest("you.will.be.absorbed"), !MergeMessage.APPROVEABLE);

        assertNotNull(mm.toString());
        
        assertEquals(MergeMessage.INFO      , mm.getLevel());
        assertEquals("INFO"                 , mm.getLevelString());
		assertEquals("you.will.be.absorbed", mm.getMessage().toString());
        
        assertFalse(mm.isApproveable());
        assertTrue (mm.isApproved());
        
        mm.perform(null);

		mm = new MergeMessage(MergeMessage.ERROR, ResKey.forTest("you.wil.be.integerated"));
        mm.setApproveable(!MergeMessage.APPROVEABLE);
        
        assertNotNull(mm.toString());
        
        assertEquals(MergeMessage.ERROR        , mm.getLevel());
        assertEquals("ERROR"                   , mm.getLevelString());
		assertEquals("you.wil.be.integerated", mm.getMessage().toString());
        
        assertFalse(mm.isApproveable());
        assertTrue (mm.isApproved());
        
        mm.perform(null);
    }

    /**
     * Return the suite of test to execute.
     */
    public static Test suite () {

        TestSuite theSuite = new TestSuite(TestMergeMessage.class);
        return theSuite;
    }

    
    /**
     * Main method to start this test case.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {

        Logger.configureStdout();
        TestRunner.run (suite ());
    }

}
