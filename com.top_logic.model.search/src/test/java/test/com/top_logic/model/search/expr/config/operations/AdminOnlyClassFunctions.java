/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions that are all {@link AdminOnly} by the annotation of their class.
 *
 * @see TestAdminOnly
 */
@ScriptPrefix(AdminOnlyClassFunctions.PREFIX)
@AdminOnly
public class AdminOnlyClassFunctions extends TLScriptFunctions {

	/**
	 * The {@link ScriptPrefix} of the functions in this class.
	 */
	public static final String PREFIX = "testAdminOnlyClass";

	/**
	 * Echoes the given text.
	 *
	 * @param text
	 *        The text to echo.
	 * @return The given text.
	 */
	public static String echo(@Mandatory String text) {
		return text;
	}

}
