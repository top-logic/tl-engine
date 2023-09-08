/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.menu;

import com.top_logic.layout.basic.CommandModel;

/**
 * Part of a {@link Menu}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MenuPart {

	/**
	 * Iteration of all (deeply) contained buttons.
	 */
	Iterable<CommandModel> buttons();

}
