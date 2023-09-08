/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * I18N constants for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey ROOT_TILE;

	public static ResPrefix TILE_PATH_INFO;

	public static ResPrefix TILE_ALLOWED_ASSERTION;

	public static ResPrefix TILE_CONTENTS_ASSERTION;

	public static ResPrefix TILE_HIDDEN_ASSERTION;

	public static ResPrefix TILE_PATH_ASSERTION;

	static {
		initConstants(I18NConstants.class);
	}
}
