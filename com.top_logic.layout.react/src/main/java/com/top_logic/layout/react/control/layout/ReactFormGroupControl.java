/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

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
 * <li>{@code children} - child controls</li>
 * </ul>
 */
public class ReactFormGroupControl extends ReactControl {

	private static final String REACT_MODULE = "TLFormGroup";

	private static final String HEADER = "header";

	private static final String HEADER_ACTIONS = "headerActions";

	private static final String COLLAPSIBLE = "collapsible";

	private static final String COLLAPSED = "collapsed";

	private static final String BORDER = "border";

	private static final String FULL_LINE = "fullLine";

	private static final String CHILDREN = "children";

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ToggleCollapseCommand());

	private final List<ReactControl> _children;

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
	public ReactFormGroupControl(String header, boolean collapsible, boolean collapsed,
			String border, boolean fullLine,
			List<? extends ReactControl> headerActions,
			List<? extends ReactControl> children) {
		super(null, REACT_MODULE, COMMANDS);
		_collapsed = collapsed;
		_children = new ArrayList<>(children);
		_headerActions = new ArrayList<>(headerActions);
		if (header != null) {
			putState(HEADER, header);
		}
		putState(COLLAPSIBLE, collapsible);
		putState(COLLAPSED, collapsed);
		putState(BORDER, border);
		putState(FULL_LINE, fullLine);
		putState(HEADER_ACTIONS, _headerActions);
		putState(CHILDREN, _children);
	}

	/**
	 * Creates a simple form group with a header and default settings.
	 *
	 * @param header
	 *        The group heading.
	 * @param children
	 *        The child controls.
	 */
	public ReactFormGroupControl(String header, List<? extends ReactControl> children) {
		this(header, false, false, "none", false, List.of(), children);
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
		for (ReactControl child : _children) {
			child.cleanupTree();
		}
		for (ReactControl action : _headerActions) {
			action.cleanupTree();
		}
	}

	/**
	 * Command sent when the user toggles the collapse state.
	 */
	public static class ToggleCollapseCommand extends ControlCommand {

		/** Creates a {@link ToggleCollapseCommand}. */
		public ToggleCollapseCommand() {
			super("toggleCollapse");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.REACT_FORM_GROUP_TOGGLE_COLLAPSE;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			((ReactFormGroupControl) control).toggleCollapsed();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
