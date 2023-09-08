/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.export;

/**
 * Abstract JSON exporter for graphs.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface JSONGraphExporter<G> {
	/**
	 * @param graph
	 *        Graph object.
	 * @return JSON raw data format of the given graph.
	 */
	public String export(G graph);
}
