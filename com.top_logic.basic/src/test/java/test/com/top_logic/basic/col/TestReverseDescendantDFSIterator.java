/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Arrays;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.InverseComparator;
import com.top_logic.basic.col.MappedComparator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.NumberComparator;
import com.top_logic.basic.col.ReverseDescendantDFSIterator;
import com.top_logic.basic.col.TreeView;

/**
 * Test case for {@link ReverseDescendantDFSIterator}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class TestReverseDescendantDFSIterator extends BasicTestCase {

	@SuppressWarnings("synthetic-access")
	public void testNaturalOrder() {
		TreeView<Node> nodeTreeView = new TestTreeView();

		Node root = createTestTree();

		int[] expectedNodeOrder = { 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
		ReverseDescendantDFSIterator<Node> iterator = new ReverseDescendantDFSIterator<>(nodeTreeView, root, true);
		assertNodeOrder(expectedNodeOrder, iterator);
	}

	@SuppressWarnings("synthetic-access")
	public void testCustomOrder() {
		TreeView<Node> nodeTreeView = new TestTreeView();

		Node root = createTestTree();

		// The root node is not returned, only *descendant* nodes.
		int[] expectedNodeOrder = { 2, 4, 6, 7, 5, 8, 3, 9, 11, 12, 10 };
		MappedComparator<Node, Integer> nodeComparator = new MappedComparator<>(new Mapping<Node, Integer>() {

			@Override
			public Integer map(Node input) {
				return input.getNr();
			}
		}, NumberComparator.INSTANCE);
		ReverseDescendantDFSIterator<Node> iterator =
			new ReverseDescendantDFSIterator<>(nodeTreeView, root, false,
				new InverseComparator<>(nodeComparator));
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
        return BasicTestSetup.createBasicTestSetup(new TestSuite (TestReverseDescendantDFSIterator.class));
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
