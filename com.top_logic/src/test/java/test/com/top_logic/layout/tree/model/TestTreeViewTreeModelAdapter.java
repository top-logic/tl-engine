/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.TreeView;
import com.top_logic.layout.tree.model.CachedTreeView;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;

/**
 * Test case for {@link CachedTreeView}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTreeViewTreeModelAdapter extends TestCase {

	private Map<String, List<String>> cyclicTree() {
		return new MapBuilder<String, List<String>>()
			.put("A", Arrays.asList("B", "C", "D"))
			.put("B", Arrays.asList("B", "E"))
			.put("C", Arrays.asList("A", "E"))
			.put("D", Arrays.asList("E"))
			.toMap();
	}
	
	static class MapTreeView<T> implements TreeView<T> {
		final Map<T, List<T>> childrenByNode;
		
		public MapTreeView(Map<T, List<T>> childrenByNode) {
			this.childrenByNode = childrenByNode;
		}

		@Override
		public Iterator<T> getChildIterator(Object node) {
			return children(node).iterator();
		}

		private List<T> children(Object node) {
			List<T> children = childrenByNode.get(node);
			if (children == null) {
				children = Collections.<T> emptyList();
			}
			return children;
		}

		@Override
		public boolean isLeaf(T node) {
			return children(node).isEmpty();
		}

		@Override
		public boolean isFinite() {
			return false;
		}

	}
	
	/**
	 * Test building a {@link CachedTreeView} upon a cyclic {@link TreeView} base model.
	 */
	public void testCyclicModel() {
		Map<String, List<String>> tree = cyclicTree();
		CachedTreeView<String> model = new CachedTreeView<>(new MapTreeView<>(tree), "A");
		
		{
			DefaultMutableTLTreeNode A = model.getRoot();
			assertEquals("A", A.getBusinessObject());
			
			List<? extends DefaultMutableTLTreeNode> A_BCD = model.getChildren(A);
			assertEquals(Arrays.asList("B", "C", "D"), getUserObjects(A_BCD));
			
			DefaultMutableTLTreeNode AB = A_BCD.get(0);
			assertEquals("B", AB.getBusinessObject());
			assertSame(A, model.getParent(AB));
			
			List<? extends DefaultMutableTLTreeNode> AB_BE = model.getChildren(AB);
			assertEquals(Arrays.asList("B", "E"), getUserObjects(AB_BE));
			
			DefaultMutableTLTreeNode ABB = AB_BE.get(0);
			assertNotSame(AB, ABB);
			assertSame(AB, model.getParent(ABB));
			assertEquals("B", ABB.getBusinessObject());
			assertEquals(Arrays.asList("B", "E"), getUserObjects(model.getChildren(ABB)));
		}
		
		// Modify tree view.
		tree.put("B", Arrays.asList("B", "E", "F"));
		model.updateUserObject("B");
		
		{
			DefaultMutableTLTreeNode A = model.getRoot();
			assertEquals("A", A.getBusinessObject());
			
			List<? extends DefaultMutableTLTreeNode> A_BCD = model.getChildren(A);
			assertEquals(Arrays.asList("B", "C", "D"), getUserObjects(A_BCD));
			
			DefaultMutableTLTreeNode AB = A_BCD.get(0);
			assertEquals("B", AB.getBusinessObject());
			assertSame(A, model.getParent(AB));
			
			List<? extends DefaultMutableTLTreeNode> AB_BEF = model.getChildren(AB);
			assertEquals(Arrays.asList("B", "E", "F"), getUserObjects(AB_BEF));
			
			DefaultMutableTLTreeNode ABB = AB_BEF.get(0);
			assertNotSame(AB, ABB);
			assertSame(AB, model.getParent(ABB));
			assertEquals("B", ABB.getBusinessObject());
			assertEquals(Arrays.asList("B", "E", "F"), getUserObjects(model.getChildren(ABB)));
		}
		
	}

	private static List<?> getUserObjects(List<? extends DefaultMutableTLTreeNode> children) {
		ArrayList<Object> result = new ArrayList<>(children.size());
		for (DefaultMutableTLTreeNode node : children) {
			result.add(node.getBusinessObject());
		}
		return result;
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return new TestSuite(TestTreeViewTreeModelAdapter.class);
	}
	
}
