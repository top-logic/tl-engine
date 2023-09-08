/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.form.control.PopupMenuButtonControl;

/**
 * A model displayed by a {@link PopupMenuButtonControl}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface PopupMenuModel extends ButtonUIModel {

	/**
	 * Groups of commands to display in the pop-up menu.
	 */
	Menu getMenu();

}
