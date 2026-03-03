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
 * A {@link ReactControl} that renders a flexbox container via the {@code TLStack} React component.
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@code direction} - "column" or "row"</li>
 * <li>{@code gap} - "compact", "default", or "loose"</li>
 * <li>{@code align} - "start", "center", "end", or "stretch"</li>
 * <li>{@code wrap} - boolean</li>
 * <li>{@code children} - list of child control descriptors</li>
 * </ul>
 */
public class ReactStackControl extends ReactControl {

	private static final String REACT_MODULE = "TLStack";

	private static final String DIRECTION = "direction";

	private static final String GAP = "gap";

	private static final String ALIGN = "align";

	private static final String WRAP = "wrap";

	private static final String CHILDREN = "children";

	private final List<ReactControl> _children;

	/**
	 * Creates a vertical stack with default gap.
	 *
	 * @param children
	 *        The child controls to arrange.
	 */
	public ReactStackControl(List<? extends ReactControl> children) {
		this("column", "default", "stretch", false, children);
	}

	/**
	 * Creates a stack with full configuration.
	 *
	 * @param direction
	 *        "column" or "row".
	 * @param gap
	 *        "compact", "default", or "loose".
	 * @param align
	 *        "start", "center", "end", or "stretch".
	 * @param wrap
	 *        Whether to wrap children.
	 * @param children
	 *        The child controls to arrange.
	 */
	public ReactStackControl(String direction, String gap, String align, boolean wrap,
			List<? extends ReactControl> children) {
		super(null, REACT_MODULE);
		_children = new ArrayList<>(children);
		putState(DIRECTION, direction);
		putState(GAP, gap);
		putState(ALIGN, align);
		putState(WRAP, Boolean.valueOf(wrap));
		putState(CHILDREN, _children);
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl child : _children) {
			child.cleanupTree();
		}
	}

}
