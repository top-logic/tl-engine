/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import static test.com.top_logic.basic.BasicTestCase.*;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TreePath;

/**
 * Test case for {@link TreePath}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTreePath extends TestCase {

	public void testIdentify() {
		DefaultMutableTLTreeModel tree = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode r = tree.getRoot();

		DefaultMutableTLTreeNode a = r.createChild("a");
		r.createChild("x");

		a.createChild("a");
		DefaultMutableTLTreeNode b = a.createChild("b");
		a.createChild("x");

		b.createChild("a");
		b.createChild("b");
		DefaultMutableTLTreeNode c = b.createChild("c");
		b.createChild("y");

		TreePath rootPath = TreePath.fromNode(tree, r);
		assertTrue(rootPath.isRoot());
		assertTrue(rootPath.isValid());
		assertSame(r, rootPath.toNode(tree));

		DefaultMutableTLTreeModel other = new DefaultMutableTLTreeModel();

		// A root path identifies the root object in all models.
		assertSame(other.getRoot(), rootPath.toNode(other));

		TreePath cPath = TreePath.fromNode(tree, c);
		assertTrue(cPath.isValid());
		assertSame(c, cPath.toNode(tree));

		TreePath bPath = cPath.getParent();
		assertSame(b, bPath.toNode(tree));

		TreePath aPath = bPath.getParent();
		assertSame(a, aPath.toNode(tree));
		assertFalse(aPath.isRoot());

		TreePath rootPath2 = aPath.getParent();
		assertSame(r, rootPath2.toNode(tree));
		assertTrue(rootPath2.isRoot());

		// Parent of root is invalid.
		TreePath rootParentPath = rootPath2.getParent();
		assertNull(rootParentPath.toNode(tree));
		assertFalse(rootParentPath.isValid());
		assertFalse(rootParentPath.isRoot());

		// Parent of invalid is still invalid.
		assertFalse(rootParentPath.getParent().isValid());
		assertNull(rootParentPath.getParent().toNode(tree));

		// Drop identified node.
		b.removeChild(b.getIndex(c));

		// Path still valid.
		assertTrue(cPath.isValid());

		// But does no longer identify any node.
		assertNull(cPath.toNode(tree));

		// But when re-creating a node with the same user object...
		DefaultMutableTLTreeNode newC = b.createChild("c");

		// ... the new object is found.
		assertSame(newC, cPath.toNode(tree));

		DefaultMutableTLTreeNode none = r.createChild("none");
		r.removeChild(r.getIndex(none));

		assertFalse(TreePath.fromNode(tree, none).isValid());
		assertNull(TreePath.fromNode(tree, none).toNode(tree));
	}

	public void testEquality() {
		DefaultMutableTLTreeModel tree = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode r = tree.getRoot();
		DefaultMutableTLTreeNode a = r.createChild("a");
		DefaultMutableTLTreeNode b = a.createChild("b");
		DefaultMutableTLTreeNode c = a.createChild("c");

		TreePath bPath = TreePath.fromNode(tree, b);

		assertEquals(bPath, bPath);
		assertNotEquals(bPath, null);

		TreePath cPath = TreePath.fromNode(tree, c);
		assertNotEquals(bPath, cPath);
		assertNotEquals(cPath, bPath);

		TreePath aPath = TreePath.fromNode(tree, a);
		assertEquals(aPath, bPath.getParent());
		assertEquals(aPath, cPath.getParent());
		TreePath rPath = TreePath.fromNode(tree, r);
		assertEquals(rPath, aPath.getParent());
		assertNotEquals(rPath, rPath.getParent());
		assertNotEquals(rPath.getParent(), rPath);
	}

	public static void assertEquals(Object a, Object b) {
		BasicTestCase.assertEquals(a, b);
		BasicTestCase.assertEquals(b, a);
		BasicTestCase.assertEquals(b.hashCode(), a.hashCode());
	}
}
