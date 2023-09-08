/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.impl;

import com.top_logic.common.remote.event.ListenerContainer;
import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.GraphEvent;
import com.top_logic.graph.common.model.GraphListener;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.Label;
import com.top_logic.graph.common.model.Node;

/**
 * {@link ListenerContainer} of {@link GraphListener}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GraphListeners
		extends ListenerContainer<GraphListener, GraphEvent<?>>
		implements GraphListener, ListenerContainer.NotificationCallback<GraphListener, GraphEvent<?>> {

	@Override
	public void handleGraphEvent(GraphEvent<?> graphEvent) {
		notifyListeners(this, graphEvent);
	}

	@Override
	public void notifyListener(GraphListener listener, GraphEvent<?> event) {
		listener.handleGraphEvent(event);
	}

	/**
	 * Informs {@link GraphListener}s about a {@link Node} creation.
	 * 
	 * @param graph
	 *        The modified {@link GraphModel}.
	 * @param target
	 *        The created {@link Node}.
	 */
	public void nodeCreated(GraphModel graph, Node target) {
		if (idle()) {
			return;
		}
		handleGraphEvent(new GraphEvent.NodeCreated(graph, target));
	}

	/**
	 * Informs {@link GraphListener}s about an {@link Edge} creation.
	 * 
	 * @param graph
	 *        The modified {@link GraphModel}.
	 * @param target
	 *        The created {@link Edge}.
	 */
	public void edgeCreated(GraphModel graph, Edge target) {
		if (idle()) {
			return;
		}
		handleGraphEvent(new GraphEvent.EdgeCreated(graph, target));
	}

	/**
	 * Informs {@link GraphListener}s about an {@link Label} creation.
	 * 
	 * @param graph
	 *        The modified {@link GraphModel}.
	 * @param target
	 *        The created {@link Label}.
	 */
	public void labelCreated(GraphModel graph, Label target) {
		if (idle()) {
			return;
		}
		handleGraphEvent(new GraphEvent.LabelCreated(graph, target));
	}

	/**
	 * Informs {@link GraphListener}s about a {@link Node} deletion.
	 * 
	 * @param graph
	 *        The modified {@link GraphModel}.
	 * @param target
	 *        The deleted {@link Node}.
	 */
	public void nodeDeleted(GraphModel graph, Node target) {
		if (idle()) {
			return;
		}
		handleGraphEvent(new GraphEvent.NodeDeleted(graph, target));
	}

	/**
	 * Informs {@link GraphListener}s about an {@link Edge} deletion.
	 * 
	 * @param graph
	 *        The modified {@link GraphModel}.
	 * @param target
	 *        The deleted {@link Edge}.
	 */
	public void edgeDeleted(GraphModel graph, Edge target) {
		if (idle()) {
			return;
		}
		handleGraphEvent(new GraphEvent.EdgeDeleted(graph, target));
	}

	/**
	 * Informs {@link GraphListener}s about an {@link Label} deletion.
	 * 
	 * @param graph
	 *        The modified {@link GraphModel}.
	 * @param target
	 *        The deleted {@link Label}.
	 */
	public void labelDeleted(GraphModel graph, Label target) {
		if (idle()) {
			return;
		}
		handleGraphEvent(new GraphEvent.LabelDeleted(graph, target));
	}

	/**
	 * Informs {@link GraphListener}s about an internally modified {@link Node}.
	 * 
	 * @param graph
	 *        The modified {@link GraphModel}.
	 * @param target
	 *        The modified {@link Node}.
	 * @param property
	 *        The changed property.
	 */
	public void nodeChanged(GraphModel graph, Node target, String property) {
		if (idle()) {
			return;
		}
		handleGraphEvent(new GraphEvent.NodeChanged(graph, target, property));
	}

	/**
	 * Informs {@link GraphListener}s about an internally modified {@link Edge}.
	 * 
	 * @param graph
	 *        The modified {@link GraphModel}.
	 * @param target
	 *        The modified {@link Edge}.
	 * @param property
	 *        The changed property.
	 */
	public void edgeChanged(GraphModel graph, Edge target, String property) {
		if (idle()) {
			return;
		}
		handleGraphEvent(new GraphEvent.EdgeChanged(graph, target, property));
	}

	/**
	 * Informs {@link GraphListener}s about an internally modified {@link Label}.
	 * 
	 * @param graph
	 *        The modified {@link GraphModel}.
	 * @param target
	 *        The modified {@link Label}.
	 * @param property
	 *        The changed property.
	 */
	public void labelChanged(GraphModel graph, Label target, String property) {
		if (idle()) {
			return;
		}
		handleGraphEvent(new GraphEvent.LabelChanged(graph, target, property));
	}

}
