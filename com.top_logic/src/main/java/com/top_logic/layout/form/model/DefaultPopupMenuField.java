/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.listener.EventType;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.PopupMenuHelper;
import com.top_logic.layout.basic.contextmenu.menu.Menu;

/**
 * {@link PopupMenuField} based on fixed list of commands, resp. groups of commands.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultPopupMenuField extends PopupMenuField {

	private final PopupMenuHelper _helper;

	private boolean _commandVisibility = true;

	/**
	 * Creates a new {@link DefaultPopupMenuField}.
	 */
	public DefaultPopupMenuField(String formMemberName, List<List<CommandModel>> commands) {
		super(formMemberName);
		_helper = new PopupMenuHelper(this::setCommandVisibility, Menu.create(commands));
	}

	@Override
	protected void firstListenerAdded(EventType<?, ?, ?> type) {
		_helper.attach();
		super.firstListenerAdded(type);
	}

	@Override
	protected void lastListenerRemoved(EventType<?, ?, ?> type) {
		super.lastListenerRemoved(type);
		_helper.detach();
	}

	@Override
	public Menu getMenu() {
		return _helper.getMenu();
	}

	@Override
	public boolean isLocallyVisible() {
		return _commandVisibility && super.isLocallyVisible();
	}

	private void setCommandVisibility(boolean commandVisibility) {
		if (_commandVisibility != commandVisibility) {
			_commandVisibility = commandVisibility;
			updateDisplayMode();
		}
	}

	/**
	 * Creates a new {@link DefaultPopupMenuField} with the given name and one group with the given
	 * commands in the menu.
	 * 
	 * @param formMemberName
	 *        See {@link PopupMenuField#getName()}.
	 * @param commands
	 *        The commands of the single group to display in the menu.
	 */
	public static DefaultPopupMenuField newField(String formMemberName, List<CommandModel> commands) {
		return new DefaultPopupMenuField(formMemberName, Collections.singletonList(commands));
	}

	/**
	 * Creates a new {@link DefaultPopupMenuField} with the given name and one group with the given
	 * commands in the menu.
	 * 
	 * @param formMemberName
	 *        See {@link PopupMenuField#getName()}.
	 * @param commands
	 *        The commands of the single group to display in the menu.
	 */
	public static DefaultPopupMenuField newField(String formMemberName, CommandModel... commands) {
		return newField(formMemberName, Arrays.asList(commands));
	}

}


