/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import static com.top_logic.basic.col.TupleFactory.*;
import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TreeNodeComparatorLocal;

/**
 * Test case for {@link TreeNodeComparatorLocal}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTreeNodeComparatorLocal extends TestCase {

	public void testCompare() {
		DefaultMutableTLTreeModel tree = new DefaultMutableTLTreeModel();

		DefaultMutableTLTreeNode root = tree.getRoot();
		root.setBusinessObject("X");

		DefaultMutableTLTreeNode a1;
		DefaultMutableTLTreeNode b1;
		DefaultMutableTLTreeNode c1;
		DefaultMutableTLTreeNode x1;

		List<DefaultMutableTLTreeNode> nodes = new ArrayList<>();
		nodes.add(a1 = root.createChild("A"));
		nodes.add(b1 = root.createChild("B"));
		nodes.add(c1 = root.createChild("C"));
		nodes.add(x1 = root.createChild("X"));

		TreeNodeComparatorLocal comparator = new TreeNodeComparatorLocal(ComparableComparator.INSTANCE, false);
		Collections.sort(nodes, comparator);

		assertEquals(list(x1, a1, b1, c1), nodes);
	}

	@SuppressWarnings("synthetic-access")
	public void testCompareStructured() {
		DefaultMutableTLTreeModel tree = new DefaultMutableTLTreeModel();

		DefaultMutableTLTreeNode root = tree.getRoot();
		root.setBusinessObject(newTuple("X"));

		DefaultMutableTLTreeNode a1;
		DefaultMutableTLTreeNode b1;
		DefaultMutableTLTreeNode c1;
		DefaultMutableTLTreeNode x2;
		DefaultMutableTLTreeNode x1;

		List<DefaultMutableTLTreeNode> nodes = new ArrayList<>();
		nodes.add(a1 = root.createChild(newTuple("A", 1)));
		nodes.add(b1 = root.createChild(newTuple("B", 1)));
		nodes.add(c1 = root.createChild(newTuple("C", 1)));
		nodes.add(x2 = root.createChild(newTuple("X", 2)));
		nodes.add(x1 = root.createChild(newTuple("X", 1)));

		TreeNodeComparatorLocal comparator = new TestingEquivalenceComparator(ComparableComparator.INSTANCE, false);
		Collections.sort(nodes, comparator);

		assertEquals(list(x1, x2, a1, b1, c1), nodes);

		assertTrue(comparator.compare(x1, a1) < 0);
		assertTrue(comparator.compare(root, root) == 0);

		TreeNodeComparatorLocal comparatorDesc = new TestingEquivalenceComparator(ComparableComparator.INSTANCE, true);
		Collections.sort(nodes, comparatorDesc);

		assertEquals(list(x2, x1, c1, b1, a1), nodes);

		assertTrue(comparatorDesc.compare(x1, a1) < 0);
		assertTrue(comparatorDesc.compare(root, root) == 0);

	}

	private final class TestingEquivalenceComparator extends TreeNodeComparatorLocal {
		private TestingEquivalenceComparator(Comparator businessObjectComparator, boolean descending) {
			super(businessObjectComparator, descending);
		}
	
		@Override
		protected boolean sameBusinessObject(Object parentObject, Object nodeObject) {
			return super.sameBusinessObject(first(parentObject), first(nodeObject));
		}
	
		protected Object first(Object parentObject) {
			return ((Tuple) parentObject).get(0);
		}
	}

	/**
	 * Tests if no exception is thrown if the root node and all children have <code>null</code> as
	 * business object.
	 */
	public void testAllNull() {
		DefaultMutableTLTreeModel tree = new DefaultMutableTLTreeModel();

		DefaultMutableTLTreeNode root = tree.getRoot();
		root.setBusinessObject(null);

		List<DefaultMutableTLTreeNode> nodes = new ArrayList<>();
		nodes.add(root.createChild(null));
		nodes.add(root.createChild(null));

		TreeNodeComparatorLocal comparator = new TreeNodeComparatorLocal(ComparableComparator.INSTANCE, false);
		// Assert no Exception is thrown:
		Collections.sort(nodes, comparator);
	}

	/**
	 * Tests if no exception is thrown if all child nodes have <code>null</code> as business object.
	 */
	public void testChildrenNull() {
		DefaultMutableTLTreeModel tree = new DefaultMutableTLTreeModel();

		DefaultMutableTLTreeNode root = tree.getRoot();
		root.setBusinessObject("ROOT");

		List<DefaultMutableTLTreeNode> nodes = new ArrayList<>();
		nodes.add(root.createChild(null));
		nodes.add(root.createChild(null));

		TreeNodeComparatorLocal comparator = new TreeNodeComparatorLocal(ComparableComparator.INSTANCE, false);
		// Assert no Exception is thrown:
		Collections.sort(nodes, comparator);
	}

	/**
	 * Tests if no exception is thrown if the root node has <code>null</code> as business object.
	 */
	public void testRootNull() {
		DefaultMutableTLTreeModel tree = new DefaultMutableTLTreeModel();

		DefaultMutableTLTreeNode root = tree.getRoot();
		root.setBusinessObject(null);

		DefaultMutableTLTreeNode a;
		DefaultMutableTLTreeNode b;

		List<DefaultMutableTLTreeNode> nodes = new ArrayList<>();
		nodes.add(a = root.createChild("A"));
		nodes.add(b = root.createChild("B"));

		TreeNodeComparatorLocal comparator = new TreeNodeComparatorLocal(ComparableComparator.INSTANCE, false);
		Collections.sort(nodes, comparator);

		assertEquals(list(a, b), nodes);
	}

	/**
	 * Tests if no exception is thrown if the root node and one of the children has
	 * <code>null</code> as business object.
	 */
	public void testRootNullSingleChildNull() {
		DefaultMutableTLTreeModel tree = new DefaultMutableTLTreeModel();

		DefaultMutableTLTreeNode root = tree.getRoot();
		root.setBusinessObject(null);

		DefaultMutableTLTreeNode a;
		DefaultMutableTLTreeNode b;

		List<DefaultMutableTLTreeNode> nodes = new ArrayList<>();
		nodes.add(a = root.createChild("A"));
		nodes.add(b = root.createChild(null));

		TreeNodeComparatorLocal comparator = new TreeNodeComparatorLocal(ComparableComparator.INSTANCE, false);
		Collections.sort(nodes, comparator);

		// b has the same business object that root has (null) and is therefore next to it.
		assertEquals(list(b, a), nodes);
	}

	/**
	 * Tests if no exception is thrown if one of the child nodes has <code>null</code> as business
	 * object.
	 */
	public void testSingleChildNull() {
		DefaultMutableTLTreeModel tree = new DefaultMutableTLTreeModel();

		DefaultMutableTLTreeNode root = tree.getRoot();
		root.setBusinessObject("ROOT");

		DefaultMutableTLTreeNode a;
		DefaultMutableTLTreeNode b;

		List<DefaultMutableTLTreeNode> nodes = new ArrayList<>();
		nodes.add(a = root.createChild("A"));
		nodes.add(b = root.createChild(null));

		TreeNodeComparatorLocal comparator = new TreeNodeComparatorLocal(ComparableComparator.INSTANCE, false);
		Collections.sort(nodes, comparator);

		assertEquals(list(a, b), nodes);
	}

	/**
	 * Tests if no exception is thrown if one of the child nodes has <code>null</code> as business
	 * object and another child has the same business object as root.
	 */
	public void testNullChildAndRootAffineChild() {
		DefaultMutableTLTreeModel tree = new DefaultMutableTLTreeModel();

		DefaultMutableTLTreeNode root = tree.getRoot();
		root.setBusinessObject("ROOT");

		DefaultMutableTLTreeNode a;
		DefaultMutableTLTreeNode b;
		DefaultMutableTLTreeNode c;
		DefaultMutableTLTreeNode r;

		List<DefaultMutableTLTreeNode> nodes = new ArrayList<>();
		nodes.add(a = root.createChild("A"));
		nodes.add(b = root.createChild(null));
		nodes.add(c = root.createChild("C"));
		nodes.add(r = root.createChild("ROOT"));

		TreeNodeComparatorLocal comparator = new TreeNodeComparatorLocal(ComparableComparator.INSTANCE, false);
		Collections.sort(nodes, comparator);

		assertEquals(list(r, a, c, b), nodes);
	}

}
