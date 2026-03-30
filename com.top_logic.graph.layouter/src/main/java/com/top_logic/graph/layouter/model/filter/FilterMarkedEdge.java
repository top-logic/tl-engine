/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.filter;

import java.util.Set;
import java.util.function.Predicate;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;

/**
 * {@link Predicate} that matches edges that are marked.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class FilterMarkedEdge implements Predicate<LayoutEdge> {
	Set<LayoutEdge> _markedEdges;

	/**
	 * Creates an {@link FilterMarkedEdge} for the given marked {@link LayoutEdge}s.
	 */
	public FilterMarkedEdge(Set<LayoutEdge> markedEdges) {
		_markedEdges = markedEdges;
	}

	@Override
	public boolean test(LayoutEdge edge) {
		return _markedEdges.contains(edge);
	}
}
