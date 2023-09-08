/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.merge;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.top_logic.base.merge.MergeMessage;
import com.top_logic.base.merge.MergeTreeNode;
import com.top_logic.base.merge.RootMergeNode;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;

/**
 * Testcase for the {@link com.top_logic.base.merge.MergeTreeNode}.
  * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestMergeTreeNode extends TestCase {

    /** A List of messages in order as performed */
    static List list;
    
    /** 
     * Overriden to clear list after every test.
     */
    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
        list = null;
    }
    
    /**
     * Test main usage as plain TreeNode.
     */
    public void testTree() {
        RootMergeNode rootNode = new RootMergeNode();
        assertTrue   (   rootNode.isLeaf());
        assertTrue   (   rootNode.getAllowsChildren());
        assertTrue   (   CollectionUtil.isEmptyOrNull(rootNode.getChildren()));
        assertEquals (0, rootNode.getChildCount());
        assertNull   (   rootNode.getParent());
        assertEquals (-1,rootNode.getIndex(rootNode));
        assertNotNull(   rootNode.toString());
        
        MergeTreeNode node0     = new MergeTreeNode(rootNode, null    , null);
        MergeTreeNode node1     = new MergeTreeNode(rootNode, "Source", "Dest");
        
        assertFalse  (   rootNode.isLeaf());
        assertTrue   (   rootNode.getAllowsChildren());
        assertFalse  (   CollectionUtil.isEmptyOrNull(rootNode.getChildren()));
        assertEquals (2, rootNode.getChildCount());
        assertNotNull(   rootNode.toString());

        assertSame(node0, rootNode.getChildren().get(0));
        assertSame(node1, rootNode.getChildren().get(1));

        assertEquals (0       ,rootNode.getIndex(node0));
        assertSame   (node0  , rootNode.getChildAt(0));
        assertSame   (rootNode, node0.getParent());
        
        assertEquals (1       , rootNode.getIndex(node1));
        assertSame   (node1   , rootNode.getChildAt(1));
        assertSame   (rootNode, node1.getParent());

        assertSame(rootNode, node1.getParent());
    }

    /**
     * Test variants of performing messages.
     */
    public void testPerform() throws Exception {
        RootMergeNode rootNode = new RootMergeNode();
        
        MergeTreeNode node1     = new MergeTreeNode(rootNode, null    , null);
        MergeTreeNode node2     = new MergeTreeNode(rootNode, "Source", "Dest");

        rootNode.perform(); // quite a noop
        
		TestMessage t0 = new TestMessage(TestMessage.INFO, ResKey.text("Test 0"));
		TestMessage t1 = new TestMessage(TestMessage.INFO, ResKey.text("Test 1"));
		TestMessage t2 = new TestMessage(TestMessage.INFO, ResKey.text("Test 2"));
		TestMessage t3 = new TestMessage(TestMessage.INFO, ResKey.text("Test 3"));
		TestMessage t4 = new TestMessage(TestMessage.WARN, ResKey.text("Test 4"));
		TestMessage t5 = new TestMessage(TestMessage.DEBUG, ResKey.text("Test 5"));
		TestMessage t6 = new TestMessage(TestMessage.INFO, ResKey.text("Test 6"));
        
        rootNode.addPostMessage(t6);
        rootNode.addMessage(t1);
        node1   .addMessage(t2);
        node1   .addPostMessage(t3);
        node2   .addMessage(t5);
        node2   .addFirst  (t4);
        rootNode.addFirst  (t0);
        

		MergeMessage extra = node2.addMessage(TestMessage.DEBUG, ResKey.text("ignored"));
        
        list = new ArrayList(8);
        assertFalse(rootNode.hasErrors());
        assertNull    (rootNode.getErrorList());
        assertNotNull (rootNode.getMessageList());
        
        t0.setApproved(true);
        t1.setApproved(true);
        t2.setApproved(true);
        t3.setApproved(true);
        t4.setApproved(true);
        t5.setApproved(true);
        t6.setApproved(true);

        rootNode.perform(); // Should perform the message in order from 0 .. 6
        
        assertEquals(7, list.size());
        assertSame(t0, list.get(0));
        assertSame(t1, list.get(1));
        assertSame(t2, list.get(2));
        assertSame(t3, list.get(3));
        assertSame(t4, list.get(4));
        assertSame(t5, list.get(5));
        assertSame(t6, list.get(6));
        
        assertSame(extra, node2.getMessageList().get(2));
        
        // test the reverse processing for the ENTIRE tree
        rootNode.setSubtreeReverse(true);
        list.clear();
        rootNode.perform(); // Should perform the message in order from 6 .. 0
        
        assertEquals(7, list.size());
        assertSame(t2, list.get(0));
        assertSame(t3, list.get(1));
        assertSame(t4, list.get(2));
        assertSame(t5, list.get(3));
        assertSame(t0, list.get(4));
        assertSame(t1, list.get(5));
        assertSame(t6, list.get(6));
        
        // clear all messages and try to perform again
        rootNode.clearMessages();  
        list = null;
        rootNode.perform();  // Should be a noop now.
        assertFalse(rootNode.hasErrors());
    }

    /**
	 * Test Message Propagation to root.
	 */
    public void testPropagation() throws Exception {
        RootMergeNode rootNode = new RootMergeNode();
        
        MergeTreeNode node1     = new MergeTreeNode(rootNode, null    , null);

        rootNode.perform(); // quite a noop
        
		TestMessage t0 = new TestMessage(TestMessage.FATAL, ResKey.text("Test 0"));
		TestMessage t1 = new TestMessage(TestMessage.ERROR, ResKey.text("Test 1"));
		TestMessage t2 = new TestMessage(TestMessage.ERROR, ResKey.text("Test 21"));

        node1   .addMessage(t0);
        node1   .addFirst(t1);
        node1   .addPostMessage(t2); // will not propagate
        
        assertNotNull (rootNode.getErrorList());
        assertNull    (rootNode.getMessageList());
        assertSame(t1, rootNode.getErrorList().get(0));
        assertSame(t0, rootNode.getErrorList().get(1));
    }

    /**
     * Test invalid ways of using a MergeTreeNode.
     */
    public void testInvalid() {
        try {
            new MergeTreeNode(null, null , null);
            fail("Expected IllegalArgumentException");
        } catch (NullPointerException expected) { /* expected */ }
    }
    
    /** 
     * Inner class that will just add itslelf to list for tetsing.
     */
    static class TestMessage extends MergeMessage {
        
        /** 
         * Make superclass Constructor accessible.
         */
		public TestMessage(int aLevel, ResKey aMessage, boolean aIsApproveable) {
            super(aLevel, aMessage, aIsApproveable);
        }

        /** 
         * Make superclass Constructor accessible.
         */
		public TestMessage(int aLevel, ResKey aMessage) {
            super(aLevel, aMessage);
        }

        /** 
         * Add myself to list to be asserted on, later. 
         */
        @Override
		public void perform(MergeTreeNode aOwner) throws Exception {
            list.add(this);
        }
    }
    
    /**
     * Return the suite of test to execute.
     */
    public static Test suite () {
        return new TestSuite(TestMergeTreeNode.class);
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
