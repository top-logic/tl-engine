/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.comparator;

import java.util.Comparator;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Comparator for {@link LayoutNode} widths.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class LayoutNodeWidthComparator implements Comparator<LayoutNode> {

	@Override
	public int compare(LayoutNode node1, LayoutNode node2) {
		return (int) Math.signum(node1.getWidth() - node2.getWidth());
	}

}