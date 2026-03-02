/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import java.util.Map;

import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A sidebar item that executes a server-side command when clicked.
 */
public class CommandItem extends SidebarItem {

	/** Type discriminator for command items. */
	public static final String TYPE_COMMAND = "command";

	private final String _label;

	private final String _icon;

	private final Command _action;

	/**
	 * Functional interface for the server-side command action.
	 */
	@FunctionalInterface
	public interface Command {

		/**
		 * Executes the command.
		 *
		 * @param context
		 *        The display context.
		 * @return The handler result.
		 */
		HandlerResult execute(DisplayContext context);
	}

	/**
	 * Creates a new {@link CommandItem}.
	 *
	 * @param id
	 *        The unique identifier.
	 * @param label
	 *        The display label.
	 * @param icon
	 *        The icon CSS class, or {@code null} for no icon.
	 * @param action
	 *        The server-side action to execute.
	 */
	public CommandItem(String id, String label, String icon, Command action) {
		super(id);
		_label = label;
		_icon = icon;
		_action = action;
	}

	/**
	 * The display label.
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * The icon CSS class, or {@code null}.
	 */
	public String getIcon() {
		return _icon;
	}

	/**
	 * The server-side action.
	 */
	public Command getAction() {
		return _action;
	}

	@Override
	public String getType() {
		return TYPE_COMMAND;
	}

	@Override
	public Map<String, Object> toStateMap() {
		Map<String, Object> map = super.toStateMap();
		map.put(LABEL, _label);
		if (_icon != null) {
			map.put(ICON, _icon);
		}
		return map;
	}

}
