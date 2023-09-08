/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.direction;

import java.util.Collection;

import com.top_logic.basic.shared.collection.map.MappedCollection;
import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.Node;

/**
 * Algorithm abstracting the direction of a graph.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Direction {

	/**
	 * The {@link Edge}s adjacent to the given node in this {@link Direction}.
	 */
	Collection<? extends Edge> getEdges(Node self);

	/**
	 * For an {@link Edge} returned from {@link #getEdges(Node)}, the {@link Node} on that
	 * {@link #getEdges(Node)} was called.
	 */
	Node getSelf(Edge edge);

	/**
	 * The end of the given {@link Edge} in the oposite direction of {@link #getSelf(Edge)}.
	 */
	Node getOther(Edge edge);

	/**
	 * Creates an {@link Edge} from the given self node to the other node in this {@link Direction}.
	 */
	Edge createEdge(GraphModel graph, Node self, Node other, Object tag);

	/**
	 * The {@link #getOther(Edge)} nodes of the {@link #getEdges(Node)} of the given {@link Node}.
	 */
	default Collection<Node> getNext(Node self) {
		return new MappedCollection<>(this::getOther, getEdges(self));
	}

	/**
	 * The {@link Outgoing} direction.
	 */
	static Direction outgoing() {
		return Outgoing.INSTANCE;
	}

	/**
	 * The {@link Outgoing} direction.
	 */
	static Direction outgoing(boolean value) {
		return value ? outgoing() : incoming();
	}

	/**
	 * The {@link Incoming} direction.
	 */
	static Direction incoming() {
		return Incoming.INSTANCE;
	}

}
