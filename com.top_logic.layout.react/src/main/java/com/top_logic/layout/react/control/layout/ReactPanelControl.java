/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

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
public class ReactPanelControl extends ReactControl {

	private static final String REACT_MODULE = "TLPanel";

	private static final String TITLE = "title";

	private static final String EXPANSION_STATE = "expansionState";

	private static final String SHOW_MINIMIZE = "showMinimize";

	private static final String SHOW_MAXIMIZE = "showMaximize";

	private static final String SHOW_POP_OUT = "showPopOut";

	private static final String TOOLBAR_BUTTONS = "toolbarButtons";

	private static final String CHILD = "child";

	/** Default collapsed size in pixels (toolbar header height). */
	private static final float COLLAPSED_SIZE = 36f;

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ToggleMinimizeCommand(),
		new ToggleMaximizeCommand(),
		new PopOutCommand());

	private final ReactControl _child;

	private final List<ReactControl> _toolbarButtons = new ArrayList<>();

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
	public ReactPanelControl(String title, ReactControl child,
			boolean showMinimize, boolean showMaximize, boolean showPopOut) {
		super(null, REACT_MODULE, COMMANDS);
		_child = child;

		getReactState().put(TITLE, title);
		getReactState().put(EXPANSION_STATE, _expansionState.name());
		getReactState().put(SHOW_MINIMIZE, Boolean.valueOf(showMinimize));
		getReactState().put(SHOW_MAXIMIZE, Boolean.valueOf(showMaximize));
		getReactState().put(SHOW_POP_OUT, Boolean.valueOf(showPopOut));
		getReactState().put(TOOLBAR_BUTTONS, new ArrayList<>());
		getReactState().put(CHILD, child);
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
	 * Adds a custom toolbar button.
	 */
	public void addToolbarButton(ReactControl button) {
		_toolbarButtons.add(button);
		toolbarButtonList().add(button);
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
		for (ReactControl button : _toolbarButtons) {
			button.cleanupTree();
		}
	}

	@SuppressWarnings("unchecked")
	private List<Object> toolbarButtonList() {
		return (List<Object>) getReactState().get(TOOLBAR_BUTTONS);
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
	 * Command sent by the React client when the minimize button is clicked.
	 */
	public static class ToggleMinimizeCommand extends ControlCommand {

		static final String COMMAND = "toggleMinimize";

		/** Creates a new {@link ToggleMinimizeCommand}. */
		public ToggleMinimizeCommand() {
			super(COMMAND);
		}

		@Override
		public com.top_logic.basic.util.ResKey getI18NKey() {
			return I18NConstants.REACT_PANEL_TOGGLE_MINIMIZE;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactPanelControl panel = (ReactPanelControl) control;
			boolean wasMinimized = panel._expansionState == ExpansionState.MINIMIZED;
			boolean collapsed = !wasMinimized;

			panel.setExpansionState(collapsed ? ExpansionState.MINIMIZED : ExpansionState.NORMALIZED);

			if (panel._parentSplitPanel != null) {
				panel._parentSplitPanel.childCollapsed(panel._indexInParent, collapsed, COLLAPSED_SIZE);
			}

			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Command sent by the React client when the maximize button is clicked.
	 */
	public static class ToggleMaximizeCommand extends ControlCommand {

		static final String COMMAND = "toggleMaximize";

		/** Creates a new {@link ToggleMaximizeCommand}. */
		public ToggleMaximizeCommand() {
			super(COMMAND);
		}

		@Override
		public com.top_logic.basic.util.ResKey getI18NKey() {
			return I18NConstants.REACT_PANEL_TOGGLE_MAXIMIZE;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactPanelControl panel = (ReactPanelControl) control;

			if (panel._expansionState == ExpansionState.MAXIMIZED) {
				// Normalize.
				panel.setExpansionState(ExpansionState.NORMALIZED);
				if (panel._maximizeRoot != null) {
					panel._maximizeRoot.normalize();
				}
			} else {
				// Maximize.
				panel.setExpansionState(ExpansionState.MAXIMIZED);
				if (panel._maximizeRoot != null) {
					panel._maximizeRoot.maximize(panel);
				}
			}

			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Command sent by the React client when the pop-out button is clicked.
	 */
	public static class PopOutCommand extends ControlCommand {

		static final String COMMAND = "popOut";

		/** Creates a new {@link PopOutCommand}. */
		public PopOutCommand() {
			super(COMMAND);
		}

		@Override
		public com.top_logic.basic.util.ResKey getI18NKey() {
			return I18NConstants.REACT_PANEL_POP_OUT;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactPanelControl panel = (ReactPanelControl) control;
			panel.setExpansionState(ExpansionState.HIDDEN);
			// External window handling is left to the application layer.
			return HandlerResult.DEFAULT_RESULT;
		}
	}
}
