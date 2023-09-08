/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.filter;

import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * {@link Filter} nodes that are marked.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class FilterMarkedNode implements Filter<LayoutNode> {
	Set<LayoutNode> _markedNodes;

	/**
	 * Creates an {@link FilterMarkedNode} for the given marked {@link LayoutNode}s.
	 */
	public FilterMarkedNode(Set<LayoutNode> markedNodes) {
		_markedNodes = markedNodes;
	}

	@Override
	public boolean accept(LayoutNode anObject) {
		if (_markedNodes.contains(anObject)) {
			return true;
		} else {
			return false;
		}
	}

}
