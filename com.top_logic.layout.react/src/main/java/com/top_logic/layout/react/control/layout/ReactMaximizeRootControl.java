/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.Collections;

import com.top_logic.layout.react.ReactControl;

/**
 * A {@link ReactControl} that defines the boundary for panel maximization.
 *
 * <p>
 * Wraps a single child (the normal content tree). When a descendant {@link ReactPanelControl}
 * requests maximize, this root sets a CSS class so that the maximized panel stretches to fill this
 * container via absolute positioning.
 * </p>
 *
 * <p>
 * The React component {@code TLMaximizeRoot} receives the following state:
 * </p>
 * <ul>
 * <li>{@code child} - the content child control descriptor</li>
 * <li>{@code maximized} - whether a descendant panel is currently maximized</li>
 * </ul>
 */
public class ReactMaximizeRootControl extends ReactControl {

	private static final String REACT_MODULE = "TLMaximizeRoot";

	private static final String CHILD = "child";

	private static final String MAXIMIZED = "maximized";

	private final ReactControl _child;

	private ReactPanelControl _maximizedPanel;

	/**
	 * Creates a new {@link ReactMaximizeRootControl}.
	 *
	 * @param child
	 *        The content child control.
	 */
	public ReactMaximizeRootControl(ReactControl child) {
		super(null, REACT_MODULE, Collections.emptyMap());
		_child = child;

		getReactState().put(CHILD, child);
		getReactState().put(MAXIMIZED, Boolean.FALSE);
	}

	/**
	 * Called by a descendant {@link ReactPanelControl} to maximize it.
	 *
	 * @param panel
	 *        The panel to maximize.
	 */
	public void maximize(ReactPanelControl panel) {
		_maximizedPanel = panel;
		putState(MAXIMIZED, Boolean.TRUE);
	}

	/**
	 * Called to restore normal display after maximization.
	 */
	public void normalize() {
		_maximizedPanel = null;
		putState(MAXIMIZED, Boolean.FALSE);
	}

	/**
	 * The currently maximized panel, or {@code null} if none is maximized.
	 */
	public ReactPanelControl getMaximizedPanel() {
		return _maximizedPanel;
	}

	@Override
	protected void cleanupChildren() {
		_child.cleanupTree();
	}
}
