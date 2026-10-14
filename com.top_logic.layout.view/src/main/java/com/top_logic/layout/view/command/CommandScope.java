/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.layout.react.control.button.CommandModel;

/**
 * Runtime container for commands owned by a panel or dialog.
 *
 * <p>
 * Holds all commands visible in this scope, whether declared in the panel's {@code <commands>}
 * section or contributed dynamically by child elements. Listeners are notified on every change.
 * </p>
 */
public class CommandScope {

	private final List<CommandModel> _commands;

	private final List<Runnable> _listeners;

	/**
	 * Creates a new {@link CommandScope} with the given initial commands.
	 *
	 * @param initialCommands
	 *        The commands to start with (typically from the panel's configuration).
	 */
	public CommandScope(List<? extends CommandModel> initialCommands) {
		_commands = new CopyOnWriteArrayList<>(initialCommands);
		_listeners = new CopyOnWriteArrayList<>();
	}

	/**
	 * Adds a command to this scope.
	 *
	 * @param command
	 *        The command model to add.
	 */
	public void addCommand(CommandModel command) {
		_commands.add(command);
		fireChanged();
	}

	/**
	 * Adds a command to this scope ahead of all commands it already holds.
	 *
	 * <p>
	 * Within its clique, the toolbar shows the commands in the order of the scope, so a command
	 * added this way comes first in its group - for a command that is the primary one of its group,
	 * contributed by a child element after the scope's own commands are in place.
	 * </p>
	 *
	 * @param command
	 *        The command model to add.
	 */
	public void addCommandFirst(CommandModel command) {
		_commands.add(0, command);
		fireChanged();
	}

	/**
	 * Removes a command from this scope.
	 *
	 * @param command
	 *        The command model to remove.
	 */
	public void removeCommand(CommandModel command) {
		_commands.remove(command);
		fireChanged();
	}

	/**
	 * All commands in this scope.
	 *
	 * @return Unmodifiable snapshot of all commands.
	 */
	public List<CommandModel> getAllCommands() {
		return Collections.unmodifiableList(new ArrayList<>(_commands));
	}

	/**
	 * Resolves a command by name.
	 *
	 * @param name
	 *        The command name to look up.
	 * @return The command model, or {@code null} if not found.
	 */
	public CommandModel resolveCommand(String name) {
		for (CommandModel model : _commands) {
			if (name.equals(model.getName())) {
				return model;
			}
		}
		return null;
	}

	/**
	 * Registers a listener for changes to the command list.
	 *
	 * @param listener
	 *        The listener to add.
	 */
	public void addListener(Runnable listener) {
		_listeners.add(listener);
	}

	/**
	 * Removes a previously registered listener.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	public void removeListener(Runnable listener) {
		_listeners.remove(listener);
	}

	private void fireChanged() {
		for (Runnable listener : _listeners) {
			listener.run();
		}
	}
}
