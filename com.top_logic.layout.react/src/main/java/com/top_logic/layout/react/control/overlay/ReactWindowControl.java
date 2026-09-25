/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.control.layout.ToolbarGroupDisplay;
import com.top_logic.layout.react.control.layout.ToolbarOverflow;
import com.top_logic.layout.react.state.WindowState;
import com.top_logic.layout.table.ConfigKey;

/**
 * Visual window chrome for modal dialogs: title bar, close button, scrollable body, footer button
 * bar, and optional resize handles.
 *
 * <p>
 * This control provides the visual frame. It is typically placed as the child of a
 * {@link ReactDialogControl} which handles the overlay mechanics (backdrop, focus trap).
 * </p>
 *
 * <p>
 * The footer is a single collapsing toolbar: the {@link #setActions(List) actions} of the window
 * lead it as its {@link ReactToolbarControl#setPinnedGroup(String, ToolbarGroupDisplay, List)
 * pinned group}, followed by the groups of the {@link #setButtonBar(ReactToolbarControl) button
 * bar}. The footer therefore reads from the dismissing command towards the primary one, and
 * {@link ToolbarOverflow#LEADING collapses from its leading end}, which keeps the primary command
 * visible longest - the window can still be dismissed with Escape and the title bar's close
 * button.
 * </p>
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@link WindowState#TITLE__PROP} - the window title</li>
 * <li>{@link WindowState#WIDTH__PROP} - the window width (CSS value, e.g. "500px")</li>
 * <li>{@link WindowState#HEIGHT__PROP} - the window height (CSS value or null for auto)</li>
 * <li>{@link WindowState#RESIZABLE__PROP} - whether the window can be resized by dragging</li>
 * <li>{@link WindowState#CLOSABLE__PROP} - whether the close button is enabled and Escape closes
 * the window</li>
 * <li>{@link WindowState#CHILD__PROP} - the body content control</li>
 * <li>{@link WindowState#TOOLBAR__PROP} - the title bar's toolbar</li>
 * <li>{@link WindowState#FOOTER__PROP} - the footer's toolbar</li>
 * </ul>
 */
public class ReactWindowControl extends ToolbarControl {

	private static final String REACT_MODULE = "TLWindow";

	/**
	 * Clique name of the group the {@link #setActions(List) actions} form at the leading end of
	 * the {@link WindowState#FOOTER__PROP footer}.
	 */
	public static final String ACTIONS_CLIQUE = "actions";

	private static final String CONFIG_KEY_SIZE_SUFFIX = "reactDialogSize";

	/** The {@link ReactCommandHandler} that records a window resize. */
	public static final String RESIZE_COMMAND = "resize";

	/** The {@link ReactCommandHandler} that closes this window. */
	public static final String CLOSE_COMMAND = "close";

	private ReactControl _child;

	private List<ReactControl> _actions = new ArrayList<>();

	private ReactToolbarControl _toolbar;

	/**
	 * The toolbar shown in the footer, or {@code null} while the window has neither actions nor a
	 * button bar.
	 */
	private ReactToolbarControl _footer;

	private Runnable _closeHandler;

	private ConfigKey _configKey;

	private boolean _closable = true;

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
		putState(WindowState.CLOSABLE__PROP, _closable);
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
		putState(WindowState.TITLE__PROP, title);
	}

	/**
	 * Sets the window width.
	 */
	public void setWidth(DisplayDimension width) {
		putState(WindowState.WIDTH__PROP, width.toString());
	}

	/**
	 * Sets the window height.
	 */
	public void setHeight(DisplayDimension height) {
		putState(WindowState.HEIGHT__PROP, height != null ? height.toString() : null);
	}

	/**
	 * Sets whether the window is resizable.
	 */
	public void setResizable(boolean resizable) {
		putState(WindowState.RESIZABLE__PROP, resizable);
	}

	/**
	 * Sets the body content.
	 */
	public void setChild(ReactControl child) {
		_child = child;
		putState(WindowState.CHILD__PROP, child);
	}

	/**
	 * Sets the controls that lead the {@link WindowState#FOOTER__PROP footer}, such as the command
	 * dismissing the window.
	 *
	 * <p>
	 * They form the {@link ReactToolbarControl#setPinnedGroup(String, ToolbarGroupDisplay, List)
	 * pinned group} of the footer toolbar, so a rebuild of the
	 * {@link #setButtonBar(ReactToolbarControl) button bar} keeps them.
	 * </p>
	 *
	 * @param actions
	 *        The controls to show, in display order.
	 */
	public void setActions(List<? extends ReactControl> actions) {
		_actions = new ArrayList<>(actions);
		if (_footer == null) {
			if (_actions.isEmpty()) {
				return;
			}
			setFooter(new ReactToolbarControl(getReactContext()));
		}
		_footer.setPinnedGroup(ACTIONS_CLIQUE, ToolbarGroupDisplay.INLINE, _actions);
	}

	/**
	 * Sets the clique-grouped title-bar toolbar, or {@code null} to remove it.
	 */
	public void setToolbar(ReactToolbarControl toolbar) {
		_toolbar = toolbar;
		if (toolbar != null) {
			putState(WindowState.TOOLBAR__PROP, toolbar);
		}
	}

	/**
	 * Sets the clique-grouped toolbar carrying the window's button-bar commands.
	 *
	 * <p>
	 * It becomes the window's {@link WindowState#FOOTER__PROP footer}, taking over the
	 * {@link #setActions(List) actions} as its pinned group, so the footer stays a single
	 * collapsing toolbar however the two are set.
	 * </p>
	 *
	 * @param buttonBar
	 *        The toolbar to display, or {@code null} to leave the footer to the actions alone.
	 */
	public void setButtonBar(ReactToolbarControl buttonBar) {
		if (buttonBar == null) {
			return;
		}
		ReactToolbarControl previous = _footer;
		setFooter(buttonBar);
		if (!_actions.isEmpty()) {
			buttonBar.setPinnedGroup(ACTIONS_CLIQUE, ToolbarGroupDisplay.INLINE, _actions);
		}
		if (previous != null && previous != buttonBar) {
			// The actions are shown by the button bar now: release them from the toolbar that
			// was built for them alone before that one is dropped, so disposing it leaves the
			// action controls alone.
			previous.setPinnedGroup(ACTIONS_CLIQUE, ToolbarGroupDisplay.INLINE, List.of());
			previous.cleanupTree();
		}
	}

	/**
	 * Shows the given toolbar in the footer, collapsing from its leading end.
	 */
	private void setFooter(ReactToolbarControl footer) {
		_footer = footer;
		footer.setOverflow(ToolbarOverflow.LEADING);
		putState(WindowState.FOOTER__PROP, footer);
	}

	/**
	 * Whether this window can be closed.
	 *
	 * @see #setClosable(boolean)
	 */
	public boolean isClosable() {
		return _closable;
	}

	/**
	 * Sets whether this window can be closed.
	 *
	 * <p>
	 * While the window is not closable, the client shows its close button disabled and leaves
	 * Escape to the enclosing scope, and {@link #CLOSE_COMMAND} is ignored. A window is closable
	 * unless marked otherwise.
	 * </p>
	 *
	 * @param closable
	 *        Whether the window may be closed.
	 */
	public void setClosable(boolean closable) {
		if (closable == _closable) {
			return;
		}
		_closable = closable;
		putState(WindowState.CLOSABLE__PROP, closable);
	}

	/**
	 * Handles the close button click.
	 *
	 * <p>
	 * The command is ignored while the window is not {@link #isClosable() closable}.
	 * </p>
	 */
	@ReactCommandHandler(CLOSE_COMMAND)
	void handleClose() {
		if (!_closable) {
			return;
		}
		_closeHandler.run();
	}

	/**
	 * Handles a resize event from the client.
	 */
	@ReactCommandHandler(RESIZE_COMMAND)
	void handleResize(ResizeArguments args) {
		Integer w = args.getWidth();
		Integer h = args.getHeight();
		// The client performed the resize itself; no echo needed.
		updateStateSilently(() -> {
			if (w != null) {
				putState(WindowState.WIDTH__PROP, w + "px");
			}
			if (h != null) {
				putState(WindowState.HEIGHT__PROP, h + "px");
			}
		});
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
		putState(WindowState.WIDTH__PROP, width + "px");
		putState(WindowState.MIN_HEIGHT__PROP, height + "px");
	}

	private void saveCustomizedSize(Integer widthValue, Integer heightValue) {
		String key = _configKey.get();
		if (key == null) {
			return;
		}
		int width = widthValue != null ? widthValue.intValue() : -1;
		int height = heightValue != null ? heightValue.intValue() : -1;
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

}
