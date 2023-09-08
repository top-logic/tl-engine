/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.CustomStartNodeDFSIterator;
import com.top_logic.basic.col.InverseComparator;
import com.top_logic.basic.col.MappedComparator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.NumberComparator;
import com.top_logic.basic.col.StructureView;

/**
 * {@link BasicTestCase} for {@link CustomStartNodeDFSIterator}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class TestCustomStartNodeDFSIterator extends BasicTestCase {
	
	private TestStructureView _testStructure;

	@SuppressWarnings("synthetic-access")
	@Override
	protected void setUp() throws Exception {
		_testStructure = new TestStructureView();
	}

	public void testCustomStartNode() {

		Node node4 = new Node(4, null);
		Node root = new Node(1, new Node[] {
			new Node(2, null),
			new Node(3, new Node[] {
				node4,
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

		Node startNode = node4;
		int[] expectedNodeOrder = { 4, 5, 6, 7, 8, 9, 10, 11, 12 };
		CustomStartNodeDFSIterator<Node> iterator = new CustomStartNodeDFSIterator<>(_testStructure, root, startNode);

		assertNodeOrder(expectedNodeOrder, iterator);
	}

	public void testRootStartNode() {

		Node root = new Node(1, new Node[] {
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

		int[] expectedNodeOrder = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
		CustomStartNodeDFSIterator<Node> iterator = new CustomStartNodeDFSIterator<>(_testStructure, root, root);

		assertNodeOrder(expectedNodeOrder, iterator);
	}

	public void testCustomStartNodeCustomOrder() {

		Node node7 = new Node(7, new Node[] {
			new Node(8, null),
			new Node(9, null),
		});
		Node root = new Node(1, new Node[] {
			new Node(2, null),
			new Node(3, new Node[] {
				new Node(4, new Node[] {
					new Node(5, null),
					new Node(6, null)
				}),
				node7,
				new Node(10, null) }),
			new Node(11, null),
			new Node(12, new Node[] {
				new Node(13, null),
				new Node(14, null),
			})
		});

		Node startNode = node7;
		int[] expectedNodeOrder = { 7, 9, 8, 4, 6, 5, 2 };
		MappedComparator<Node, Integer> nodeComparator = new MappedComparator<>(new Mapping<Node, Integer>() {

			@Override
			public Integer map(Node input) {
				return input.getNr();
			}
		}, NumberComparator.INSTANCE);
		CustomStartNodeDFSIterator<Node> iterator =
			new CustomStartNodeDFSIterator<>(_testStructure, root, startNode, new InverseComparator<>(nodeComparator));

		assertNodeOrder(expectedNodeOrder, iterator);
	}

	public void testAssertCustomStartNodeUnderRootNode() {
		
		Node node3 = new Node(3, new Node[] {
			new Node(4, null),
			new Node(5, new Node[] {
				new Node(6, null),
				new Node(7, null),
			}),
			new Node(8, null) });
		
		Node node9 = new Node(9, null);
		
		Node root = new Node(1, new Node[] {
			new Node(2, null),
			node3,
			node9,
			new Node(10, new Node[] {
				new Node(11, null),
				new Node(12, null),
			})
		});
		
		Node rootNode = node3;
		Node startNode = node9;
		
		try {
			Iterator<Node> iterator =
				new CustomStartNodeDFSIterator<>(_testStructure, rootNode, startNode);
			fail("Assertion must be thrown, when start node is not in subtree of root node!");
		} catch (IllegalArgumentException ex) {
			assertEquals(CustomStartNodeDFSIterator.ILLEGAL_START_NODE_MESSAGE, ex.getMessage());
		}
		
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

	/**
	 * Return the suite of tests to execute.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestCustomStartNodeDFSIterator.class));
	}

	private final class TestStructureView implements StructureView<Node> {

		@Override
		public boolean isLeaf(Node aNode) {
			return aNode.getChildren() == null;
		}

		@Override
		public Iterator<Node> getChildIterator(Node node) {
			if (node.getChildren() != null) {
				return Arrays.asList(node.getChildren()).iterator();
			} else {
				return Collections.<Node> emptyList().iterator();
			}
		}

		@Override
		public boolean isFinite() {
			return false;
		}

		@Override
		public Node getParent(Node node) {
			return node.getParent();
		}
	}

}
