/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines.group;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1D;
import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1DContainer;

/**
 * Group one dimensional lines with the same target together.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LineEndGrouper implements Line1DGroupAlgorithm {

	/**
	 * Singleton instance for {@link LineEndGrouper}.
	 */
	public static final LineEndGrouper INSTANCE = new LineEndGrouper();

	private LineEndGrouper() {
		// Singleton
	}

	@Override
	public Collection<Line1DContainer> group(Collection<Line1D> lines) {
		return groupingByLineEnd(lines).values().stream().map(createLineContainer()).collect(Collectors.toList());
	}

	private Function<? super List<Line1D>, ? extends Line1DContainer> createLineContainer() {
		return groupedLines -> new Line1DContainer(groupedLines);
	}

	private Map<Double, List<Line1D>> groupingByLineEnd(Collection<Line1D> lines) {
		return lines.stream().collect(Collectors.groupingBy(Line1D::getEnd, LinkedHashMap::new, Collectors.toList()));
	}

}
