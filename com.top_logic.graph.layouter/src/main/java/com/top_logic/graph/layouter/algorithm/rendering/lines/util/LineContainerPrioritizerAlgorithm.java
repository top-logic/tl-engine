/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines.util;

import java.util.Collection;

import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1DContainer;

/**
 * Algorithm to prioritize {@link Line1DContainer}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface LineContainerPrioritizerAlgorithm {

	/**
	 * Prioritize the given containers.
	 */
	void prioritize(Collection<Line1DContainer> containers);

}
