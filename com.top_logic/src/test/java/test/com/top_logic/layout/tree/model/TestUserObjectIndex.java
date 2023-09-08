/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import static test.com.top_logic.basic.BasicTestCase.*;
import junit.framework.TestCase;

import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.UserObjectIndex;

/**
 * Test of {@link UserObjectIndex}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestUserObjectIndex extends TestCase {

	private UserObjectIndex<DefaultMutableTLTreeNode> _index;

	private DefaultMutableTLTreeModel _model;

	private DefaultMutableTLTreeNode _root;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_index = new UserObjectIndex<>();
		_model = new DefaultMutableTLTreeModel();
		_root = _model.getRoot();
	}

	public void testAddDifferentNodesTwice() {
		String businessObject = "bo1";
		DefaultMutableTLTreeNode node1 = _root.createChild(businessObject);

		_index.handleInitNode(node1);
		assertEquals(list(node1), _index.getNodes(businessObject));
		_index.handleInitNode(node1);
		assertEquals("Initializing twice must not have any effect.", list(node1), _index.getNodes(businessObject));

		DefaultMutableTLTreeNode node2 = _root.createChild(businessObject);
		_index.handleInitNode(node2);
		assertEquals(set(node1, node2), toSet(_index.getNodes(businessObject)));
		_index.handleInitNode(node2);
		assertTrue(
			"Ticket #14831: Initializing same node again, when there are more than one node, must not remove the others.",
			_index.getNodes(businessObject).contains(node1));
		assertEquals(
			"Ticket #14831: Initializing same node again, when there are more than one node, must not remove the others.",
			set(node1, node2), toSet(_index.getNodes(businessObject)));
	}

}
