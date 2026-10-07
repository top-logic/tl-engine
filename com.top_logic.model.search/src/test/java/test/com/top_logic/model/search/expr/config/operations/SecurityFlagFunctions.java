/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.model.search.expr.config.operations.UsesSecurity;

/**
 * TL-Script functions receiving the security flag of the calling expression through a
 * {@link UsesSecurity} parameter.
 *
 * @see TestUsesSecurity
 */
@ScriptPrefix(SecurityFlagFunctions.PREFIX)
public class SecurityFlagFunctions extends TLScriptFunctions {

	/**
	 * The {@link ScriptPrefix} of the functions in this class.
	 */
	public static final String PREFIX = "testSecurity";

	/**
	 * Name of the {@link UsesSecurity} parameter of {@link #combine(String, boolean, int)}.
	 */
	public static final String SECURITY_PARAM = "usesSecurity";

	/**
	 * Combines the arguments with the security flag of the call.
	 *
	 * <p>
	 * The security flag is placed between the script arguments, so that the arguments following it
	 * must be shifted when calling the Java method.
	 * </p>
	 *
	 * @param prefix
	 *        The first script argument.
	 * @param usesSecurity
	 *        Whether the call is evaluated with the current user's access rights.
	 * @param count
	 *        The second script argument, converted from a script number.
	 * @return The arguments and the security flag, separated by a colon.
	 */
	@SideEffectFree(canEvaluateAtCompileTime = true)
	public static String combine(@Mandatory String prefix, @UsesSecurity boolean usesSecurity, @Mandatory int count) {
		return prefix + ":" + usesSecurity + ":" + count;
	}

}
