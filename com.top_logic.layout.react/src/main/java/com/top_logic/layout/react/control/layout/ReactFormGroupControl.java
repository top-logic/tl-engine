/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactCompositeControl;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that renders a nestable, optionally collapsible form section via the
 * {@code TLFormGroup} React component.
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@code header} - group heading text, or {@code null}</li>
 * <li>{@code headerActions} - optional action buttons in the header</li>
 * <li>{@code collapsible} - whether the group can be collapsed</li>
 * <li>{@code collapsed} - current collapsed state</li>
 * <li>{@code border} - "none", "subtle", or "outlined"</li>
 * <li>{@code fullLine} - whether the group spans the full grid row</li>
 * <li>{@code children} - child controls (inherited from {@link ReactCompositeControl})</li>
 * </ul>
 */
public class ReactFormGroupControl extends ReactCompositeControl {

	private static final String REACT_MODULE = "TLFormGroup";

	private static final String HEADER = "header";

	private static final String HEADER_ACTIONS = "headerActions";

	private static final String COLLAPSIBLE = "collapsible";

	private static final String COLLAPSED = "collapsed";

	private static final String BORDER = "border";

	private static final String FULL_LINE = "fullLine";

	private final List<ReactControl> _headerActions;

	private boolean _collapsed;

	/**
	 * Creates a form group with full configuration.
	 *
	 * @param header
	 *        The group heading, or {@code null}.
	 * @param collapsible
	 *        Whether the group can be collapsed.
	 * @param collapsed
	 *        Initial collapsed state.
	 * @param border
	 *        "none", "subtle", or "outlined".
	 * @param fullLine
	 *        Whether the group spans all parent columns.
	 * @param headerActions
	 *        Optional action buttons in the header.
	 * @param children
	 *        The child controls.
	 */
	public ReactFormGroupControl(ReactContext context, String header, boolean collapsible, boolean collapsed,
			String border, boolean fullLine,
			List<? extends ReactControl> headerActions,
			List<? extends ReactControl> children) {
		super(context, null, REACT_MODULE, children);
		_collapsed = collapsed;
		_headerActions = new ArrayList<>(headerActions);
		if (header != null) {
			putState(HEADER, header);
		}
		putState(COLLAPSIBLE, collapsible);
		putState(COLLAPSED, collapsed);
		putState(BORDER, border);
		putState(FULL_LINE, fullLine);
		putState(HEADER_ACTIONS, _headerActions);
	}

	/**
	 * Creates a simple form group with a header and default settings.
	 *
	 * @param header
	 *        The group heading.
	 * @param children
	 *        The child controls.
	 */
	public ReactFormGroupControl(ReactContext context, String header, List<? extends ReactControl> children) {
		this(context, header, false, false, "none", false, List.of(), children);
	}

	/**
	 * Updates the group header text.
	 *
	 * @param header
	 *        The new header text, or {@code null} to clear.
	 */
	public void setHeader(String header) {
		putState(HEADER, header);
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

	@Override
	protected void cleanupChildren() {
		super.cleanupChildren();
		for (ReactControl action : _headerActions) {
			action.cleanupTree();
		}
	}

	/**
	 * Handles the toggleCollapse command sent when the user toggles the collapse state.
	 */
	@ReactCommand("toggleCollapse")
	void handleToggleCollapse() {
		toggleCollapsed();
	}

}
