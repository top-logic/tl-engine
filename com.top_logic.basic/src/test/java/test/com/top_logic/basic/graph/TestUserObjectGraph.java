/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.graph;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.graph.ExplicitGraph;
import com.top_logic.basic.graph.Graph;
import com.top_logic.basic.graph.UserObjectGraph;

/**
 * Test case for the {@link Graph} interface with {@link ExplicitGraph} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestUserObjectGraph extends TestCase {

	UserObjectGraph<String, String> _graph;

	private Map<String, UserObjectGraph<String, String>.UserObjectNode> _nodes;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_graph = newGraph();
		_nodes = new HashMap<>();
	}

	public void testConnect() {
		add("singleton");

		connect("foo", "foo", "foo-foo");
		connect("foo", "bar", "foo-bar");
		connect("bar", "bar", "bar-bar");

		assertEquals(set("foo", "bar", "singleton"), vertices());

		// Add again (noop).
		add("foo");

		assertEquals(set(), outgoing("singleton"));
		assertEquals(set(), incoming("singleton"));
		assertEquals(set("foo", "bar"), outgoing("foo"));
		assertEquals(set("bar"), outgoing("bar"));
		assertEquals(set("foo"), incoming("foo"));
		assertEquals(set("foo", "bar"), incoming("bar"));

		assertEquals(set("foo-foo"), edges("foo", "foo"));
		assertEquals(set("foo-bar"), edges("foo", "bar"));
		assertEquals(set(), edges("singleton", "foo"));
		assertEquals(set(), edges("foo", "yyy"));
		assertEquals(set(), edges("xxx", "yyy"));

		assertTrue(contains("bar"));
		remove("bar");
		assertFalse(contains("bar"));

		assertEquals(set(), outgoing("singleton"));
		assertEquals(set(), incoming("singleton"));
		assertEquals(set("foo"), outgoing("foo"));
		assertEquals(set(), outgoing("bar"));
		assertEquals(set("foo"), incoming("foo"));
		assertEquals(set(), incoming("bar"));

		remove("bar");
		assertEquals(set(), outgoing("singleton"));
		assertEquals(set(), incoming("singleton"));
		assertEquals(set("foo"), outgoing("foo"));
		assertEquals(set(), outgoing("bar"));
		assertEquals(set("foo"), incoming("foo"));
		assertEquals(set(), incoming("bar"));

		disconnect("foo", "foo");
		assertEquals(set(), outgoing("singleton"));
		assertEquals(set(), incoming("singleton"));
		assertEquals(set(), outgoing("foo"));
		assertEquals(set(), incoming("foo"));

		assertEquals(set(), disconnect("foo", "foo"));
		assertEquals(set(), disconnect("foo", "xxx"));
		assertEquals(set(), disconnect("xxx", "yyy"));
	}

	private Set<String> vertices() {
		return user(_graph.nodes());
	}

	private boolean contains(String string) {
		return nodeFor(string) != null;
	}

	private void remove(String string) {
		_graph.remove(nodeFor(string));
		_nodes.remove(string);
	}

	private Set<String> disconnect(String string, String string2) {
		return userEdges(_graph.disconnect(nodeFor(string), nodeFor(string2)));
	}

	private Set<String> edges(String string, String string2) {
		return userEdges(_graph.edges(nodeFor(string), nodeFor(string2)));
	}

	private String user(UserObjectGraph<String, String>.UserObjectEdge edge) {
		return edge == null ? null : edge.getUserObject();
	}

	private Set<String> incoming(String string) {
		return user(_graph.incoming(nodeFor(string)));
	}

	private Set<String> outgoing(String string) {
		return user(_graph.outgoing(nodeFor(string)));
	}

	private Set<String> user(Set<UserObjectGraph<String, String>.UserObjectNode> nodes) {
		return Mappings.mapIntoSet(n -> n.getUserObject(), nodes);
	}

	private Set<String> userEdges(Set<UserObjectGraph<String, String>.UserObjectEdge> nodes) {
		return Mappings.mapIntoSet(n -> n.getUserObject(), nodes);
	}

	private void connect(String string, String string2, String string3) {
		_graph.connect(node(string), node(string2), string3);
	}

	private UserObjectGraph<String, String>.UserObjectNode node(String string) {
		UserObjectGraph<String, String>.UserObjectNode result = nodeFor(string);
		if (result == null) {
			result = _graph.add(string);
			_nodes.put(string, result);
		}
		return result;
	}

	private UserObjectGraph<String, String>.UserObjectNode nodeFor(String string) {
		return _nodes.get(string);
	}

	private UserObjectGraph<String, String>.UserObjectNode add(String label) {
		return node(label);
	}

	public void testRemove() {
		connect("foo", "bar", "foo-bar");
		assertEquals(set("bar"), outgoing("foo"));
		assertEquals(set("foo"), incoming("bar"));
		remove("foo");
		assertEquals(set("bar"), vertices());
		assertEquals(set(), outgoing("foo"));
		assertEquals(set(), incoming("bar"));
		assertEquals(set("bar"), vertices());
		remove("bar");
		assertEquals(set(), vertices());
	}

	public void testDisconnect() {
		connect("foo", "bar", "foo-bar");
		assertEquals(set("foo", "bar"), vertices());

		assertEquals(set("foo-bar"), disconnect("foo", "bar"));
		assertEquals(set(), outgoing("foo"));
		assertEquals(set(), incoming("bar"));
		assertEquals(set("foo", "bar"), vertices());
	}

	public void testNodeAPI() {
		assertNull(nodeFor("singleton"));
		add("singleton");
		assertNotNull(nodeFor("singleton"));

		connect("foo", "foo", "foo-foo");
		connect("foo", "bar", "foo-bar");
		connect("bar", "bar", "bar-bar");

		UserObjectGraph<String, String>.UserObjectEdge newEdge = _graph.connect(nodeFor("foo"), nodeFor("bar"));
		assertNull(user(newEdge));

		newEdge.setUserObject("foo-bar2");

		// Add again (noop).
		add("foo");

		assertEquals(set("foo", "bar", "singleton"), vertices());
		assertEquals(set(nodeFor("foo"), nodeFor("bar"), nodeFor("singleton")), nodes());

		assertEquals(set(), outgoing("singleton"));
		assertEquals(set(), incoming("singleton"));
		assertEquals(set("foo", "bar"), outgoing("foo"));
		assertEquals(set("bar"), outgoing("bar"));
		assertEquals(set("foo"), incoming("foo"));
		assertEquals(set("foo", "bar"), incoming("bar"));

		assertEquals(set("foo-foo"), edges("foo", "foo"));
		assertEquals(set("foo-bar", "foo-bar2"), edges("foo", "bar"));
		assertEquals(set(), edges("singleton", "foo"));
		assertEquals(set(), edges("foo", "yyy"));
		assertEquals(set(), edges("xxx", "yyy"));

		assertEdges(set("foo-foo", "foo-bar", "foo-bar2"), outgoingEdges("foo"));
		assertEdges(set("foo-foo"), incomingEdges("foo"));

		assertEdges(set("bar-bar"), outgoingEdges("bar"));
		assertEdges(set("foo-bar", "foo-bar2", "bar-bar"), incomingEdges("bar"));

		assertNoEdges(incomingEdges("singleton"));
		assertNoEdges(incomingEdges("xxx"));
		assertNoEdges(outgoingEdges("xxx"));

		assertEquals(set("foo-bar", "foo-bar2"), userEdges(nodeFor("bar").edgesFrom(nodeFor("foo"))));
		assertEquals(set("foo-bar", "foo-bar2"), userEdges(nodeFor("foo").edgesTo(nodeFor("bar"))));

		assertTrue(contains("bar"));
		remove("bar");
		assertFalse(contains("bar"));

		assertEquals(set(), outgoing("singleton"));
		assertEquals(set(), incoming("singleton"));
		assertEquals(set("foo"), outgoing("foo"));
		assertEquals(set(), outgoing("bar"));
		assertEquals(set("foo"), incoming("foo"));
		assertEquals(set(), incoming("bar"));

		remove("bar");
		assertEquals(set(), outgoing("singleton"));
		assertEquals(set(), incoming("singleton"));
		assertEquals(set("foo"), outgoing("foo"));
		assertEquals(set(), outgoing("bar"));
		assertEquals(set("foo"), incoming("foo"));
		assertEquals(set(), incoming("bar"));

		disconnect("foo", "foo");
		assertEquals(set(), outgoing("singleton"));
		assertEquals(set(), incoming("singleton"));
		assertEquals(set(), outgoing("foo"));
		assertEquals(set(), incoming("foo"));

		assertEquals(set(), disconnect("foo", "foo"));
		assertEquals(set(), disconnect("foo", "xxx"));
		assertEquals(set(), disconnect("xxx", "yyy"));
	}

	public void testMultiEdges() {
		connect("a", "b", "x");
		connect("a", "b", "y");
		assertEquals(set("x", "y"), edges("a", "b"));
	}

	private Collection<UserObjectGraph<String, String>.UserObjectEdge> incomingEdges(String string) {
		return _graph.incomingEdges(nodeFor(string));
	}

	private Collection<UserObjectGraph<String, String>.UserObjectEdge> outgoingEdges(String string) {
		return _graph.outgoingEdges(nodeFor(string));
	}

	private Set<UserObjectGraph<String, String>.UserObjectNode> nodes() {
		return _graph.nodes();
	}

	private void assertNoEdges(Collection<? extends UserObjectGraph<?, ?>.UserObjectEdge> edges) {
		assertTrue(edges.isEmpty());
	}

	private <E> void assertEdges(Set<E> expected, Collection<? extends UserObjectGraph<?, E>.UserObjectEdge> edges) {
		HashSet<E> values = new HashSet<>();
		for (UserObjectGraph<?, E>.UserObjectEdge edge : edges) {
			values.add(edge.getUserObject());
		}
		assertEquals(expected, values);
	}

	protected UserObjectGraph<String, String> newGraph() {
		return new UserObjectGraph<>();
	}

}
