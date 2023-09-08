/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.top_logic.basic.col.TreeViewAdapter;

/**
 * Test the {@link TreeViewAdapter}
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestTreeViewAdapter extends TestCase {

    public TestTreeViewAdapter(String name) {
        super(name);
    }

    /**
     * Test TreeViewAdapter with a single root Node.
     */
    public void testRootOnly() {
        
        TreeNode        root = new DefaultMutableTreeNode("Root");
        TreeModel       tm   = new DefaultTreeModel(root);
        
        TreeViewAdapter tva  = new TreeViewAdapter(tm);
        
        assertFalse((! tva.isLeaf(root)));
        assertFalse(tva.getChildIterator(root).hasNext());
        try {
            tva.getChildIterator(root).remove();
            fail ("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) { /* epxected */ }
    }

    /**
     * Test TreeViewAdapter with two Children.
     */
    public void testChildren() {
        
        DefaultMutableTreeNode  root   = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode  child1 = new DefaultMutableTreeNode("Kai");
        DefaultMutableTreeNode  child2 = new DefaultMutableTreeNode("???");
        root.add(child1);
        root.add(child2);
        TreeModel       tm   = new DefaultTreeModel(root);
        
        TreeViewAdapter tva  = new TreeViewAdapter(tm);
        
        assertTrue((! tva.isLeaf(root)));
        Iterator children = tva.getChildIterator(root);
        assertTrue(children.hasNext());
        assertSame(child1, children.next());
        assertTrue(children.hasNext());
        assertSame(child2, children.next());
        assertFalse(children.hasNext());
        try {
            tva.getChildIterator(root).remove();
            fail ("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) { /* epxected */ }
    }

    /**
     * Returns a suite of tests.
     */
    public static Test suite() {
        return new TestSuite(TestTreeViewAdapter.class);
    }

    /**
     * This main function is for direct testing.
     */
    public static void main(String[] args) {
        TestRunner.run (suite ());
    }    
}

