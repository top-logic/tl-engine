/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link CommandRegistry} based on lists.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleCommandRegistry extends ArrayList<String> implements CommandRegistry {

	private List<String> _commands = new ArrayList<>();

	/**
	 * The list containing the commands added in {@link #registerButton(String)}.
	 */
	public List<String> getButtons() {
		return this;
	}

	/**
	 * The list containing the commands added in {@link #registerCommand(String)}.
	 */
	public List<String> getCommands() {
		return _commands;
	}

	@Override
	public void registerButton(String command) {
		getButtons().add(command);
	}

	@Override
	public void registerCommand(String command) {
		getCommands().add(command);
	}

	@Override
	public void unregisterHandler(String command) {
		if (!getButtons().remove(command)) {
			getCommands().remove(command);
		}
	}

	@Override
	public String toString() {
		return "Buttons: " + super.toString() + ", Commands: " + getCommands().toString();
	}

}

