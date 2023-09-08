/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.clean;

/**
 * Algorithm for cleaning the graph and structure.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface GraphCleanAlgorithm<G> {
	/**
	 * Cleans the given graph from unnecessary graph structures. For instance nodes or edges.
	 */
	public void clean(G graph);
}
