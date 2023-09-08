/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.graph;

import static test.com.top_logic.basic.BasicTestCase.*;

import junit.framework.TestCase;

import com.top_logic.basic.graph.Graph;
import com.top_logic.basic.graph.HashGraph;
import com.top_logic.basic.graph.NodeGraph;

/**
 * Test case for the {@link Graph} interface with {@link NodeGraph} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestHashGraph extends TestCase {

	public void testConnect() {
		Graph<String, String> graph = newGraph();

		graph.add("singleton");

		graph.connect("foo", "foo", "foo-foo");
		graph.connect("foo", "bar", "foo-bar");
		graph.connect("bar", "bar", "bar-bar");
		
		assertEquals(set("foo", "bar", "singleton"), graph.vertices());

		// Add again (noop).
		graph.add("foo");

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

	public void testRemove() {
		Graph<String, String> graph = newGraph();
		graph.connect("foo", "bar", "foo-bar");
		assertEquals(set("bar"), graph.outgoing("foo"));
		assertEquals(set("foo"), graph.incoming("bar"));
		graph.remove("foo");
		assertEquals(set(), graph.outgoing("foo"));
		assertEquals(set(), graph.incoming("bar"));
		assertEquals(set("bar"), graph.vertices());
		graph.remove("bar");
		assertEquals(set(), graph.vertices());
	}

	public void testDisconnect() {
		Graph<String, String> graph = newGraph();
		graph.connect("foo", "bar", "foo-bar");
		assertEquals(set("foo", "bar"), graph.vertices());

		assertEquals("foo-bar", graph.disconnect("foo", "bar"));
		assertEquals(set(), graph.outgoing("foo"));
		assertEquals(set(), graph.incoming("bar"));
		assertEquals(set("foo", "bar"), graph.vertices());
	}

	protected Graph<String, String> newGraph() {
		Graph<String, String> graph = new HashGraph<>();
		return graph;
	}

}
