/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.graph.BFSTree;
import com.top_logic.basic.graph.DFSTree;
import com.top_logic.basic.graph.MapGraph;
import com.top_logic.basic.graph.Traversal;
import com.top_logic.basic.graph.TraversalCollector;

/**
 * Test case for {@link Traversal} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTraversal extends BasicTestCase {

	private MapGraph<String> graph;


	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Map<String, List<String>> edges = new HashMap<>();
		edges.put("a", list("b", "c", "d"));
		edges.put("b", list("e", "f"));
		edges.put("c", list("g", "h"));
		edges.put("d", list("i", "j"));
		edges.put("g", list("k", "l", "m"));
		edges.put("i", list("n", "o", "p"));
		this.graph = new MapGraph<>(edges);
	}
	
	/**
	 * Test for {@link DFSTree}.
	 */
	public void testDFSTree() {
		assertEquals(list("a", "b", "e", "f", "c", "g", "k", "l", "m", "h", "d", "i", "n", "o", "p", "j"), 
			traverse(new DFSTree<>(graph), "a").getNodes());
	}
	
	/**
	 * Test for {@link DFSTree} with excluded start node.
	 */
	public void testDFSTreeExcludeStart() {
		assertEquals(list("b", "e", "f", "c", "g", "k", "l", "m", "h", "d", "i", "n", "o", "p", "j"), 
				traverse(new DFSTree<>(graph).setExcludeStart(true), "a").getNodes());
	}
	
	/**
	 * Test for {@link DFSTree} with depth limit.
	 */
	public void testDFSTreeDepthLimit() {
		assertEquals(list("b", "e", "f", "c", "g", "h", "d", "i", "j"), 
				traverse(new DFSTree<>(graph).setExcludeStart(true).setMaxDepth(2), "a").getNodes());
	}
	
	/**
	 * Test for {@link DFSTree} with post order traversal.
	 */
	public void testDFSTreePostOrder() {
		assertEquals(list("e", "f", "b", "g", "h", "c", "i", "j", "d"), 
				traverse(new DFSTree<>(graph).setPostOrder(true).setExcludeStart(true).setMaxDepth(2), "a").getNodes());
	}

	/**
	 * Test for {@link BFSTree}.
	 */
	public void testBFSTree() {
		assertEquals(list("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p"), 
				traverse(new BFSTree<>(graph), "a").getNodes());
	}
	
	/**
	 * Test for {@link BFSTree} with excluded start node.
	 */
	public void testBFSTreeExcludeStart() {
		assertEquals(list("b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p"), 
				traverse(new BFSTree<>(graph).setExcludeStart(true), "a").getNodes());
	}
	
	/**
	 * Test for {@link BFSTree} with depth limit.
	 */
	public void testBFSTreeDepthLimit() {
		assertEquals(list("b", "c", "d", "e", "f", "g", "h", "i", "j"), 
				traverse(new BFSTree<>(graph).setExcludeStart(true).setMaxDepth(2), "a").getNodes());
	}
	
	private static <T> TraversalCollector<T> traverse(Traversal<T> traversal, T startNode) {
		TraversalCollector<T> collector = new TraversalCollector<>(new ArrayList<>());
		traversal.traverse(startNode, collector);
		return collector;
	}
	
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestTraversal.class));
	}

}
