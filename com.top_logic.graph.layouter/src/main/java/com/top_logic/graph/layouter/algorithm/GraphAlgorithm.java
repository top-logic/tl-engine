/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm;

/**
 * Abstract graph algorithm.
 * 
 * @param <G>
 *        Type of graph.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class GraphAlgorithm<G> {
	private G _graph;

	/**
	 * Creates an graph algorithm for the given graph.
	 * 
	 * @param graph
	 *        Graph on which the algorithm is applied.
	 */
	public GraphAlgorithm(G graph) {
		setGraph(graph);
	}

	/**
	 * Used graph.
	 */
	public G getGraph() {
		return _graph;
	}

	/**
	 * @param graph
	 *        A new graph.
	 */
	public void setGraph(G graph) {
		_graph = graph;
	}
}