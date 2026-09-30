/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions of which one is {@link AdminOnly}.
 *
 * @see TestAdminOnly
 */
@ScriptPrefix(AdminOnlyFunctions.PREFIX)
public class AdminOnlyFunctions extends TLScriptFunctions {

	/**
	 * The {@link ScriptPrefix} of the functions in this class.
	 */
	public static final String PREFIX = "testAdminOnly";

	/**
	 * Echoes the given text; only an administrator may call it interactively.
	 *
	 * @param text
	 *        The text to echo.
	 * @return The given text.
	 */
	@AdminOnly
	@SideEffectFree(canEvaluateAtCompileTime = true)
	public static String restricted(@Mandatory String text) {
		return text;
	}

	/**
	 * Echoes the given text; every user may call it.
	 *
	 * @param text
	 *        The text to echo.
	 * @return The given text.
	 */
	@SideEffectFree(canEvaluateAtCompileTime = true)
	public static String open(@Mandatory String text) {
		return text;
	}

}
