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
import com.top_logic.basic.col.Equality;
import com.top_logic.basic.col.InverseComparator;
import com.top_logic.basic.col.MappedComparator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.NumberComparator;
import com.top_logic.basic.col.ReverseCustomStartNodeDFSIterator;
import com.top_logic.basic.col.StructureView;

/**
 * {@link BasicTestCase} for {@link ReverseCustomStartNodeDFSIterator}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class TestReverseCustomStartNodeDFSIterator extends BasicTestCase {
	
	private TestStructureView _testStructure;

	@SuppressWarnings("synthetic-access")
	@Override
	protected void setUp() throws Exception {
		_testStructure = new TestStructureView();
	}

	public void testCustomStartNode() {

		Node node11 = new Node(11, null);
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
				node11,
				new Node(12, null),
			})
		});

		Node startNode = node11;
		int[] expectedNodeOrder = { 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
		ReverseCustomStartNodeDFSIterator<Node> iterator =
			new ReverseCustomStartNodeDFSIterator<>(_testStructure, root, startNode);

		assertNodeOrder(expectedNodeOrder, iterator);
	}

	public void testRootStartNodeIncludeRoot() {

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

		int[] expectedNodeOrder = { 1 };
		ReverseCustomStartNodeDFSIterator<Node> iterator =
			new ReverseCustomStartNodeDFSIterator<>(_testStructure, root, root);

		assertNodeOrder(expectedNodeOrder, iterator);
	}

	public void testRootStartNodeNotIncludeRoot() {

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

		int[] expectedNodeOrder = {};
		ReverseCustomStartNodeDFSIterator<Node> iterator =
			new ReverseCustomStartNodeDFSIterator<>(_testStructure, root, root, false,
				Equality.<Node> getInstance());

		assertNodeOrder(expectedNodeOrder, iterator);
	}

	public void testCustomStartNodeCustomOrder() {

		Node node5 = new Node(5, new Node[] {
			new Node(6, null),
			new Node(7, null),
		});
		Node root = new Node(1, new Node[] {
			new Node(2, null),
			new Node(3, new Node[] {
				new Node(4, null),
				node5,
				new Node(8, new Node[] {
					new Node(9, null),
					new Node(10, null)
				}) }),
			new Node(11, null),
			new Node(12, new Node[] {
				new Node(13, null),
				new Node(14, null),
			})
		});

		Node startNode = node5;
		int[] expectedNodeOrder = { 5, 9, 10, 8, 3, 11, 13, 14, 12 };
		MappedComparator<Node, Integer> nodeComparator = new MappedComparator<>(new Mapping<Node, Integer>() {

			@Override
			public Integer map(Node input) {
				return input.getNr();
			}
		}, NumberComparator.INSTANCE);
		ReverseCustomStartNodeDFSIterator<Node> iterator =
			new ReverseCustomStartNodeDFSIterator<>(_testStructure, root, startNode, false,
				new InverseComparator<>(nodeComparator));

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
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestReverseCustomStartNodeDFSIterator.class));
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
