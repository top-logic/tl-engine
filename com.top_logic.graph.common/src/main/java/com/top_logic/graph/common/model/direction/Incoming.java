/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.direction;

import java.util.Collection;

import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.Node;

/**
 * The oposite {@link Direction} to {@link Outgoing}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class Incoming implements Direction {

	/**
	 * Singleton {@link Outgoing} instance.
	 */
	public static final Direction INSTANCE = new Incoming();

	private Incoming() {
		// Singleton constructor.
	}

	@Override
	public Collection<? extends Edge> getEdges(Node self) {
		return self.getIncomingEdges();
	}

	@Override
	public Node getSelf(Edge edge) {
		return edge.getDestination();
	}

	@Override
	public Node getOther(Edge edge) {
		return edge.getSource();
	}

	@Override
	public Edge createEdge(GraphModel graph, Node self, Node other, Object tag) {
		return graph.createEdge(other, self, tag);
	}

}