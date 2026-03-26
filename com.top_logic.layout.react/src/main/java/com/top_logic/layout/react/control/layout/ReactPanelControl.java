/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;

/**
 * A {@link ReactControl} that wraps a single child with a titled toolbar header.
 *
 * <p>
 * The toolbar hosts action buttons: minimize, maximize, pop-out, and any custom buttons.
 * </p>
 *
 * <p>
 * The React component {@code TLPanel} receives the following state:
 * </p>
 * <ul>
 * <li>{@code title} - the panel title</li>
 * <li>{@code expansionState} - one of "NORMALIZED", "MINIMIZED", "MAXIMIZED", "HIDDEN"</li>
 * <li>{@code showMinimize} - whether the minimize button is shown</li>
 * <li>{@code showMaximize} - whether the maximize button is shown</li>
 * <li>{@code showPopOut} - whether the pop-out button is shown</li>
 * <li>{@code toolbarButtons} - list of child control descriptors for custom toolbar buttons</li>
 * <li>{@code child} - the content child control descriptor</li>
 * </ul>
 */
public class ReactPanelControl extends ToolbarControl {

	private static final String REACT_MODULE = "TLPanel";

	/** @see #setTitle(String) */
	private static final String TITLE = "title";

	/** @see #setExpansionState(ExpansionState) */
	private static final String EXPANSION_STATE = "expansionState";

	/** @see #ReactPanelControl(ReactContext, String, ReactControl, boolean, boolean, boolean) */
	private static final String SHOW_MINIMIZE = "showMinimize";

	/** @see #ReactPanelControl(ReactContext, String, ReactControl, boolean, boolean, boolean) */
	private static final String SHOW_MAXIMIZE = "showMaximize";

	/** @see #ReactPanelControl(ReactContext, String, ReactControl, boolean, boolean, boolean) */
	private static final String SHOW_POP_OUT = "showPopOut";

	/** @see #ReactPanelControl(ReactContext, String, ReactControl, boolean, boolean, boolean) */
	private static final String CHILD = "child";

	/** Default collapsed size in pixels (toolbar header height). */
	private static final float COLLAPSED_SIZE = 36f;

	private final ReactControl _child;

	private ExpansionState _expansionState = ExpansionState.NORMALIZED;

	private ReactSplitPanelControl _parentSplitPanel;

	private int _indexInParent = -1;

	private ReactMaximizeRootControl _maximizeRoot;

	/**
	 * Creates a new {@link ReactPanelControl}.
	 *
	 * @param title
	 *        The panel title.
	 * @param child
	 *        The content child control.
	 * @param showMinimize
	 *        Whether the minimize button is shown.
	 * @param showMaximize
	 *        Whether the maximize button is shown.
	 * @param showPopOut
	 *        Whether the pop-out button is shown.
	 */
	public ReactPanelControl(ReactContext context, String title, ReactControl child,
			boolean showMinimize, boolean showMaximize, boolean showPopOut) {
		super(context, null, REACT_MODULE);
		_child = child;

		setTitle(title);
		setExpansionState(_expansionState);
		putState(SHOW_MINIMIZE, Boolean.valueOf(showMinimize));
		putState(SHOW_MAXIMIZE, Boolean.valueOf(showMaximize));
		putState(SHOW_POP_OUT, Boolean.valueOf(showPopOut));
		putState(CHILD, child);
	}

	/**
	 * Sets the expansion state.
	 */
	public void setExpansionState(ExpansionState state) {
		_expansionState = state;
		putState(EXPANSION_STATE, state.name());
	}

	/**
	 * Sets the panel title.
	 */
	public void setTitle(String title) {
		putState(TITLE, title);
	}

	/**
	 * Sets the parent split panel reference for collapse notification.
	 */
	void setParentSplitPanel(ReactSplitPanelControl parent, int indexInParent) {
		_parentSplitPanel = parent;
		_indexInParent = indexInParent;
	}

	/**
	 * Sets the maximize root for maximize delegation.
	 */
	public void setMaximizeRoot(ReactMaximizeRootControl root) {
		_maximizeRoot = root;
	}

	@Override
	protected void cleanupChildren() {
		_child.cleanupTree();
		cleanupToolbarButtons();
	}

	/**
	 * Expansion states for a panel.
	 */
	public enum ExpansionState {

		/** Normal display. */
		NORMALIZED,

		/** Collapsed to just the toolbar header. */
		MINIMIZED,

		/** Filling the maximize root area. */
		MAXIMIZED,

		/** Hidden (e.g. popped out to external window). */
		HIDDEN
	}

	/**
	 * Handles the toggleMinimize command sent by the React client when the minimize button is
	 * clicked.
	 */
	@ReactCommand("toggleMinimize")
	void handleToggleMinimize() {
		boolean wasMinimized = _expansionState == ExpansionState.MINIMIZED;
		boolean collapsed = !wasMinimized;

		if (collapsed && _parentSplitPanel != null
				&& !_parentSplitPanel.canChildCollapse(_indexInParent)) {
			return;
		}

		setExpansionState(collapsed ? ExpansionState.MINIMIZED : ExpansionState.NORMALIZED);

		if (_parentSplitPanel != null) {
			_parentSplitPanel.childCollapsed(_indexInParent, collapsed, COLLAPSED_SIZE);
		}
	}

	/**
	 * Handles the toggleMaximize command sent by the React client when the maximize button is
	 * clicked.
	 */
	@ReactCommand("toggleMaximize")
	void handleToggleMaximize() {
		if (_expansionState == ExpansionState.MAXIMIZED) {
			setExpansionState(ExpansionState.NORMALIZED);
			if (_maximizeRoot != null) {
				_maximizeRoot.normalize();
			}
		} else {
			setExpansionState(ExpansionState.MAXIMIZED);
			if (_maximizeRoot != null) {
				_maximizeRoot.maximize(this);
			}
		}
	}

	/**
	 * Handles the popOut command sent by the React client when the pop-out button is clicked.
	 */
	@ReactCommand("popOut")
	void handlePopOut() {
		setExpansionState(ExpansionState.HIDDEN);
	}
}
