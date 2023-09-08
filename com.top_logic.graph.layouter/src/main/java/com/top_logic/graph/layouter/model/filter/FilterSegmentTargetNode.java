/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.filter;

import com.top_logic.basic.col.Filter;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * {@link Filter} segment target {@link LayoutNode}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class FilterSegmentTargetNode implements Filter<LayoutNode> {

	@Override
	public boolean accept(LayoutNode node) {
		if (node.isTargetDummy()) {
			return true;
		} else {
			return false;
		}
	}

}
