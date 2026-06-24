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
 * <li>{@code fill} - whether the panel fills its container's bounded height instead of growing
 * with its content</li>
 * <li>{@code toolbar} - child control descriptor for the toolbar (optional)</li>
 * <li>{@code buttonBar} - child control descriptor for the button bar (optional)</li>
 * <li>{@code child} - the content child control descriptor</li>
 * </ul>
 */
public class ReactPanelControl extends ToolbarControl {

	private static final String REACT_MODULE = "TLPanel";

	/** @see #setTitle(String) */
	private static final String TITLE = "title";

	/** @see #setExpansionState(ExpansionState) */
	private static final String EXPANSION_STATE = "expansionState";

	/** @see #ReactPanelControl(ReactContext, String, ReactControl, ReactToolbarControl, ReactToolbarControl, boolean, boolean, boolean) */
	private static final String SHOW_MINIMIZE = "showMinimize";

	/** @see #ReactPanelControl(ReactContext, String, ReactControl, ReactToolbarControl, ReactToolbarControl, boolean, boolean, boolean) */
	private static final String SHOW_MAXIMIZE = "showMaximize";

	/** @see #ReactPanelControl(ReactContext, String, ReactControl, ReactToolbarControl, ReactToolbarControl, boolean, boolean, boolean) */
	private static final String SHOW_POP_OUT = "showPopOut";

	/** @see #getToolbar() */
	private static final String TOOLBAR = "toolbar";

	/** @see #getButtonBar() */
	private static final String BUTTON_BAR = "buttonBar";

	/** @see #ReactPanelControl(ReactContext, String, ReactControl, ReactToolbarControl, ReactToolbarControl, boolean, boolean, boolean) */
	private static final String CHILD = "child";

	/** @see #setFill(boolean) */
	private static final String FILL = "fill";

	/** Default collapsed size in pixels (toolbar header height). */
	private static final float COLLAPSED_SIZE = 36f;

	private final ReactControl _child;

	private final ReactToolbarControl _toolbar;

	private final ReactToolbarControl _buttonBar;

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
	 * @param toolbar
	 *        The toolbar control, or {@code null}.
	 * @param buttonBar
	 *        The button-bar control, or {@code null}.
	 * @param showMinimize
	 *        Whether the minimize button is shown.
	 * @param showMaximize
	 *        Whether the maximize button is shown.
	 * @param showPopOut
	 *        Whether the pop-out button is shown.
	 */
	public ReactPanelControl(ReactContext context, String title, ReactControl child,
			ReactToolbarControl toolbar, ReactToolbarControl buttonBar,
			boolean showMinimize, boolean showMaximize, boolean showPopOut) {
		super(context, null, REACT_MODULE);
		_child = child;
		_toolbar = toolbar;
		_buttonBar = buttonBar;

		setTitle(title);
		setExpansionState(_expansionState);
		putState(SHOW_MINIMIZE, Boolean.valueOf(showMinimize));
		putState(SHOW_MAXIMIZE, Boolean.valueOf(showMaximize));
		putState(SHOW_POP_OUT, Boolean.valueOf(showPopOut));
		if (toolbar != null) {
			putState(TOOLBAR, toolbar);
		}
		if (buttonBar != null) {
			putState(BUTTON_BAR, buttonBar);
		}
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
	 * Whether the panel fills the bounded height of its container instead of growing with its
	 * content.
	 *
	 * <p>
	 * Set this for a panel whose content scrolls internally - in particular a virtualized
	 * {@link com.top_logic.layout.react.control.table.TableViewControl}, which only bounds its
	 * scroll viewport when its container has a definite height.
	 * </p>
	 */
	public void setFill(boolean fill) {
		putState(FILL, Boolean.valueOf(fill));
	}

	/**
	 * Returns the toolbar control, or {@code null}.
	 */
	public ReactToolbarControl getToolbar() {
		return _toolbar;
	}

	/**
	 * Returns the button-bar control, or {@code null}.
	 */
	public ReactToolbarControl getButtonBar() {
		return _buttonBar;
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
		if (_toolbar != null) {
			_toolbar.cleanupTree();
		}
		if (_buttonBar != null) {
			_buttonBar.cleanupTree();
		}
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
