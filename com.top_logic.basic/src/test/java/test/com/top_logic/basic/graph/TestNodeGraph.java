/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.graph;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.graph.Graph;
import com.top_logic.basic.graph.NodeGraph;
import com.top_logic.basic.graph.NodeGraph.Edge;

/**
 * Test case for the {@link Graph} interface with {@link NodeGraph} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestNodeGraph extends TestHashGraph {

	public void testNodeAPI() {
		NodeGraph<String, String> graph = new NodeGraph<>();

		assertNull(graph.node("singleton"));
		graph.add("singleton");
		assertNotNull(graph.node("singleton"));

		graph.connect("foo", "foo", "foo-foo");
		graph.connect("foo", "bar", "foo-bar");
		graph.connect("bar", "bar", "bar-bar");
		
		assertEquals("foo-bar", graph.node("foo").connectTo(graph.node("bar")).userObject());

		// Add again (noop).
		graph.add("foo");

		assertEquals(set("foo", "bar", "singleton"), graph.vertices());
		assertEquals(set(graph.node("foo"), graph.node("bar"), graph.node("singleton")), toSet(graph.nodes()));

		assertEquals(set(), graph.outgoing("singleton"));
		assertEquals(set(), graph.incoming("singleton"));
		assertEquals(set("foo", "bar"), graph.outgoing("foo"));
		assertEquals(set("bar"), graph.outgoing("bar"));
		assertEquals(set("foo"), graph.incoming("foo"));
		assertEquals(set("foo", "bar"), graph.incoming("bar"));

		assertEquals("foo-foo", graph.edge("foo", "foo"));
		assertEquals("foo-bar", graph.edge("foo", "bar"));
		assertEquals(null, graph.edge("singleton", "foo"));
		assertEquals(null, graph.edge("foo", "yyy"));
		assertEquals(null, graph.edge("xxx", "yyy"));

		assertEdges(set("foo-foo", "foo-bar"), graph.outgoingEdges("foo"));
		assertEdges(set("foo-foo"), graph.incomingEdges("foo"));

		assertEdges(set("bar-bar"), graph.outgoingEdges("bar"));
		assertEdges(set("foo-bar", "bar-bar"), graph.incomingEdges("bar"));

		assertNoEdges(graph.incomingEdges("singleton"));
		assertNoEdges(graph.incomingEdges("xxx"));
		assertNoEdges(graph.outgoingEdges("xxx"));

		assertEquals("foo-bar", graph.node("bar").edgeFrom("foo").userObject());
		assertEquals("foo-bar", graph.node("foo").edgeTo("bar").userObject());

		assertTrue(graph.contains("bar"));
		graph.remove("bar");
		assertFalse(graph.contains("bar"));

		assertEquals(set(), graph.outgoing("singleton"));
		assertEquals(set(), graph.incoming("singleton"));
		assertEquals(set("foo"), graph.outgoing("foo"));
		assertEquals(set(), graph.outgoing("bar"));
		assertEquals(set("foo"), graph.incoming("foo"));
		assertEquals(set(), graph.incoming("bar"));

		graph.remove("bar");
		assertEquals(set(), graph.outgoing("singleton"));
		assertEquals(set(), graph.incoming("singleton"));
		assertEquals(set("foo"), graph.outgoing("foo"));
		assertEquals(set(), graph.outgoing("bar"));
		assertEquals(set("foo"), graph.incoming("foo"));
		assertEquals(set(), graph.incoming("bar"));

		graph.disconnect("foo", "foo");
		assertEquals(set(), graph.outgoing("singleton"));
		assertEquals(set(), graph.incoming("singleton"));
		assertEquals(set(), graph.outgoing("foo"));
		assertEquals(set(), graph.incoming("foo"));

		assertNull(graph.disconnect("foo", "foo"));
		assertNull(graph.disconnect("foo", "xxx"));
		assertNull(graph.disconnect("xxx", "yyy"));
	}

	private void assertNoEdges(Collection<? extends Edge<?, ?>> edges) {
		assertTrue(edges.isEmpty());
	}

	private <E> void assertEdges(Set<E> expected, Collection<? extends Edge<?, E>> edges) {
		HashSet<E> values = new HashSet<>();
		for (Edge<?, E> edge : edges) {
			values.add(edge.userObject());
		}
		assertEquals(expected, values);
	}

	@Override
	protected Graph<String, String> newGraph() {
		return new NodeGraph<>();
	}

}
