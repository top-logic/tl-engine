/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines.util;

import java.util.Comparator;

import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1DContainer;

/**
 * Compares line container by their priority.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LineContainerPriorityComparator implements Comparator<Line1DContainer> {

	@Override
	public int compare(Line1DContainer container1, Line1DContainer container2) {
		return Integer.compare(container1.getPriority(), container2.getPriority());
	}

}
