/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.filter;

import java.util.function.Predicate;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * {@link Predicate} that matches segment source {@link LayoutNode}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class FilterSegmentSourceNode implements Predicate<LayoutNode> {

	@Override
	public boolean test(LayoutNode node) {
		return node.isSourceDummy();
	}

}
