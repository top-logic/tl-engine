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

/**
 * TL-Script functions with the different kinds of {@link SideEffectFree} declarations.
 *
 * @see TestSideEffectFree
 */
@ScriptPrefix(SideEffectFreeFunctions.PREFIX)
public class SideEffectFreeFunctions extends TLScriptFunctions {

	/**
	 * The {@link ScriptPrefix} of the functions in this class.
	 */
	public static final String PREFIX = "testSideEffect";

	/**
	 * Function without a {@link SideEffectFree} annotation.
	 *
	 * @param value
	 *        The value to tag.
	 * @return The value with the prefix <code>plain:</code>.
	 */
	public static String plain(@Mandatory String value) {
		return "plain:" + value;
	}

	/**
	 * Function annotated with {@link SideEffectFree} that must not be evaluated at compile time.
	 *
	 * @param value
	 *        The value to tag.
	 * @return The value with the prefix <code>free:</code>.
	 */
	@SideEffectFree
	public static String free(@Mandatory String value) {
		return "free:" + value;
	}

	/**
	 * Function annotated with {@link SideEffectFree} that may be evaluated at compile time.
	 *
	 * @param value
	 *        The value to tag.
	 * @return The value with the prefix <code>folded:</code>.
	 */
	@SideEffectFree(canEvaluateAtCompileTime = true)
	public static String folded(@Mandatory String value) {
		return "folded:" + value;
	}

}
