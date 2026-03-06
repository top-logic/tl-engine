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

/**
 * Runtime container for commands owned by a panel or dialog.
 *
 * <p>
 * Holds both explicit commands (from {@code <commands>}) and implicit commands (contributed by child
 * elements like tables). Supports reactive add/remove for lazy children.
 * </p>
 */
public class CommandScope {

	private final List<ViewCommandModel> _explicitCommands;

	private final List<ViewCommandModel> _implicitCommands;

	private final List<Runnable> _listeners;

	/**
	 * Creates a new {@link CommandScope} with the given explicit commands.
	 *
	 * @param explicitCommands
	 *        The commands declared in the panel's {@code <commands>} section.
	 */
	public CommandScope(List<ViewCommandModel> explicitCommands) {
		_explicitCommands = new ArrayList<>(explicitCommands);
		_implicitCommands = new CopyOnWriteArrayList<>();
		_listeners = new CopyOnWriteArrayList<>();
	}

	/**
	 * Add an implicit command (called by child elements).
	 *
	 * @param command
	 *        The command model to add.
	 */
	public void addCommand(ViewCommandModel command) {
		_implicitCommands.add(command);
		fireChanged();
	}

	/**
	 * Remove an implicit command (called when child is destroyed).
	 *
	 * @param command
	 *        The command model to remove.
	 */
	public void removeCommand(ViewCommandModel command) {
		_implicitCommands.remove(command);
		fireChanged();
	}

	/**
	 * All commands (explicit first, then implicit).
	 *
	 * @return Unmodifiable list of all commands.
	 */
	public List<ViewCommandModel> getAllCommands() {
		List<ViewCommandModel> all = new ArrayList<>(_explicitCommands.size() + _implicitCommands.size());
		all.addAll(_explicitCommands);
		all.addAll(_implicitCommands);
		return Collections.unmodifiableList(all);
	}

	/**
	 * Resolves a command by name.
	 *
	 * @param name
	 *        The command name to look up.
	 * @return The command model, or {@code null} if not found.
	 */
	public ViewCommandModel resolveCommand(String name) {
		for (ViewCommandModel model : _explicitCommands) {
			if (name.equals(model.getName())) {
				return model;
			}
		}
		for (ViewCommandModel model : _implicitCommands) {
			if (name.equals(model.getName())) {
				return model;
			}
		}
		return null;
	}

	/**
	 * Listen for changes to the command list (for toolbar re-rendering).
	 *
	 * @param listener
	 *        The listener to add.
	 */
	public void addListener(Runnable listener) {
		_listeners.add(listener);
	}

	/**
	 * Remove a listener.
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
