/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.menu;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.basic.CommandModel;

/**
 * Description of a menu consisting of {@link MenuItem}s.
 * 
 * @see #getTitle()
 * @see #getContents()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Menu extends AbstractMenuContents {

	private HTMLFragment _title;

	/**
	 * Creates a {@link Menu}.
	 *
	 */
	public Menu() {
		super();
	}

	/**
	 * Contents displayed in an optional menu title.
	 * 
	 * <p>
	 * A menu only displays a title, if the title contents is non-<code>null</code>.
	 * </p>
	 */
	public HTMLFragment getTitle() {
		return _title;
	}

	/**
	 * @see #getTitle()
	 */
	public void setTitle(HTMLFragment title) {
		_title = title;
	}

	/**
	 * Backwards-compatible API for creating a {@link Menu} from a list of lists of buttons.
	 */
	public static Menu create(List<? extends List<? extends CommandModel>> buttonGroups) {
		Menu result = new Menu();
		for (List<? extends CommandModel> buttonGroup : buttonGroups) {
			result.add(MenuGroup.create(buttonGroup));
		}
		return result;
	}

	/**
	 * Factory method for a {@link Menu} built from a flat list of buttons.
	 * 
	 * @see CommandItem#create(CommandModel)
	 */
	public static Menu create(CommandModel... buttons) {
		Menu result = new Menu();
		for (CommandModel button : buttons) {
			result.add(CommandItem.create(button));
		}
		return result;
	}

}
