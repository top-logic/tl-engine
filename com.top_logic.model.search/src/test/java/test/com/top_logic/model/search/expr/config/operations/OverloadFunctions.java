/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions sharing their Java method name with other methods of this class.
 *
 * @see TestScriptFunctionOverloads
 */
@ScriptPrefix(OverloadFunctions.PREFIX)
public class OverloadFunctions extends TLScriptFunctions {

	/**
	 * The {@link ScriptPrefix} of the functions in this class.
	 */
	public static final String PREFIX = "testOverload";

	/**
	 * Script function sharing its name with the private helper {@link #value(int)} and the instance
	 * method {@link #value()}.
	 *
	 * @param text
	 *        The value to mark.
	 * @return The given text marked as result of the public function.
	 */
	public static String value(@Mandatory String text) {
		return "public:" + value(text.length());
	}

	/**
	 * Helper with the name of {@link #value(String)}, not a script function.
	 */
	private static String value(int length) {
		return Integer.toString(length);
	}

	/**
	 * Instance method with the name of {@link #value(String)}, not a script function.
	 *
	 * @return A marker for the instance method.
	 */
	public String value() {
		return "instance";
	}

	/**
	 * Overload of {@link #format(int)} offered as script function <code>testOverloadText</code>.
	 *
	 * @param text
	 *        The text to format.
	 * @return The given text marked as text.
	 */
	@Name("text")
	public static String format(@Mandatory String text) {
		return "text:" + text;
	}

	/**
	 * Overload of {@link #format(String)} offered as script function
	 * <code>testOverloadNumber</code>.
	 *
	 * @param number
	 *        The number to format.
	 * @return The given number marked as number.
	 */
	@Name("number")
	public static String format(@Mandatory int number) {
		return "number:" + number;
	}

}
