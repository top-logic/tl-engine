/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Declarative contribution to a context menu: one {@link ViewChannel} providing the
 * target object, and the {@link CommandModel}s that consume it.
 *
 * <p>
 * Runtime-only; views create these, controls receive them.
 * </p>
 */
public final class ContextMenuContribution {

	private final ViewChannel _target;

	private final List<CommandModel> _commands;

	/**
	 * Creates a {@link ContextMenuContribution}.
	 *
	 * @param target
	 *        The channel carrying the target object for the contributed commands.
	 * @param commands
	 *        The commands to contribute. Copied defensively.
	 */
	public ContextMenuContribution(ViewChannel target, Collection<? extends CommandModel> commands) {
		_target = target;
		_commands = Collections.unmodifiableList(new ArrayList<>(commands));
	}

	/**
	 * The channel carrying the target object for this contribution's commands.
	 */
	public ViewChannel target() {
		return _target;
	}

	/**
	 * All contributed commands in the order they were provided.
	 */
	public List<CommandModel> commands() {
		return _commands;
	}

	/**
	 * The subset of {@link #commands()} whose {@link CommandModel#isVisible()} returns {@code true}.
	 */
	public List<CommandModel> visibleCommands() {
		List<CommandModel> result = new ArrayList<>();
		for (CommandModel cmd : _commands) {
			if (cmd.isVisible()) {
				result.add(cmd);
			}
		}
		return result;
	}
}
