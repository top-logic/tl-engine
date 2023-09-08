/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.comparator;

import java.util.Comparator;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Comparator for {@link LayoutNode} vertical coordinates.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayoutNodeYComparator implements Comparator<LayoutNode> {

	@Override
	public int compare(LayoutNode o1, LayoutNode o2) {
		if (o1.getY() < o2.getY()) {
			return -1;
		} else if (o1.getY() > o2.getY()) {
			return 1;
		} else {
			return 0;
		}
	}

}
