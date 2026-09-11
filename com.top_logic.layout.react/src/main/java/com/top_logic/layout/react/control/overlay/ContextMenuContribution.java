/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.layout.react.control.button.CommandModel;

/**
 * Declarative contribution to a context menu: one target setter and the {@link CommandModel}s that
 * consume it.
 *
 * <p>
 * Runtime-only; constructed by view-layer code, consumed by {@link ContextMenuOpener}.
 * </p>
 */
public final class ContextMenuContribution {

	private final Consumer<Object> _setTarget;

	private final List<CommandModel> _commands;

	private final String _label;

	/**
	 * Creates a {@link ContextMenuContribution} without a heading.
	 *
	 * @param setTarget
	 *        Sink that publishes the target object for the contributed commands.
	 * @param commands
	 *        The commands to contribute. Copied defensively.
	 */
	public ContextMenuContribution(Consumer<Object> setTarget, Collection<? extends CommandModel> commands) {
		this(setTarget, commands, null);
	}

	/**
	 * Creates a {@link ContextMenuContribution}.
	 *
	 * @param setTarget
	 *        Sink that publishes the target object for the contributed commands.
	 * @param commands
	 *        The commands to contribute. Copied defensively.
	 * @param label
	 *        The heading the menu shows above this contribution's entries, or {@code null} for
	 *        none. Already resolved for the session the menu is built for.
	 */
	public ContextMenuContribution(Consumer<Object> setTarget, Collection<? extends CommandModel> commands,
			String label) {
		_setTarget = setTarget;
		_commands = Collections.unmodifiableList(new ArrayList<>(commands));
		_label = label;
	}

	/**
	 * Sink for publishing the target object for this contribution's commands.
	 */
	public Consumer<Object> setTarget() {
		return _setTarget;
	}

	/**
	 * The heading shown above this contribution's entries, or {@code null} for none.
	 *
	 * <p>
	 * Shown only while the contribution has {@link #visibleCommands() visible commands}: a heading
	 * names the entries beneath it, so without them it is dropped along with the separator.
	 * </p>
	 */
	public String label() {
		return _label;
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
