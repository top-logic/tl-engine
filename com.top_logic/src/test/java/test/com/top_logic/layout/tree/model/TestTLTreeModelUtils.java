/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeModelUtil;

/**
 * Tests {@link TLTreeModelUtil}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTLTreeModelUtils extends TestCase {
	
	public void testUpdateChildren() {
		DefaultMutableTLTreeModel model = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode root = model.getRoot();
		assertUpdateChildren(root, list());
		assertUpdateChildren(root, list(1, 2, 3, 4, 5));
		{
			// copy nodes because child list could be reused.
			List<DefaultMutableTLTreeNode> oldChildren = new ArrayList<>(root.getChildren());
			assertUpdateChildren(root, list(1, 2, 3, 4, 5));
			assertEquals("Business objects does not change, no new nodes must be created.", oldChildren,
				root.getChildren());
		}
		{
			// copy nodes because child list could be reused.
			List<DefaultMutableTLTreeNode> oldChildren = new ArrayList<>(root.getChildren());
			assertUpdateChildren(root, list(1, 3, 4, 2, 5));
			assertEquals("No new nodes must be created if just order of business objects changed.", toSet(oldChildren),
				toSet(root.getChildren()));
		}
		{
			// copy nodes because child list could be reused.
			List<DefaultMutableTLTreeNode> oldChildren = new ArrayList<>(root.getChildren());
			assertUpdateChildren(root, list(4, 2));
			assertTrue("No new nodes must be created if business objects were deleted.",
				oldChildren.containsAll(root.getChildren()));
		}
		{
			// copy nodes because child list could be reused.
			List<DefaultMutableTLTreeNode> oldChildren = new ArrayList<>(root.getChildren());
			assertUpdateChildren(root, list(8, 9, 2, 5, 4, 7));
			assertTrue("No new nodes must be created if business are added.",
				root.getChildren().containsAll(oldChildren));
			assertEquals("Old node must be reused", oldChildren.get(0), root.getChildren().get(4));
			assertEquals("Old node must be reused", oldChildren.get(1), root.getChildren().get(2));
		}

	}
	
	

	private <N extends AbstractMutableTLTreeNode<N>> void assertUpdateChildren(N parent, List<?> businessObjects) {
		TLTreeModelUtil.updateChildren(parent, businessObjects.iterator());
		List<N> children = parent.getChildren();
		assertEquals("Incorrect number of new children", businessObjects.size(), children.size());
		for (int i = 0; i < children.size(); i++) {
			assertEquals("Child at position " + i + " has wrong business object.", businessObjects.get(i), children
				.get(i).getBusinessObject());
		}
	}



	public void testFindBestNode() {
		DefaultMutableTLTreeModel model = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode root = model.getRoot();

		DefaultMutableTLTreeNode child_1 = root.createChild("1");
		DefaultMutableTLTreeNode child_1_1 = child_1.createChild("1.1");
		DefaultMutableTLTreeNode child_1_1_1 = child_1_1.createChild("1.1.1");

		root.removeChild(0);
		assertEquals(0, root.getChildCount());

		DefaultMutableTLTreeNode new_child_1 = root.createChild("1");
		DefaultMutableTLTreeNode new_child_1_1 = new_child_1.createChild("1.1");
		DefaultMutableTLTreeNode new_child_1_1_1 = new_child_1_1.createChild("1.1.1");

		Object bestMatch = TLTreeModelUtil.findBestMatch(model, child_1_1_1, root);
		assertSame(new_child_1_1_1, bestMatch);
	}

	public void testFindBestNodeJustApproximated() {
		DefaultMutableTLTreeModel model = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode root = model.getRoot();
		
		DefaultMutableTLTreeNode child_1 = root.createChild("1");
		DefaultMutableTLTreeNode child_1_1 = child_1.createChild("1.1");
		DefaultMutableTLTreeNode child_1_1_1 = child_1_1.createChild("1.1.1");
		
		root.removeChild(0);
		assertEquals(0, root.getChildCount());
		
		DefaultMutableTLTreeNode new_child_1 = root.createChild("1");
		DefaultMutableTLTreeNode new_child_1_1 = new_child_1.createChild("1.1_new");
		DefaultMutableTLTreeNode new_child_1_1_1 = new_child_1_1.createChild("1.1.1");
		assertNotNull(new_child_1_1_1);
		
		Object bestMatch = TLTreeModelUtil.findBestMatch(model, child_1_1_1, root);
		assertSame(new_child_1, bestMatch);
	}
	
	public void testFindBestNodeNoChildren() {
		DefaultMutableTLTreeModel model = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode root = model.getRoot();

		DefaultMutableTLTreeNode child_1 = root.createChild("1");
		DefaultMutableTLTreeNode child_1_1 = child_1.createChild("1.1");
		DefaultMutableTLTreeNode child_1_1_1 = child_1_1.createChild("1.1.1");

		root.removeChild(0);
		assertEquals(0, root.getChildCount());

		Object bestMatch = TLTreeModelUtil.findBestMatch(model, child_1_1_1, root);
		assertSame(root, bestMatch);

	}

	public void testFindBestNodeStillInTree() {
		DefaultMutableTLTreeModel model = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode root = model.getRoot();

		DefaultMutableTLTreeNode child_1 = root.createChild("1");
		DefaultMutableTLTreeNode child_1_1 = child_1.createChild("1.1");
		DefaultMutableTLTreeNode child_1_1_1 = child_1_1.createChild("1.1.1");

		Object bestMatch = TLTreeModelUtil.findBestMatch(model, child_1_1_1, root);
		assertSame(child_1_1_1, bestMatch);

	}

	public static Test suite() {
		return new TestSuite(TestTLTreeModelUtils.class);
	}
}
