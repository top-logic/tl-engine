/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.graph;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.graph.Graph;
import com.top_logic.basic.graph.GraphPartitioning;
import com.top_logic.basic.graph.HashGraph;
import com.top_logic.basic.graph.StronglyConnectedComponents;

/**
 * Tests for {@link TestStronglyConnectedComponents} implementation.
 * 
 * @author <a href="mailto:wta@top-logic.com">wta</a>
 */
@SuppressWarnings("javadoc")
public class TestStronglyConnectedComponents extends TestCase {

	public void testEmptyGraph() {
		final Graph<String, String> graph = newGraph();

		final List<Set<String>> components = StronglyConnectedComponents.findComponents(graph);
		assertEquals(0, components.size());
	}

	public void testSingleVertexGraph() {
		final Graph<String, String> graph = newGraph();

		graph.add("A");

		final List<Set<String>> components = StronglyConnectedComponents.findComponents(graph);
		assertEquals(1, components.size());
	}

	public void testSingleVertexCyclicGraph() {
		final Graph<String, String> graph = newGraph();

		final String a = "A";

		graph.add(a);

		graph.connect(a, a, "a->a");

		final List<Set<String>> components = StronglyConnectedComponents.findComponents(graph);
		assertEquals(list(set(a)), components);
	}

	public void testTwoVertexGraph() {
		final Graph<String, String> graph = newGraph();

		final String a = "A";
		final String b = "B";

		graph.add(a);
		graph.add(b);

		graph.connect(a, b, "a->b");

		final List<Set<String>> components = StronglyConnectedComponents.findComponents(graph);
		assertEquals(list(set(a), set(b)), components);
	}

	public void testTwoVertexCyclicGraph() {
		final Graph<String, String> graph = newGraph();

		final String a = "A";
		final String b = "B";

		graph.add(a);
		graph.add(b);

		graph.connect(a, b, "a->b");
		graph.connect(b, a, "b->a");

		final List<Set<String>> components = StronglyConnectedComponents.findComponents(graph);
		assertEquals(list(set(a, b)), components);
	}

	public void testComplexCyclicGraph() {
		final Graph<String, String> graph = newGraph();

		final String a = "A";
		final String b = "B";
		final String c = "C";
		final String d = "D";
		final String e = "E";

		graph.add(a);
		graph.add(b);
		graph.add(c);
		graph.add(d);
		graph.add(e);

		graph.connect(a, b, "a->b");
		graph.connect(b, a, "b->a");

		graph.connect(c, d, "c->d");
		graph.connect(d, c, "d->c");

		graph.connect(a, c, "a->c");
		graph.connect(b, e, "b->e");
		graph.connect(d, e, "d->e");

		final List<Set<String>> components = StronglyConnectedComponents.findComponents(graph);
		assertEquals(list(set(a, b), set(c, d), set(e)), components);

		// Test partitioning.
		Graph<Graph<String, String>, String> partitionedGraph = GraphPartitioning.partition(graph, components);
		Map<Set<String>, Graph<String, String>> subgraphByComponent = new HashMap<>();
		Set<Set<String>> componentSet = toSet(components);
		for (Graph<String, String> subgraph : partitionedGraph.vertices()) {
			assertTrue(componentSet.contains(subgraph.vertices()));

			subgraphByComponent.put(subgraph.vertices(), subgraph);
		}
		assertEquals("a->c", partitionedGraph.edge(subgraphByComponent.get(set(a, b)), subgraphByComponent.get(set(c, d))));
		assertEquals("b->e", partitionedGraph.edge(subgraphByComponent.get(set(a, b)), subgraphByComponent.get(set(e))));
		assertEquals("d->e", partitionedGraph.edge(subgraphByComponent.get(set(c, d)), subgraphByComponent.get(set(e))));

		assertEquals("a->b", subgraphByComponent.get(set(a, b)).edge(a, b));
		assertEquals("b->a", subgraphByComponent.get(set(a, b)).edge(b, a));

		assertEquals("c->d", subgraphByComponent.get(set(c, d)).edge(c, d));
		assertEquals("d->c", subgraphByComponent.get(set(c, d)).edge(d, c));
	}

	protected Graph<String, String> newGraph() {
		return new HashGraph<>();
	}

}
