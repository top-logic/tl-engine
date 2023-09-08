/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.filter;

import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;

/**
 * {@link Filter} edges that are marked.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class FilterMarkedEdge implements Filter<LayoutEdge> {
	Set<LayoutEdge> _markedEdges;

	/**
	 * Creates an {@link FilterMarkedEdge} for the given marked {@link LayoutEdge}s.
	 */
	public FilterMarkedEdge(Set<LayoutEdge> markedEdges) {
		_markedEdges = markedEdges;
	}

	@Override
	public boolean accept(LayoutEdge edge) {
		if (_markedEdges.contains(edge)) {
			return true;
		} else {
			return false;
		}
	}
}
