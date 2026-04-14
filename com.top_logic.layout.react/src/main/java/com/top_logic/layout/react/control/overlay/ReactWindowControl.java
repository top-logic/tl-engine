/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;
import com.top_logic.layout.table.ConfigKey;

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

	private static final String CONFIG_KEY_SIZE_SUFFIX = "reactDialogSize";

	private static final String MIN_HEIGHT = "minHeight";

	private ReactControl _child;

	private List<ReactControl> _actions = new ArrayList<>();

	private Runnable _closeHandler;

	private ConfigKey _configKey;

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
		_configKey = ConfigKey.none();
		setTitle(title);
		setWidth(width);
		setResizable(true);
		setActions(List.of());
	}

	/**
	 * Creates a window control with size persistence.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param title
	 *        The window title.
	 * @param width
	 *        The default window width.
	 * @param closeHandler
	 *        Called when the close button is clicked.
	 * @param configKey
	 *        Key for storing personalized size in {@link PersonalConfiguration}.
	 */
	public ReactWindowControl(ReactContext context, String title, DisplayDimension width,
			Runnable closeHandler, ConfigKey configKey) {
		this(context, title, width, closeHandler);
		_configKey = ConfigKey.derived(configKey, CONFIG_KEY_SIZE_SUFFIX);
		applyCustomizedSize();
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
	protected void propagateAttach() {
		super.propagateAttach();
		if (_child != null) {
			_child.attach();
		}
		for (ReactControl action : _actions) {
			action.attach();
		}
	}

	@Override
	protected void propagateDetach() {
		super.propagateDetach();
		if (_child != null) {
			_child.detach();
		}
		for (ReactControl action : _actions) {
			action.detach();
		}
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
			putStateSilent(WIDTH, w.toString());
		}
		if (h != null) {
			putStateSilent(HEIGHT, h.toString());
		}
		saveCustomizedSize(w, h);
	}

	private void applyCustomizedSize() {
		String key = _configKey.get();
		if (key == null) {
			return;
		}
		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		if (config == null) {
			return;
		}
		Object jsonValue = config.getJSONValue(key);
		if (!(jsonValue instanceof List<?> list) || list.size() != 2) {
			return;
		}
		int width = ((Number) list.get(0)).intValue();
		int height = ((Number) list.get(1)).intValue();
		putState(WIDTH, width + "px");
		putState(MIN_HEIGHT, height + "px");
	}

	private void saveCustomizedSize(Object widthValue, Object heightValue) {
		String key = _configKey.get();
		if (key == null) {
			return;
		}
		int width = parsePx(widthValue, -1);
		int height = parsePx(heightValue, -1);
		if (width < 0 && height < 0) {
			return;
		}

		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		if (config == null) {
			return;
		}

		// Merge with existing values if only one dimension changed.
		Object existing = config.getJSONValue(key);
		if (existing instanceof List<?> list && list.size() == 2) {
			if (width < 0) width = ((Number) list.get(0)).intValue();
			if (height < 0) height = ((Number) list.get(1)).intValue();
		}

		if (width > 0 && height > 0) {
			List<Integer> parameters = new ArrayList<>();
			parameters.add(width);
			parameters.add(height);
			config.setJSONValue(key, parameters);
		}
	}

	private static int parsePx(Object value, int fallback) {
		if (value == null) return fallback;
		String s = value.toString().trim();
		if (s.endsWith("px")) {
			s = s.substring(0, s.length() - 2);
		}
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

}
