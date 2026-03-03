/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactControl;

/**
 * A {@link ReactControl} that renders a CSS Grid container via the {@code TLGrid} React component.
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@code columns} - fixed column count</li>
 * <li>{@code minColumnWidth} - minimum column width for responsive auto-fit</li>
 * <li>{@code gap} - "compact", "default", or "loose"</li>
 * <li>{@code children} - list of child control descriptors</li>
 * </ul>
 */
public class ReactGridControl extends ReactControl {

	private static final String REACT_MODULE = "TLGrid";

	private static final String COLUMNS = "columns";

	private static final String MIN_COLUMN_WIDTH = "minColumnWidth";

	private static final String GAP = "gap";

	private static final String CHILDREN = "children";

	private final List<ReactControl> _children;

	/**
	 * Creates a responsive grid with auto-fit columns.
	 *
	 * @param minColumnWidth
	 *        Minimum column width (e.g. "16rem"). Columns reflow responsively.
	 * @param gap
	 *        "compact", "default", or "loose".
	 * @param children
	 *        The child controls to arrange in the grid.
	 */
	public ReactGridControl(String minColumnWidth, String gap, List<? extends ReactControl> children) {
		super(null, REACT_MODULE);
		_children = new ArrayList<>(children);
		putState(MIN_COLUMN_WIDTH, minColumnWidth);
		putState(GAP, gap);
		putState(CHILDREN, _children);
	}

	/**
	 * Creates a fixed-column grid.
	 *
	 * @param columns
	 *        The number of columns.
	 * @param gap
	 *        "compact", "default", or "loose".
	 * @param children
	 *        The child controls to arrange in the grid.
	 */
	public ReactGridControl(int columns, String gap, List<? extends ReactControl> children) {
		super(null, REACT_MODULE);
		_children = new ArrayList<>(children);
		putState(COLUMNS, Integer.valueOf(columns));
		putState(GAP, gap);
		putState(CHILDREN, _children);
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl child : _children) {
			child.cleanupTree();
		}
	}

}
