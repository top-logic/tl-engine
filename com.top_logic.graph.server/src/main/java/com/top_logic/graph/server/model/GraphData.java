/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.model;

import com.top_logic.basic.listener.EventType;
import com.top_logic.graph.common.model.impl.SharedGraph;

/**
 * Server-side model of a graph component.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GraphData {

	/**
	 * {@link EventType} announcing a change in the {@link #getGraph()} property.
	 */
	EventType<GraphModelListener, GraphData, SharedGraph> GRAPH =
		new EventType<>("graph") {
			@Override
			public Bubble dispatch(GraphModelListener listener, GraphData sender,
					SharedGraph oldValue, SharedGraph newValue) {
				listener.handleGraphChange(sender, oldValue, newValue);
				return Bubble.BUBBLE;
			}
	};

	/**
	 * The graph that is shared with the client-side.
	 */
	SharedGraph getGraph();

	/**
	 * See {@link #getGraph()}
	 */
	void setGraph(SharedGraph graphModel);

	/**
	 * Defines the behavior if a drag-and-drop operation delivers data to this {@link GraphData}.
	 */
	GraphDropTarget getDropTarget();

	/**
	 * @see #getDropTarget()
	 */
	void setDropTarget(GraphDropTarget value);

	/**
	 * Adds the given {@link GraphModelListener}.
	 * 
	 * @param listener
	 *        The {@link GraphModelListener} to add.
	 * @return Whether the listener was newly added.
	 */
	boolean addGraphListener(GraphModelListener listener);

	/**
	 * Removes the given {@link GraphModelListener}.
	 * 
	 * @param listener
	 *        The listener to remove.
	 * @return Whether the given listener was registered before.
	 */
	boolean removeGraphListener(GraphModelListener listener);
}
