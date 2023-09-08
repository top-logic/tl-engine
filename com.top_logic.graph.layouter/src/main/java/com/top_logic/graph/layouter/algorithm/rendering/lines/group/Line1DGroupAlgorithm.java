/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines.group;

import java.util.Collection;

import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1D;
import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1DContainer;

/**
 * Algorithm to group a collection of one dimensional lines.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface Line1DGroupAlgorithm {

	/**
	 * Group one dimensional lines together.
	 */
	Collection<Line1DContainer> group(Collection<Line1D> lines);

}
