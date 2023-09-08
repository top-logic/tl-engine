/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.top_logic.graph.layouter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.graph.layouter.algorithm.acycle.EadesLinSmythAcycleFinder;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Tests if a graph is acyclic.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TestAcyclicGraph extends BasicTestCase {

	private LayoutGraph _graph;
	
	private Map<Integer, LayoutNode> _nodeMapping;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_graph = new LayoutGraph();
		_nodeMapping = new LinkedHashMap<>();

		initVertices();
		initEdges();
	}

	private void initVertices() {
		for (int i = 1; i <= 12; i++) {
			_nodeMapping.put(i, _graph.add(_graph.newNode()));
		}
	}

	private void initEdges() {
		_graph.connect(_nodeMapping.get(2), _nodeMapping.get(1));
		_graph.connect(_nodeMapping.get(3), _nodeMapping.get(1));
		_graph.connect(_nodeMapping.get(6), _nodeMapping.get(1));
		_graph.connect(_nodeMapping.get(4), _nodeMapping.get(3));
		_graph.connect(_nodeMapping.get(5), _nodeMapping.get(3));
		_graph.connect(_nodeMapping.get(7), _nodeMapping.get(4));
		_graph.connect(_nodeMapping.get(7), _nodeMapping.get(5));
		_graph.connect(_nodeMapping.get(8), _nodeMapping.get(7));
		_graph.connect(_nodeMapping.get(8), _nodeMapping.get(4));
		_graph.connect(_nodeMapping.get(8), _nodeMapping.get(5));
		_graph.connect(_nodeMapping.get(4), _nodeMapping.get(9));
		_graph.connect(_nodeMapping.get(5), _nodeMapping.get(10));
		_graph.connect(_nodeMapping.get(9), _nodeMapping.get(8));
		_graph.connect(_nodeMapping.get(10), _nodeMapping.get(8));
		_graph.connect(_nodeMapping.get(12), _nodeMapping.get(9));
		_graph.connect(_nodeMapping.get(12), _nodeMapping.get(10));
		_graph.connect(_nodeMapping.get(12), _nodeMapping.get(11));
		_graph.connect(_nodeMapping.get(11), _nodeMapping.get(6));
	}

	/**
	 * Test if the given graph is acyclic.
	 */
	public void testAcycleGraph() {
		assertTrue(isCyclic(_graph));

		LayoutGraph acyclicSubgraph = EadesLinSmythAcycleFinder.INSTANCE.findMaximalAcyclicSubgraph(_graph);

		assertFalse(isCyclic(acyclicSubgraph));
	}

	private boolean isCyclic(LayoutGraph graph) {
		Set<LayoutNode> visitedNodes = new LinkedHashSet<>();
		Set<LayoutNode> recursionStack = new LinkedHashSet<>();

		for (LayoutNode node : graph.nodes()) {
			if (hasCycle(node, visitedNodes, recursionStack)) {
				return true;
			}
		}

		return false;
	}

	private boolean hasCycle(LayoutNode node, Set<LayoutNode> visitedNodes, Set<LayoutNode> recursionStack) {
		if (recursionStack.contains(node)) {
			return true;
		}

		if(visitedNodes.contains(node)) {
			return false;
		}
		
		visitedNodes.add(node);
		recursionStack.add(node);
		
		for (LayoutNode nextNode : node.outgoing()) {
			if (hasCycle(nextNode, visitedNodes, recursionStack)) {
				return true;
			}
		}

		recursionStack.remove(node);
		
		return false;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestAcyclicGraph}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestAcyclicGraph.class));
	}
}
