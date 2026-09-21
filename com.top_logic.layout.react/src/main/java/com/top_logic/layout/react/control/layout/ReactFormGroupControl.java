/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactCompositeControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;

/**
 * A {@link ReactControl} that renders a nestable, optionally collapsible form section via the
 * {@code TLFormGroup} React component.
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@link #HEADER_CONTROL} - group heading control, or {@code null}</li>
 * <li>{@link #HEADER_ACTIONS} - optional action buttons in the header</li>
 * <li>{@link #COLLAPSIBLE} - whether the group can be collapsed</li>
 * <li>{@link #COLLAPSED} - current collapsed state</li>
 * <li>{@link #BORDER} - the {@link GroupBorder} drawn around the section</li>
 * <li>{@link #FULL_LINE} - whether the group spans the full grid row</li>
 * <li>{@code children} - child controls (inherited from {@link ReactCompositeControl})</li>
 * </ul>
 */
public class ReactFormGroupControl extends ReactCompositeControl {

	private static final String REACT_MODULE = "TLFormGroup";

	/** State key holding the control displaying the group heading, or {@code null} for none. */
	public static final String HEADER_CONTROL = "headerControl";

	/** State key holding the action buttons displayed beside the group heading. */
	public static final String HEADER_ACTIONS = "headerActions";

	/** State key telling whether the user can fold the group away. */
	public static final String COLLAPSIBLE = "collapsible";

	/** State key telling whether the group is currently folded away. */
	public static final String COLLAPSED = "collapsed";

	/**
	 * State key holding the {@link GroupBorder#getExternalName() name} of the {@link GroupBorder}
	 * drawn around the group.
	 */
	public static final String BORDER = "border";

	/** State key telling whether the group spans all columns of the surrounding form grid. */
	public static final String FULL_LINE = "fullLine";

	/**
	 * Frame drawn around a form group.
	 */
	public enum GroupBorder implements ExternallyNamed {

		/** Section without a frame, set apart by its heading alone. */
		NONE("none"),

		/** Section framed by a thin line in the subtle border color. */
		SUBTLE("subtle"),

		/** Section framed by a thin line in the strong border color. */
		OUTLINED("outlined");

		private final String _externalName;

		GroupBorder(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	private final List<ReactControl> _headerActions;

	private ReactControl _headerControl;

	private boolean _collapsed;

	/**
	 * Creates a form group with full configuration.
	 *
	 * @param header
	 *        The group heading text, or {@code null}. Wrapped in a {@link ReactTextControl}
	 *        internally. To use a custom header control, pass {@code null} here and call
	 *        {@link #setHeader(ReactControl)}.
	 * @param collapsible
	 *        Whether the group can be collapsed.
	 * @param collapsed
	 *        Initial collapsed state.
	 * @param border
	 *        The frame drawn around the group.
	 * @param fullLine
	 *        Whether the group spans all parent columns.
	 * @param headerActions
	 *        Optional action buttons in the header.
	 * @param children
	 *        The child controls.
	 */
	public ReactFormGroupControl(ReactContext context, String header, boolean collapsible, boolean collapsed,
			GroupBorder border, boolean fullLine,
			List<? extends ReactControl> headerActions,
			List<? extends ReactControl> children) {
		super(context, null, REACT_MODULE, children);
		_collapsed = collapsed;
		_headerActions = new ArrayList<>(headerActions);
		putState(COLLAPSIBLE, collapsible);
		putState(COLLAPSED, collapsed);
		putState(BORDER, border.getExternalName());
		putState(FULL_LINE, fullLine);
		putState(HEADER_ACTIONS, _headerActions);
		if (header != null) {
			setHeader(new ReactTextControl(context, header));
		}
	}

	/**
	 * Creates a simple form group with a header and default settings.
	 *
	 * @param header
	 *        The group heading text.
	 * @param children
	 *        The child controls.
	 */
	public ReactFormGroupControl(ReactContext context, String header, List<? extends ReactControl> children) {
		this(context, header, false, false, GroupBorder.NONE, false, List.of(), children);
	}

	/**
	 * Convenience: replaces the header with a plain text control.
	 *
	 * @param header
	 *        The new header text, or {@code null} to clear.
	 */
	public void setHeader(String header) {
		setHeader(header != null ? new ReactTextControl(getReactContext(), header) : null);
	}

	/**
	 * Replaces the header control.
	 *
	 * @param headerControl
	 *        The new header control, or {@code null} to clear.
	 */
	public void setHeader(ReactControl headerControl) {
		if (_headerControl != null) {
			_headerControl.cleanupTree();
		}
		_headerControl = headerControl;
		putState(HEADER_CONTROL, headerControl);
	}

	/**
	 * The control displaying the group heading, or {@code null} if the group shows no heading.
	 */
	public ReactControl getHeader() {
		return _headerControl;
	}

	/**
	 * Toggles the collapsed state.
	 */
	public void toggleCollapsed() {
		_collapsed = !_collapsed;
		putState(COLLAPSED, _collapsed);
	}

	/**
	 * Whether the group is currently collapsed.
	 */
	public boolean isCollapsed() {
		return _collapsed;
	}

	/**
	 * Handles the toggleCollapse command sent when the user toggles the collapse state.
	 */
	@ReactCommandHandler(value = "toggleCollapse", technical = true)
	void handleToggleCollapse() {
		toggleCollapsed();
	}

}
