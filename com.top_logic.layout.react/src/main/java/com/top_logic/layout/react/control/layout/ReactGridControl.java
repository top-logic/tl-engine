/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;

/**
 * A {@link ReactControl} that renders a CSS Grid container via the {@code TLGrid} React component.
 */
public class ReactGridControl extends ReactControl {

	private static final String REACT_MODULE = "TLGrid";

	private static final String COLUMNS = "columns";

	private static final String MIN_COLUMN_WIDTH = "minColumnWidth";

	/** @see #ReactGridControl(ReactContext, String, Integer, StackGap, List) */
	private static final String MAX_COLUMNS = "maxColumns";

	private static final String GAP = "gap";

	/** @see #setItemClass(String) */
	private static final String ITEM_CLASS = "itemClass";

	private static final String CHILDREN = "children";

	private final List<ReactControl> _children;

	/**
	 * Creates a responsive grid with auto-fit columns.
	 *
	 * @param minColumnWidth
	 *        Minimum column width (e.g. "16rem"). Columns reflow responsively.
	 * @param maxColumns
	 *        Largest number of columns to place, or {@code null} to place as many as fit. A grid
	 *        bounded this way still drops columns as its container narrows, it just never grows
	 *        beyond the given number however wide the container gets.
	 * @param gap
	 *        The gap between grid items.
	 * @param children
	 *        The child controls to arrange in the grid.
	 */
	public ReactGridControl(ReactContext context, String minColumnWidth, Integer maxColumns, StackGap gap,
			List<? extends ReactControl> children) {
		super(context, null, REACT_MODULE);
		_children = new ArrayList<>(children);
		putState(MIN_COLUMN_WIDTH, minColumnWidth);
		putState(MAX_COLUMNS, maxColumns);
		putState(GAP, gap.getExternalName());
		putState(CHILDREN, _children);
	}

	/**
	 * Creates a fixed-column grid.
	 *
	 * @param columns
	 *        The number of columns.
	 * @param gap
	 *        The gap between grid items.
	 * @param children
	 *        The child controls to arrange in the grid.
	 */
	public ReactGridControl(ReactContext context, int columns, StackGap gap,
			List<? extends ReactControl> children) {
		super(context, null, REACT_MODULE);
		_children = new ArrayList<>(children);
		putState(COLUMNS, Integer.valueOf(columns));
		putState(GAP, gap.getExternalName());
		putState(CHILDREN, _children);
	}

	/**
	 * Wraps each child in an element of the given CSS class, carrying the 0-based position of the
	 * child as the CSS custom property {@code --tl-item-index}.
	 *
	 * <p>
	 * A stylesheet composes a per-item value from that position, a staggered entrance animation
	 * being the case it is meant for. Without a class, the children are placed in the grid
	 * directly.
	 * </p>
	 *
	 * @param itemClass
	 *        The CSS class of the wrapper around each child, or {@code null} for no wrapper.
	 */
	public void setItemClass(String itemClass) {
		putState(ITEM_CLASS, itemClass);
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return Set.of(GAP, MIN_COLUMN_WIDTH, MAX_COLUMNS, ITEM_CLASS);
	}
}
