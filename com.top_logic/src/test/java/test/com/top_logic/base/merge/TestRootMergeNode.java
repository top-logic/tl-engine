/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.merge;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.top_logic.base.merge.MergeMessage;
import com.top_logic.base.merge.RootMergeNode;
import com.top_logic.basic.Logger;

/**
 * Test the {@link RootMergeNode}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestRootMergeNode extends TestCase {

    /**
     * Show the expected usage / performance via a validator.
     * 
     * This should result in complete covergae.
     */
    public void testValidator() throws Exception {
        RootMergeNode    node      = new RootMergeNode();
        TestingValidator validator = new TestingValidator();
        
        assertFalse(validator.validate(node));
        assertTrue (node.hasErrors());
        try {
            node.perform();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) { /* expected, hasErrors() */ }

        node      = new RootMergeNode("Not a List");
        validator = new TestingValidator();
        assertFalse(validator.validate(node));
        assertTrue (node.hasErrors());
        // assertFalse(node.hasMessages());
        try {
            node.perform();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) { /* expected, hasErrors() */ }

        node      = new RootMergeNode(Collections.EMPTY_LIST);
        validator = new TestingValidator();
        assertTrue (validator.validate(node));
        assertFalse(node.hasErrors());
        // assertTrue (node.hasMessages());
        node.perform();

        node      = new RootMergeNode(Collections.singletonList("Not aNumber"));
        validator = new TestingValidator();
        
        assertTrue(validator.validate(node));
        ((MergeMessage) node.getMessageList().get(0)).setApproved(true);
        try {
            node.perform(); // ist not 100% fool prof ...
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) { /* expected */ }

        node      = new RootMergeNode(Collections.singletonList("99"));
        validator = new TestingValidator();
        
        assertTrue(validator.validate(node));
        node.perform();
        assertTrue(((List) node.getDest()).isEmpty()); // was not approved 

        node      = new RootMergeNode(Collections.singletonList("99"));
        validator = new TestingValidator();
        
        assertTrue(validator.validate(node));
        ((MergeMessage) node.getMessageList().get(0)).setApproved(true);
        node.perform();
		assertEquals(Integer.valueOf(99), ((List) node.getDest()).get(0));

        node      = new RootMergeNode(Collections.singletonList("99"), "Unexpected dest");
        validator = new TestingValidator();
        
        try {
            validator.validate(node);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) { /* expected */ }
    }

    /**
     * Return the suite of test to execute.
     */
    public static Test suite () {

        TestSuite theSuite = new TestSuite(TestRootMergeNode.class);
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
