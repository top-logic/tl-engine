/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;

/**
 * Visual window chrome for modal dialogs: title bar, close button, scrollable body, footer actions,
 * and optional resize handles.
 *
 * <p>
 * This control provides the visual frame. It is typically placed as the child of a
 * {@link ReactDialogControl} which handles the overlay mechanics (backdrop, focus trap).
 * </p>
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@code title} - the window title</li>
 * <li>{@code width} - the window width (CSS value, e.g. "500px")</li>
 * <li>{@code height} - the window height (CSS value or null for auto)</li>
 * <li>{@code resizable} - whether the window can be resized by dragging</li>
 * <li>{@code child} - the body content control</li>
 * <li>{@code actions} - list of footer action controls</li>
 * </ul>
 */
public class ReactWindowControl extends ToolbarControl {

	private static final String REACT_MODULE = "TLWindow";

	private static final String TITLE = "title";

	private static final String WIDTH = "width";

	private static final String HEIGHT = "height";

	private static final String RESIZABLE = "resizable";

	private static final String CHILD = "child";

	private static final String ACTIONS = "actions";

	private ReactControl _child;

	private List<ReactControl> _actions = new ArrayList<>();

	private Runnable _closeHandler;

	/**
	 * Creates a window control.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param title
	 *        The window title.
	 * @param width
	 *        The initial window width.
	 * @param closeHandler
	 *        Called when the close button is clicked.
	 */
	public ReactWindowControl(ReactContext context, String title, DisplayDimension width, Runnable closeHandler) {
		super(context, null, REACT_MODULE);
		_closeHandler = closeHandler;
		setTitle(title);
		setWidth(width);
		setResizable(false);
		setActions(List.of());
	}

	/**
	 * Sets the window title.
	 */
	public void setTitle(String title) {
		putState(TITLE, title);
	}

	/**
	 * Sets the window width.
	 */
	public void setWidth(DisplayDimension width) {
		putState(WIDTH, width.toString());
	}

	/**
	 * Sets the window height.
	 */
	public void setHeight(DisplayDimension height) {
		putState(HEIGHT, height != null ? height.toString() : null);
	}

	/**
	 * Sets whether the window is resizable.
	 */
	public void setResizable(boolean resizable) {
		putState(RESIZABLE, resizable);
	}

	/**
	 * Sets the body content.
	 */
	public void setChild(ReactControl child) {
		_child = child;
		putState(CHILD, child);
	}

	/**
	 * Sets the footer action controls.
	 */
	public void setActions(List<? extends ReactControl> actions) {
		_actions = new ArrayList<>(actions);
		putState(ACTIONS, _actions);
	}

	@Override
	protected void cleanupChildren() {
		if (_child != null) {
			_child.cleanupTree();
		}
		for (ReactControl action : _actions) {
			action.cleanupTree();
		}
		cleanupToolbarButtons();
	}

	/**
	 * Handles the close button click.
	 */
	@ReactCommand("close")
	void handleClose() {
		_closeHandler.run();
	}

	/**
	 * Handles a resize event from the client.
	 */
	@ReactCommand("resize")
	void handleResize(Map<String, Object> args) {
		Object w = args.get("width");
		Object h = args.get("height");
		if (w != null) {
			getReactState().put(WIDTH, w.toString());
		}
		if (h != null) {
			getReactState().put(HEIGHT, h.toString());
		}
	}

}
