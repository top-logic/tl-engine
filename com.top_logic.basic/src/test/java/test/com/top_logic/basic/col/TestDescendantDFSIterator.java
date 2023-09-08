/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.col.InverseComparator;
import com.top_logic.basic.col.MappedComparator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.NumberComparator;
import com.top_logic.basic.col.TreeView;

/**
 * Test case for {@link DescendantDFSIterator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDescendantDFSIterator extends BasicTestCase {

	private static final int CHILDS_PER_NODE = 4;
	private static final int MAX_NODE_NUMBER = 100;

	public void testDescend() {
		
		// Virtual tree view in integer numbers. 
		TreeView<Integer> intTreeView = new TreeView<>() {

		    @Override
			public boolean isLeaf(Integer aNode) {
		        Integer integerNode = aNode;
                
                return integerNode.intValue() >= 100;
		    }
		    
			@Override
			public Iterator<Integer> getChildIterator(Integer node) {
				int current = node.intValue();
				
				int firstChild = current * CHILDS_PER_NODE + 1;
				int lastChild = (current + 1) * CHILDS_PER_NODE;
				
				if (lastChild > MAX_NODE_NUMBER) 
					lastChild = MAX_NODE_NUMBER;
				
				ArrayList<Integer> childs = new ArrayList<>();
				for (int n = firstChild; n <= lastChild; n++) {
					childs.add(Integer.valueOf(n));
				}
				return childs.iterator();
			}
			
			@Override
			public boolean isFinite() {
				return false;
			}

		};
		
		TreeSet<Integer> allNodes = new TreeSet<>();
		
		for (Iterator<Integer> it = new DescendantDFSIterator<>(intTreeView, Integer.valueOf(0)); 
			it.hasNext(); ) 
		{
			Integer nextNode = it.next();
			assertFalse(allNodes.contains(nextNode));

			allNodes.add(nextNode);
		}
		
		assertEquals(Integer.valueOf(1), allNodes.first());
		assertEquals(Integer.valueOf(100), allNodes.last());
	}
	
	@SuppressWarnings("synthetic-access")
	public void testNaturalOrder() {
		TreeView<Node> nodeTreeView = new TestTreeView();

		Node root = createTestTree();

		// The root node is not returned, only *descendant* nodes.
		int[] expectedNodeOrder = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
		DescendantDFSIterator<Node> iterator = new DescendantDFSIterator<>(nodeTreeView, root, false);
		assertNodeOrder(expectedNodeOrder, iterator);
	}

	@SuppressWarnings("synthetic-access")
	public void testCustomOrder() {
		TreeView<Node> nodeTreeView = new TestTreeView();

		Node root = createTestTree();

		// The root node is not returned, only *descendant* nodes.
		int[] expectedNodeOrder = { 10, 12, 11, 9, 3, 8, 5, 7, 6, 4, 2 };
		MappedComparator<Node, Integer> nodeComparator = new MappedComparator<>(new Mapping<Node, Integer>() {

			@Override
			public Integer map(Node input) {
				return input.getNr();
			}
		}, NumberComparator.INSTANCE);
		DescendantDFSIterator<Node> iterator =
			new DescendantDFSIterator<>(nodeTreeView, root, false, new InverseComparator<>(nodeComparator));
		assertNodeOrder(expectedNodeOrder, iterator);
	}

	private void assertNodeOrder(int[] expectedNodeOrder, Iterator<Node> nodeIterator) {
		int nodeIndex = 0;
		int nodeCount = 0;
		while (nodeIterator.hasNext()) {
			Node nextNode = nodeIterator.next();
			assertEquals(expectedNodeOrder[nodeIndex++], nextNode.getNr());
			nodeCount++;
		}

		assertEquals("More or less nodes than expected has been returned by iterator!", expectedNodeOrder.length,
			nodeCount);
	}
	
	private Node createTestTree() {
		return new Node(1, new Node[] {
			new Node(2, null),
			new Node(3, new Node[] {
				new Node(4, null),
				new Node(5, new Node[] {
					new Node(6, null),
					new Node(7, null),
				}),
				new Node(8, null) }),
			new Node(9, null),
			new Node(10, new Node[] {
				new Node(11, null),
				new Node(12, null),
			})
		});
	}

    /**
	 * Return the suite of tests to execute.
	 */
    public static Test suite () {
        return BasicTestSetup.createBasicTestSetup(new TestSuite (TestDescendantDFSIterator.class));
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

	private final class TestTreeView implements TreeView<Node> {
		@Override
		public boolean isLeaf(Node aNode) {
			return aNode.getChildren() == null;
		}

		@Override
		public Iterator<Node> getChildIterator(Node node) {
			return Arrays.asList(node.getChildren()).iterator();
		}

		@Override
		public boolean isFinite() {
			return false;
		}
	}

}
