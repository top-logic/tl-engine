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
 * The {@link Direction} from {@link Edge#getSource()} to {@link Edge#getDestination()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class Outgoing implements Direction {

	/**
	 * Singleton {@link Outgoing} instance.
	 */
	public static final Direction INSTANCE = new Outgoing();

	private Outgoing() {
		// Singleton constructor.
	}

	@Override
	public Collection<? extends Edge> getEdges(Node self) {
		return self.getOutgoingEdges();
	}

	@Override
	public Node getSelf(Edge edge) {
		return edge.getSource();
	}

	@Override
	public Node getOther(Edge edge) {
		return edge.getDestination();
	}

	@Override
	public Edge createEdge(GraphModel graph, Node self, Node other, Object tag) {
		return graph.createEdge(self, other, tag);
	}

}