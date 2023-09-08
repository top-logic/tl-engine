/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.basic.contextmenu.menu.Menu;

/**
 * Default implementation of a {@link PopupMenuModel}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DefaultPopupMenuModel extends DefaultButtonUIModel implements PopupMenuModel {

	private PopupMenuHelper _helper;

	public DefaultPopupMenuModel(ThemeImage openMenuImage, Menu commands) {
		initHelper(commands);
		setImage(openMenuImage);
	}

	private void initHelper(Menu menu) {
		_helper = new PopupMenuHelper(this::setVisible, menu);
	}

	@Override
	protected void firstListenerAdded() {
		_helper.attach();
		super.firstListenerAdded();
	}

	@Override
	protected void lastListenerRemoved() {
		super.lastListenerRemoved();
		_helper.detach();
	}

	@Override
	public Menu getMenu() {
		return _helper.getMenu();
	}

	public void setCommands(Menu newMenu) {
		if (_helper.isAttached()) {
			_helper.detach();
			initHelper(newMenu);
			_helper.attach();
		} else {
			initHelper(newMenu);
		}
	}

}
