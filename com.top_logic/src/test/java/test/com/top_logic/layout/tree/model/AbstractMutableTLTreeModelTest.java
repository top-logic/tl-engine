/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.ListBuilder;
import com.top_logic.layout.IndexPosition;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;

/**
 * Abstract tests for {@link AbstractMutableTLTreeModel}.
 * 
 * The class {@link AbstractMutableTLTreeModelTest} tests the {@link DefaultMutableTLTreeModel}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractMutableTLTreeModelTest<N extends AbstractMutableTLTreeNode<N>> extends
		AbstractTLTreeModelTest<N> {

    public void testMoveSubStructure() {
        child_1_1.createChild("1_1_1");
        child_1_1.createChild("1_1_2");
        
        child_2.createChild("2_1");
        child_2.createChild("2_2");
        
        assertEquals(1, child_1.getChildCount());
        assertEquals(2, child_1_1.getChildCount());
        assertEquals(2, child_2.getChildCount());

        child_1_1.moveTo(child_2, 1);
        
        assertEquals(0, child_1.getChildCount());
        assertEquals(3, child_2.getChildCount());
        assertEquals(child_1_1, child_2.getChildAt(1));
        assertEquals(2, child_1_1.getChildCount());
    }
    
    public void testMoveChild() {
        assertEquals(1, child_1.getChildCount());
        assertEquals(0, child_2.getChildCount());
        
        child_1_1.moveTo(child_2);
        
        assertEquals(0, child_1.getChildCount());
        assertEquals(1, child_2.getChildCount());
        assertFalse("IsLeaf must be reset upon add.", child_2.isLeaf());
    }
    
    public void testMoveChildDown() {
    	assertEquals(0, child_1_1.getChildCount());
    	
    	child_2.moveTo(child_1_1);
    	
    	assertEquals(1, rootNode.getChildCount());
    	assertEquals(1, child_1_1.getChildCount());
    	assertFalse("IsLeaf must be reset upon add.", child_1_1.isLeaf());
    }
    
    public void testMoveNodeUp() {
		N child_1_1_1 = child_1_1.createChild("1_1_1");
    	assertEquals(1, child_1_1.getChildCount());
    	assertEquals(1, child_1.getChildCount());
    	
    	child_1_1_1.moveTo(child_1);

    	assertEquals(0, child_1_1.getChildCount());
    	assertEquals(2, child_1.getChildCount());
    }
    
    public void testMoveToRoot() {
    	assertEquals(2, rootNode.getChildCount());
    	assertEquals(1, child_1.getChildCount());
    	
    	child_1_1.moveTo(rootNode);
    	
    	assertEquals(3, rootNode.getChildCount());
    	assertEquals(0, child_1.getChildCount());
    }
    
    /**
     * Tests to move a node in the children list to the end
     */
    public void testMoveToEnd() {
		final N child_1_2 = child_1.createChild("1_2");
    	
    	assertEquals(2, child_1.getChildCount());
    	assertEquals(child_1_1, child_1.getChildAt(0));
    	assertEquals(child_1_2, child_1.getChildAt(1));
    	
    	child_1_1.moveTo(child_1);

    	assertEquals(2, child_1.getChildCount());
    	assertEquals(child_1_2, child_1.getChildAt(0));
    	assertEquals(child_1_1, child_1.getChildAt(1));
    }
    
    /**
     * Tests to move a node in the children list backwards 
     */
    public void testMoveBackwards() {
		final N child_1_2 = child_1.createChild("1_2");
		final N child_1_3 = child_1.createChild("1_3");
		final N child_1_4 = child_1.createChild("1_4");
    	
    	assertEquals(4, child_1.getChildCount());
    	assertEquals(child_1_1, child_1.getChildAt(0));
    	assertEquals(child_1_2, child_1.getChildAt(1));
    	assertEquals(child_1_3, child_1.getChildAt(2));
    	assertEquals(child_1_4, child_1.getChildAt(3));
    	
    	child_1_2.moveTo(child_1, 2);
    	
    	assertEquals(4, child_1.getChildCount());
    	assertEquals(child_1_1, child_1.getChildAt(0));
    	assertEquals(child_1_3, child_1.getChildAt(1));
    	assertEquals(child_1_2, child_1.getChildAt(2));
    	assertEquals(child_1_4, child_1.getChildAt(3));
    }
    
    /**
     * Tests to move a node in the children list forwards 
     */
    public void testMoveForwards() {
		final N child_1_2 = child_1.createChild("1_2");
		final N child_1_3 = child_1.createChild("1_3");
		final N child_1_4 = child_1.createChild("1_4");
		final N child_1_5 = child_1.createChild("1_4");
    	
    	assertEquals(5, child_1.getChildCount());
    	assertEquals(child_1_1, child_1.getChildAt(0));
    	assertEquals(child_1_2, child_1.getChildAt(1));
    	assertEquals(child_1_3, child_1.getChildAt(2));
    	assertEquals(child_1_4, child_1.getChildAt(3));
    	assertEquals(child_1_5, child_1.getChildAt(4));
    	
    	child_1_2.moveTo(child_1, 3);
    	
    	assertEquals(5, child_1.getChildCount());
    	assertEquals(child_1_1, child_1.getChildAt(0));
    	assertEquals(child_1_3, child_1.getChildAt(1));
    	assertEquals(child_1_4, child_1.getChildAt(2));
    	assertEquals(child_1_2, child_1.getChildAt(3));
    	assertEquals(child_1_5, child_1.getChildAt(4));
    }
    
    /**
     * Tests to move a node in the children list forwards 
     */
    public void testNoOpMove() {
		final N child_1_2 = child_1.createChild("1_2");
		final N child_1_3 = child_1.createChild("1_3");
    	
    	assertEquals(3, child_1.getChildCount());
    	assertEquals(child_1_1, child_1.getChildAt(0));
    	assertEquals(child_1_2, child_1.getChildAt(1));
    	assertEquals(child_1_3, child_1.getChildAt(2));
    	
    	child_1_2.moveTo(child_1, 1);
    	
    	assertEquals(3, child_1.getChildCount());
    	assertEquals(child_1_1, child_1.getChildAt(0));
    	assertEquals(child_1_2, child_1.getChildAt(1));
    	assertEquals(child_1_3, child_1.getChildAt(2));
    }
    
    public void testIllegalMove1() {
    	try {
			child_1.moveTo(child_1_1);
			fail("Creation of cycles must be detected.");
		} catch (IllegalArgumentException e) {
			// Expected.
		}
    }
    
    public void testIllegalMove2() {
    	try {
    		child_1.moveTo(child_1);
    		fail("Creation of cycles must be detected.");
    	} catch (IllegalArgumentException e) {
    		// Expected.
    	}
    }
    
    public void testIllegalMove3() {
		AbstractMutableTLTreeModel<N> otherModel = createTreeModel();
    	
    	try {
    		child_1.moveTo(otherModel.getRoot());
    		fail("Nodes cannot be moved between models.");
    	} catch (IllegalArgumentException e) {
    		// Expected.
    	}
    }
    
    public void testRemoveAddSubStructure() {
        child_1_1.createChild("1_1_1");
        child_1_1.createChild("1_1_2");
        
        child_2.createChild("2_1");
        child_2.createChild("2_2");
        
        assertEquals(1, child_1.getChildCount());
        assertEquals(2, child_1_1.getChildCount());
        assertEquals(2, child_2.getChildCount());

        child_1.removeChild(child_1.getIndex(child_1_1));
		child_2.addChild(IndexPosition.before(1), child_1_1);
        
        assertEquals(0, child_1.getChildCount());
        assertEquals(3, child_2.getChildCount());
        assertEquals(child_1_1, child_2.getChildAt(1));
        assertEquals(2, child_1_1.getChildCount());
    }
    
    public void testRemoveAddChild() {
        assertEquals(1, child_1.getChildCount());
        assertEquals(0, child_2.getChildCount());
        
        child_1.removeChild(child_1.getIndex(child_1_1));
        child_2.addChild(child_1_1);
        
        assertEquals(0, child_1.getChildCount());
        assertEquals(1, child_2.getChildCount());
        assertFalse("IsLeaf must be reset upon add.", child_2.isLeaf());
    }
    
    public void testCreateChild() {
        assertEquals(rootNode, child_1.getParent());
        assertEquals(child_1.getBusinessObject(), "1");
        assertEquals(rootNode, child_2.getParent());
        assertEquals(child_2.getBusinessObject(), "2");

        assertEquals(2, rootNode.getChildCount());
        assertEquals(list(child_1, child_2), rootNode.getChildren());
        assertEquals(child_1, rootNode.getChildAt(0));
        assertEquals(0, rootNode.getIndex(child_1));
        assertEquals(child_2, rootNode.getChildAt(1));
        assertEquals(1, rootNode.getIndex(child_2));
        assertEquals(-1, rootNode.getIndex(new DefaultMutableTLTreeModel().getRoot()));
    }

    public void testCreateChildren() {
        assertTrue(child_1_1.isLeaf());
        assertTrue(child_1_1.createChildren(Collections.EMPTY_LIST).isEmpty());
        assertTrue(child_1_1.isLeaf());
        List children = new ListBuilder().add("A").add("B").add("C").toList();
		List<? extends N> nodeChildren = child_1_1.createChildren(children);
        assertFalse(child_1_1.isLeaf());
        assertEquals(3, nodeChildren.size());
        assertEquals(3, child_1_1.getChildCount());
        assertEquals("A", nodeChildren.get(0).getBusinessObject());
        assertEquals("B", nodeChildren.get(1).getBusinessObject());
        assertEquals("C", nodeChildren.get(2).getBusinessObject());

        children = new ListBuilder().add("D").toList();
		nodeChildren = child_1_1.createChildren(IndexPosition.before(1), children);
        assertFalse(child_1_1.isLeaf());
        assertEquals(1, nodeChildren.size());
        assertEquals(4, child_1_1.getChildCount());
        assertEquals("D", nodeChildren.get(0).getBusinessObject());
        nodeChildren = child_1_1.getChildren();
        assertEquals(4, nodeChildren.size());
        assertEquals("A", nodeChildren.get(0).getBusinessObject());
        assertEquals("D", nodeChildren.get(1).getBusinessObject());
        assertEquals("B", nodeChildren.get(2).getBusinessObject());
        assertEquals("C", nodeChildren.get(3).getBusinessObject());
    }

	public void testPathToParent() {
		assertEquals(list(child_1_1, child_1, rootNode), treemodel.createPathToRoot(child_1_1));
		assertEquals(list(child_1, rootNode), treemodel.createPathToRoot(child_1));
		assertEquals(list(child_2, rootNode), treemodel.createPathToRoot(child_2));
		assertEquals(list(rootNode), treemodel.createPathToRoot(rootNode));
	}

	public void testRemoveChild() {
		rootNode.removeChild(0);
		assertFalse(rootNode.getChildren().contains(child_1));
		assertEquals(-1, rootNode.getIndex(child_1));

		assertFalse(treemodel.createPathToRoot(child_1).contains(rootNode));
		assertFalse(treemodel.createPathToRoot(child_1_1).contains(rootNode));
	}

	public void testClearChildren() {
		rootNode.clearChildren();
		assertTrue(rootNode.getChildren().isEmpty());
		assertEquals(0, rootNode.getChildCount());

		assertFalse(treemodel.createPathToRoot(child_1).contains(rootNode));
		assertFalse(treemodel.createPathToRoot(child_1_1).contains(rootNode));
		assertFalse(treemodel.createPathToRoot(child_2).contains(rootNode));
	}

	public void testContainsNode() {
		N firstChild = rootNode.getChildAt(0);
		assertTrue("TreeModel must contain children of root", treemodel.containsNode(firstChild));
		
		rootNode.removeChild(0);
		
		assertFalse("Ticket #3935: TreeModel must not contain removed node.", treemodel.containsNode(firstChild));
	}

}
