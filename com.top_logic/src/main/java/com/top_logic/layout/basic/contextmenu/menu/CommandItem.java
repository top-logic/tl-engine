/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.menu;

import java.util.Collections;

import com.top_logic.layout.basic.CommandModel;

/**
 * {@link MenuItem} consisting of a {@link CommandModel} button.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommandItem implements MenuItem {

	private final CommandModel _button;

	/**
	 * Creates a {@link CommandItem}.
	 *
	 */
	public CommandItem(CommandModel button) {
		_button = button;
	}

	/**
	 * The button to display in the menu.
	 */
	public CommandModel getButton() {
		return _button;
	}

	@Override
	public boolean isVisible() {
		return _button.isVisible();
	}

	@Override
	public Iterable<CommandModel> buttons() {
		return Collections.singletonList(_button);
	}

	/**
	 * Backwards-compatible API to wrap a {@link CommandModel} into a {@link CommandItem}.
	 */
	public static CommandItem create(CommandModel button) {
		return new CommandItem(button);
	}

}
