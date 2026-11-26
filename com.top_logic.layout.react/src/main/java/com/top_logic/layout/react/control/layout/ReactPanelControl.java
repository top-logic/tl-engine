/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
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
 * <li>{@code explanation} - the explanation of the title (optional): the tooltip of the title, or
 * of an info icon beside the content of a panel without title</li>
 * <li>{@code explanationHelp} - whether the explanation is offered as help text, which an icon
 * shows and hides (edit mode)</li>
 * <li>{@code titleContent} - child control descriptor rendered in the title area (optional)</li>
 * <li>{@code expansionState} - one of "NORMALIZED", "MINIMIZED", "MAXIMIZED", "HIDDEN"</li>
 * <li>{@code showMinimize} - whether the minimize button is shown</li>
 * <li>{@code showMaximize} - whether the maximize button is shown</li>
 * <li>{@code showPopOut} - whether the pop-out button is shown</li>
 * <li>{@code fill} - whether the panel fills its container's bounded height instead of growing
 * with its content</li>
 * <li>{@code width} - a width of the panel's own (optional), instead of the width its container
 * offers</li>
 * <li>{@link #TOOLBAR} - child control descriptor for the toolbar, while it holds a command
 * (optional)</li>
 * <li>{@link #BUTTON_BAR} - child control descriptor for the button bar, while it holds a command
 * (optional)</li>
 * <li>{@code child} - the content child control descriptor</li>
 * </ul>
 */
public class ReactPanelControl extends ToolbarControl implements CollapsibleChild {

	private static final String REACT_MODULE = "TLPanel";

	/** @see #setTitle(String) */
	private static final String TITLE = "title";

	/**
	 * State of the explanation of what the panel shows, e.g. the description of the attribute whose
	 * values a table lists; absent for none.
	 *
	 * <p>
	 * A panel with a {@link #setTitle(String) title} offers the explanation as tooltip of its
	 * title, a panel without title through an info icon beside its content, whose tooltip it is.
	 * </p>
	 */
	public static final String EXPLANATION = "explanation";

	/**
	 * State whether the {@link #EXPLANATION} is offered as help text as well, e.g. while the
	 * content is edited: an icon beside the title - or the info icon - shows and hides it below the
	 * head of the panel.
	 */
	public static final String EXPLANATION_HELP = "explanationHelp";

	/** @see #setExpansionState(ExpansionState) */
	private static final String EXPANSION_STATE = "expansionState";

	/** @see #ReactPanelControl(ReactContext, String, ReactControl, ReactToolbarControl, ReactToolbarControl, boolean, boolean, boolean) */
	private static final String SHOW_MINIMIZE = "showMinimize";

	/** @see #ReactPanelControl(ReactContext, String, ReactControl, ReactToolbarControl, ReactToolbarControl, boolean, boolean, boolean) */
	private static final String SHOW_MAXIMIZE = "showMaximize";

	/** @see #ReactPanelControl(ReactContext, String, ReactControl, ReactToolbarControl, ReactToolbarControl, boolean, boolean, boolean) */
	private static final String SHOW_POP_OUT = "showPopOut";

	/**
	 * State of the {@link #getToolbar() toolbar}, present only while the toolbar is not
	 * {@link ReactToolbarControl#isEmpty() empty}.
	 */
	public static final String TOOLBAR = "toolbar";

	/**
	 * State of the {@link #getButtonBar() button bar}, present only while the button bar is not
	 * {@link ReactToolbarControl#isEmpty() empty}.
	 */
	public static final String BUTTON_BAR = "buttonBar";

	/** @see #ReactPanelControl(ReactContext, String, ReactControl, ReactToolbarControl, ReactToolbarControl, boolean, boolean, boolean) */
	private static final String CHILD = "child";

	/** @see #setFill(Boolean) */
	public static final String FILL = "fill";

	/** @see #setWidth(String) */
	private static final String WIDTH = "width";

	/** @see #setTitleContent(ReactControl) */
	private static final String TITLE_CONTENT = "titleContent";

	/** @see #setHoverActions(boolean) */
	private static final String HOVER_ACTIONS = "hoverActions";

	/** @see #setAppearance(PanelAppearance) */
	private static final String APPEARANCE = "appearance";

	/**
	 * Visual variants of a panel.
	 */
	public enum PanelAppearance implements ExternallyNamed {

		/** Flush panel filling its container. */
		DEFAULT("default"),

		/** Bordered, rounded card with compact content insets, e.g. one entry of an item list. */
		CARD("card");

		private final String _externalName;

		PanelAppearance(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/** Default collapsed size in pixels (toolbar header height). */
	private static final float COLLAPSED_SIZE = 36f;

	private final ReactControl _child;

	private ReactControl _titleContent;

	private final ReactToolbarControl _toolbar;

	private final ReactToolbarControl _buttonBar;

	private ExpansionState _expansionState = ExpansionState.NORMALIZED;

	private CollapseContainer _container;

	private ReactMaximizeRootControl _maximizeRoot;

	/**
	 * Publishes the {@link #getToolbar() toolbar} or {@link #getButtonBar() button bar} while it
	 * holds a command, so that a panel with nothing to show in its header draws none.
	 */
	private final ReactToolbarControl.EmptyListener _barListener = (bar, wasEmpty, empty) -> publishBar(bar);

	/**
	 * Creates a new {@link ReactPanelControl}.
	 *
	 * @param title
	 *        The panel title.
	 * @param child
	 *        The content child control.
	 * @param toolbar
	 *        The toolbar control, or {@code null}. It is sent to the client only while it holds a
	 *        command, but belongs to the panel either way.
	 * @param buttonBar
	 *        The button-bar control, or {@code null}. It is sent to the client only while it holds
	 *        a command, but belongs to the panel either way.
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
			toolbar.addListener(ReactToolbarControl.EMPTY_PROPERTY, _barListener);
			publishBar(toolbar);
		}
		if (buttonBar != null) {
			buttonBar.addListener(ReactToolbarControl.EMPTY_PROPERTY, _barListener);
			publishBar(buttonBar);
		}
		putState(CHILD, child);
	}

	/**
	 * Puts the given bar into the state while it holds a command and removes it from the state
	 * while it is empty.
	 *
	 * <p>
	 * A bar outside the state is not displayed, so it is detached while it is empty; it is attached
	 * again together with the panel's state once it holds a command.
	 * </p>
	 */
	private void publishBar(ReactToolbarControl bar) {
		String key = bar == _toolbar ? TOOLBAR : BUTTON_BAR;
		if (bar.isEmpty()) {
			putState(key, null);
			bar.detach();
		} else {
			putState(key, bar);
			if (isAttached()) {
				bar.attach();
			}
		}
	}

	/**
	 * Disposes the {@link #getToolbar() toolbar} and the {@link #getButtonBar() button bar} as
	 * well, since the state holds them only while they hold a command.
	 */
	@Override
	protected void cleanupChildren() {
		super.cleanupChildren();
		if (_toolbar != null) {
			_toolbar.cleanupTree();
		}
		if (_buttonBar != null) {
			_buttonBar.cleanupTree();
		}
	}

	@Override
	protected void onCleanup() {
		super.onCleanup();
		if (_toolbar != null) {
			_toolbar.removeListener(ReactToolbarControl.EMPTY_PROPERTY, _barListener);
		}
		if (_buttonBar != null) {
			_buttonBar.removeListener(ReactToolbarControl.EMPTY_PROPERTY, _barListener);
		}
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
	 * When enabled, the header's toolbar buttons stay hidden until the pointer hovers over the
	 * panel or a button receives keyboard focus.
	 */
	public void setHoverActions(boolean hoverActions) {
		putState(HOVER_ACTIONS, Boolean.valueOf(hoverActions));
	}

	/**
	 * Sets the panel's visual variant.
	 */
	public void setAppearance(PanelAppearance appearance) {
		putState(APPEARANCE, appearance.getExternalName());
	}

	/**
	 * Sets a control rendered in the header's title area (after an optional {@link #setTitle(String)
	 * title text}), e.g. an avatar byline.
	 *
	 * @param titleContent
	 *        The title content control, or {@code null} for none.
	 */
	public void setTitleContent(ReactControl titleContent) {
		if (_titleContent != null) {
			_titleContent.cleanupTree();
		}
		_titleContent = titleContent;
		putState(TITLE_CONTENT, titleContent);
	}

	/**
	 * Whether the panel fills the bounded height of its container instead of growing with its
	 * content.
	 *
	 * <p>
	 * {@code null} (the default) lets the panel follow its content: it fills while its content
	 * fills - in particular a virtualized
	 * {@link com.top_logic.layout.react.control.table.TableViewControl}, which only bounds its
	 * scroll viewport when its container has a definite height. {@link Boolean#TRUE} fills even
	 * around content of its own size, {@link Boolean#FALSE} does not pass the fill request of its
	 * content on to its container.
	 * </p>
	 */
	public void setFill(Boolean fill) {
		putState(FILL, fill);
	}

	/**
	 * Gives the panel a width of its own, instead of the width its container offers.
	 *
	 * <p>
	 * A panel of its own width no longer stretches across its container, so the alignment of the
	 * container places it - a stack aligning its children to the center, for instance, centers it.
	 * The width is a preference, not a minimum: the panel never grows beyond the space there is.
	 * </p>
	 *
	 * @param width
	 *        The width as a CSS length, e.g. {@code 380px}, or {@code null} to take the width the
	 *        container offers.
	 */
	public void setWidth(String width) {
		putState(WIDTH, width);
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
	 * Sets the container of this panel; a panel the container lays out as collapsed is
	 * {@link ExpansionState#MINIMIZED minimized} to its header.
	 */
	@Override
	public void setCollapseContainer(CollapseContainer container) {
		_container = container;
		if (container != null && container.isCollapsed()) {
			setExpansionState(ExpansionState.MINIMIZED);
			container.collapsed(COLLAPSED_SIZE);
		}
	}

	/**
	 * Sets the maximize root for maximize delegation.
	 */
	public void setMaximizeRoot(ReactMaximizeRootControl root) {
		_maximizeRoot = root;
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
	@ReactCommandHandler(value = "toggleMinimize", technical = true)
	void handleToggleMinimize() {
		boolean wasMinimized = _expansionState == ExpansionState.MINIMIZED;
		boolean collapsed = !wasMinimized;

		if (collapsed && _container != null && !_container.canCollapse()) {
			return;
		}

		setExpansionState(collapsed ? ExpansionState.MINIMIZED : ExpansionState.NORMALIZED);

		if (_container != null) {
			if (collapsed) {
				_container.collapsed(COLLAPSED_SIZE);
			} else {
				_container.expanded();
			}
		}
	}

	/**
	 * Handles the toggleMaximize command sent by the React client when the maximize button is
	 * clicked.
	 */
	@ReactCommandHandler(value = "toggleMaximize", technical = true)
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
	@ReactCommandHandler(value = "popOut", technical = true)
	void handlePopOut() {
		setExpansionState(ExpansionState.HIDDEN);
	}
}
