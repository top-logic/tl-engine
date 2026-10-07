/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.top_logic.graph.layouter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.comparator.LexicographicEdgeComparator;
import com.top_logic.graph.layouter.model.comparator.LexicographicEdgeReversedComparator;

/**
 * Test of {@link LexicographicEdgeComparator} and {@link LexicographicEdgeReversedComparator} on a
 * graph with parallel edges (several edges between the same pair of nodes).
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestLexicographicEdgeComparators extends BasicTestCase {

	private static final int NODES_PER_LAYER = 4;

	private static final int PARALLEL_EDGES = 3;

	private List<Object> _edges;

	private List<Object> _nodeList;

	private Map<Object, Integer> _nodePositions;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		LayoutGraph graph = new LayoutGraph();
		List<LayoutNode> sources = new ArrayList<>();
		List<LayoutNode> targets = new ArrayList<>();
		for (int n = 0; n < NODES_PER_LAYER; n++) {
			sources.add(graph.add(graph.newNode()));
			targets.add(graph.add(graph.newNode()));
		}

		_nodeList = new ArrayList<>();
		_nodeList.addAll(sources);
		_nodeList.addAll(targets);
		_nodePositions = new HashMap<>();
		for (int n = 0; n < _nodeList.size(); n++) {
			_nodePositions.put(_nodeList.get(n), n);
		}

		_edges = new ArrayList<>();
		for (LayoutNode source : sources) {
			for (LayoutNode target : targets) {
				for (int n = 0; n < PARALLEL_EDGES; n++) {
					_edges.add(graph.connect(source, target, null));
				}
			}
		}
	}

	/**
	 * Tests the contract of {@link LexicographicEdgeComparator}.
	 */
	public void testLexicographicEdgeComparator() {
		for (LayoutDirection direction : LayoutDirection.values()) {
			assertComparatorContract(new LexicographicEdgeComparator(_nodePositions, _nodeList, direction));
		}
	}

	/**
	 * Tests the contract of {@link LexicographicEdgeReversedComparator}.
	 */
	public void testLexicographicEdgeReversedComparator() {
		for (LayoutDirection direction : LayoutDirection.values()) {
			assertComparatorContract(new LexicographicEdgeReversedComparator(_nodePositions, _nodeList, direction));
		}
	}

	private void assertComparatorContract(Comparator<Object> comparator) {
		for (Object first : _edges) {
			assertEquals("An edge must be equal to itself.", 0, comparator.compare(first, first));
			for (Object second : _edges) {
				assertEquals("Comparison must be antisymmetric.",
					Integer.signum(comparator.compare(first, second)),
					-Integer.signum(comparator.compare(second, first)));
			}
		}

		Random random = new Random(42);
		for (int n = 0; n < 100; n++) {
			List<Object> edges = new ArrayList<>(_edges);
			Collections.shuffle(edges, random);
			edges.sort(comparator);
			for (int i = 1; i < edges.size(); i++) {
				assertTrue("Edges must be sorted.", comparator.compare(edges.get(i - 1), edges.get(i)) <= 0);
			}
		}
	}

	/**
	 * a cumulative {@link Test} for all tests in {@link TestLexicographicEdgeComparators}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestLexicographicEdgeComparators.class));
	}

}
