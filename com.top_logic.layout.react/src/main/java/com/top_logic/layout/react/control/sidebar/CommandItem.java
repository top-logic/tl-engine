/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import java.util.Map;

import com.top_logic.layout.react.ReactContext;
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

	private boolean _hidden;

	private boolean _disabled;

	private String _tooltip;

	/**
	 * Functional interface for the server-side command action.
	 */
	@FunctionalInterface
	public interface Command {

		/**
		 * Executes the command.
		 *
		 * @param context
		 *        The view display context.
		 * @return The handler result.
		 */
		HandlerResult execute(ReactContext context);
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

	/**
	 * Whether this item is withheld from the sidebar UI.
	 *
	 * <p>
	 * A hidden item is neither displayed nor reachable by keyboard, and its action is refused.
	 * This is the state an item takes while the command it hosts reports itself as invisible.
	 * </p>
	 */
	public boolean isHidden() {
		return _hidden;
	}

	/**
	 * @see #isHidden()
	 */
	public void setHidden(boolean hidden) {
		_hidden = hidden;
	}

	/**
	 * Sets {@link #isHidden()}.
	 *
	 * @param hidden
	 *        {@code true} to withhold this item from the sidebar.
	 * @return This instance for fluent chaining.
	 */
	public CommandItem withHidden(boolean hidden) {
		setHidden(hidden);
		return this;
	}

	/**
	 * Whether this item is displayed, but cannot be activated.
	 *
	 * <p>
	 * A disabled item is shown greyed out and skipped by keyboard navigation, and its action is
	 * refused. This is the state an item takes while the command it hosts reports itself as not
	 * executable: the user sees what is offered and that it is out of reach right now.
	 * </p>
	 */
	public boolean isDisabled() {
		return _disabled;
	}

	/**
	 * @see #isDisabled()
	 */
	public void setDisabled(boolean disabled) {
		_disabled = disabled;
	}

	/**
	 * Sets {@link #isDisabled()}.
	 *
	 * @param disabled
	 *        {@code true} to show this item without allowing its activation.
	 * @return This instance for fluent chaining.
	 */
	public CommandItem withDisabled(boolean disabled) {
		setDisabled(disabled);
		return this;
	}

	/**
	 * The text explaining this item, shown when the pointer rests on it, or {@code null} for none.
	 *
	 * <p>
	 * While the command the item hosts is shown out of reach, this is the reason its rules gave for
	 * disabling it: the item says why it cannot be used, as the button of the same command does.
	 * </p>
	 */
	public String getTooltip() {
		return _tooltip;
	}

	/**
	 * @see #getTooltip()
	 */
	public void setTooltip(String tooltip) {
		_tooltip = tooltip;
	}

	/**
	 * Sets {@link #getTooltip()}.
	 *
	 * @param tooltip
	 *        The explaining text, or {@code null} for none.
	 * @return This instance for fluent chaining.
	 */
	public CommandItem withTooltip(String tooltip) {
		setTooltip(tooltip);
		return this;
	}

	@Override
	public String getType() {
		return TYPE_COMMAND;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * {@link #HIDDEN}, {@link #DISABLED} and {@link #TOOLTIP} are written only while set, so an item
	 * in its ordinary state costs no extra field on the wire.
	 * </p>
	 */
	@Override
	public Map<String, Object> toStateMap() {
		Map<String, Object> map = super.toStateMap();
		map.put(LABEL, _label);
		if (_icon != null) {
			map.put(ICON, _icon);
		}
		if (_hidden) {
			map.put(HIDDEN, Boolean.TRUE);
		}
		if (_disabled) {
			map.put(DISABLED, Boolean.TRUE);
		}
		if (_tooltip != null) {
			map.put(TOOLTIP, _tooltip);
		}
		return map;
	}

}
