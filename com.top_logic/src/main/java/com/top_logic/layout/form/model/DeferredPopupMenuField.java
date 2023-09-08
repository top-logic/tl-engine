/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.List;

import com.top_logic.basic.col.Provider;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.contextmenu.menu.Menu;

/**
 * {@link PopupMenuField} determining the commands by delegating to a {@link Provider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeferredPopupMenuField extends PopupMenuField {

	private final Provider<List<List<CommandModel>>> _commands;

	/**
	 * Creates a new {@link DeferredPopupMenuField}.
	 */
	public DeferredPopupMenuField(String formMemberName, Provider<List<List<CommandModel>>> commands) {
		super(formMemberName);
		_commands = commands;
	}

	@Override
	public Menu getMenu() {
		return Menu.create(_commands.get());
	}

}

