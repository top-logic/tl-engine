/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.coordinates.horizontal.aligner;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Structure storing the vertical alignment, a set of vertical blocks of {@link LayoutNode}s.
 * 
 * @see VerticalAlignAlgorithm
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class VerticalAlignment {
	private Map<LayoutNode, LayoutNode> _roots = new LinkedHashMap<>();

	private Map<LayoutNode, LayoutNode> _aligns = new LinkedHashMap<>();

	/**
	 * Creates a {@link VerticalAlignment}.
	 */
	public VerticalAlignment(Map<LayoutNode, LayoutNode> roots, Map<LayoutNode, LayoutNode> aligns) {
		setRoots(roots);
		setAligns(aligns);
	}

	/**
	 * Root {@link LayoutNode}s of each block.
	 */
	public Map<LayoutNode, LayoutNode> getRoots() {
		return _roots;
	}

	/**
	 * @see #getRoots()
	 */
	public void setRoots(Map<LayoutNode, LayoutNode> roots) {
		_roots = roots;
	}

	/**
	 * {@link Map} describing which {@link LayoutNode} is aligned next in the same vertical
	 *         block.
	 */
	public Map<LayoutNode, LayoutNode> getAligns() {
		return _aligns;
	}

	/**
	 * @see #getAligns()
	 */
	public void setAligns(Map<LayoutNode, LayoutNode> aligns) {
		_aligns = aligns;
	}
}
