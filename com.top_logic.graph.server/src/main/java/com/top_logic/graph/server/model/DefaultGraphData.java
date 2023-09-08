/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.model;

import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.graph.common.model.impl.SharedGraph;

/**
 * Default {@link GraphData} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultGraphData extends PropertyObservableBase implements GraphData {

	private SharedGraph _graph;

	private GraphDropTarget _dropTarget = NoGraphDrop.INSTANCE;

	/**
	 * Creates a {@link DefaultGraphData}.
	 *
	 * @param graph
	 *        See {@link #getGraph()}
	 */
	public DefaultGraphData(SharedGraph graph) {
		super();
		_graph = graph;
	}

	@Override
	public SharedGraph getGraph() {
		return _graph;
	}

	@Override
	public void setGraph(SharedGraph newGraph) {
		SharedGraph oldGraph = _graph;
		if (oldGraph != newGraph) {
			_graph = newGraph;
			notifyListeners(GRAPH, this, oldGraph, newGraph);
		}
	}

	@Override
	public GraphDropTarget getDropTarget() {
		return _dropTarget;
	}

	@Override
	public void setDropTarget(GraphDropTarget dropTarget) {
		_dropTarget = dropTarget;
	}

	@Override
	public boolean addGraphListener(GraphModelListener listener) {
		return addListener(GRAPH, listener);
	}

	@Override
	public boolean removeGraphListener(GraphModelListener listener) {
		return removeListener(GRAPH, listener);
	}
}
