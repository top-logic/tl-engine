/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.favorites;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Resources of this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	public static ResKey REMOVE_TOOLTIP;

	public static ResKey ADD_TOOLTIP;

	public static ResKey DISABLED;
	
	public static ResPrefix EXECUTABLE_COMMAND = legacyPrefix("ModifyFavoritesExecutable.command.");

	public static ResKey IS_FAVORITE;

	public static ResKey IS_NOT_FAVORITE;
	
	static {
		initConstants(I18NConstants.class);
	}

}
