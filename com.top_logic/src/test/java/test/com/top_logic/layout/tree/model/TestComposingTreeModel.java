/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import junit.framework.TestCase;

import com.top_logic.layout.tree.model.ComposingTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;

/**
 * Test for {@link ComposingTreeModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestComposingTreeModel extends TestCase {

	public void testSimple() {
		Object root = "root";
		DefaultMutableTLTreeModel model1 = new DefaultMutableTLTreeModel("r1");
		DefaultMutableTLTreeNode m1c1 = model1.getRoot().createChild("c1");
		DefaultMutableTLTreeNode m1c2 = model1.getRoot().createChild("c2");

		DefaultMutableTLTreeModel model2 = new DefaultMutableTLTreeModel("r2");

		ComposingTreeModel composite = new ComposingTreeModel(root, model1, model2);
		assertTrue(composite.containsNode(model1.getRoot()));
		assertEquals(root, composite.getParent(model1.getRoot()));
		assertTrue(composite.containsNode(model2.getRoot()));
		assertEquals(root, composite.getParent(model2.getRoot()));
		assertEquals(list(model1.getRoot(), root), composite.createPathToRoot(model1.getRoot()));
		assertEquals(list(model2.getRoot(), root), composite.createPathToRoot(model2.getRoot()));
		
		assertEquals(list(model1.getRoot(), model2.getRoot()), composite.getChildren(composite.getRoot()));
		assertFalse(composite.isLeaf(composite.getRoot()));

		assertEquals(composite.getChildren(model1.getRoot()), model1.getChildren(model1.getRoot()));
		assertEquals(composite.getChildren(model2.getRoot()), model1.getChildren(model2.getRoot()));

		assertEquals(model1.getRoot(), composite.getParent(m1c1));
		assertEquals(model1.getBusinessObject(m1c2), composite.getBusinessObject(m1c2));
	}


}

