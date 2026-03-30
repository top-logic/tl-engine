/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.filter;

import java.util.Set;
import java.util.function.Predicate;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * {@link Predicate} that matches nodes that are marked.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class FilterMarkedNode implements Predicate<LayoutNode> {
	Set<LayoutNode> _markedNodes;

	/**
	 * Creates an {@link FilterMarkedNode} for the given marked {@link LayoutNode}s.
	 */
	public FilterMarkedNode(Set<LayoutNode> markedNodes) {
		_markedNodes = markedNodes;
	}

	@Override
	public boolean test(LayoutNode anObject) {
		return _markedNodes.contains(anObject);
	}

}
