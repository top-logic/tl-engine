/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.layout.react.control.button.CommandModel;

/**
 * {@link CommandScope} of content that is only sometimes displayed: it collects the contributions of
 * its content at all times, but mirrors them into the enclosing scope only while
 * {@link #setActive(boolean) active}.
 *
 * <p>
 * Used for the content of a single tab, whose control is kept alive while another tab is displayed
 * (to preserve its state). Without this indirection, the form of a hidden tab would keep its
 * edit/save/cancel commands in the enclosing panel's toolbar: the panel would show a second edit
 * button, operating on a form the user cannot see.
 * </p>
 *
 * @implNote Commands stay in this scope while inactive, so that {@link #resolveCommand(String)}
 *           keeps working for the hidden content (e.g. a slot referencing a command by name).
 */
public class ForwardingCommandScope extends CommandScope {

	private final CommandScope _target;

	private boolean _active;

	/**
	 * Creates a {@link ForwardingCommandScope}, initially inactive.
	 *
	 * @param target
	 *        The enclosing scope that receives the commands while this scope is active.
	 */
	public ForwardingCommandScope(CommandScope target) {
		super(List.of());
		_target = target;
	}

	@Override
	public void addCommand(CommandModel command) {
		super.addCommand(command);
		if (_active) {
			_target.addCommand(command);
		}
	}

	@Override
	public void removeCommand(CommandModel command) {
		super.removeCommand(command);
		if (_active) {
			_target.removeCommand(command);
		}
	}

	/**
	 * Whether the commands of this scope are currently mirrored into the enclosing scope.
	 */
	public boolean isActive() {
		return _active;
	}

	/**
	 * Shows or hides this scope's commands in the enclosing scope.
	 *
	 * @param active
	 *        Whether the content of this scope is the displayed one.
	 */
	public void setActive(boolean active) {
		if (active == _active) {
			return;
		}
		_active = active;
		for (CommandModel command : getAllCommands()) {
			if (active) {
				_target.addCommand(command);
			} else {
				_target.removeCommand(command);
			}
		}
	}
}
