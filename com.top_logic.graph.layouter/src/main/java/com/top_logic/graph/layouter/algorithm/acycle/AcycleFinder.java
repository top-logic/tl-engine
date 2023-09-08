/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.acycle;

/**
 * Abstract maximal acyclic subgraph finder.
 * 
 * @see AcycleAlgorithm
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class AcycleFinder implements AcycleAlgorithm {

	/**
	 * Creates an acyclic graph finder.
	 */
	public AcycleFinder() {
		// Nothing to do.
	}

}
