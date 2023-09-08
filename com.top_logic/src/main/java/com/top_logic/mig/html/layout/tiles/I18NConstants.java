/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey EDIT_TILE_COMMAND_LABEL;

	public static ResKey DELETE_TILE_COMMAND_LABEL;

	public static ResKey2 UNKNOWN_TILE_REF__REF_NAMES__AVAILABLE;

	public static ResKey2 ILLEGAL_STORED_LAYOUT_TYPE__EXPECTED__ACTUAL;

	static {
		initConstants(I18NConstants.class);
	}
}
