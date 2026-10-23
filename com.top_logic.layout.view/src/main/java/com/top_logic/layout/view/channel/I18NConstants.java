/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Invalid input "{0}": expected a channel name, optionally followed by "as" and the name of
	 *     the argument.
	 */
	public static ResKey1 ERROR_INVALID_INPUT__VALUE;

	/**
	 * @en Unexpected element "{0}" in a list of inputs, expected "{1}" {2}.
	 */
	public static ResKey3 ERROR_UNEXPECTED_INPUT_ELEMENT__NAME_EXPECTED_LOCATION;

	/**
	 * @en An input requires the attribute "{0}" naming its channel {1}.
	 */
	public static ResKey2 ERROR_MISSING_INPUT_CHANNEL__ATTRIBUTE_LOCATION;

	/**
	 * @en The script declares the parameter "{0}". A script of a view sees its values as the
	 *     variables {1} and is evaluated without arguments, so the declaration makes its result a
	 *     function: remove the parameter declaration and use the variables as they are.
	 */
	public static ResKey2 ERROR_SCRIPT_PARAMETER__PARAMETER_VARIABLES;

	/**
	 * @en The script declares the parameter "{0}". A script of a view is evaluated without
	 *     arguments, so the declaration makes its result a function: remove the parameter
	 *     declaration.
	 */
	public static ResKey1 ERROR_SCRIPT_PARAMETER_WITHOUT_VARIABLES__PARAMETER;

	/**
	 * @en The input "{1}" of the script is named "{0}", like a value the element supplies to the
	 *     script, so the script cannot tell them apart. Name the input differently with "as" (e.g.
	 *     "{1} as other"), or the supplied value with the attribute "{2}".
	 */
	public static ResKey3 ERROR_INPUT_NAMED_LIKE_SUPPLIED__NAME_INPUT_ATTRIBUTE;

	/**
	 * @en The input "{1}" of the script is named "{0}", like a value the element supplies to the
	 *     script, so the script cannot tell them apart. Name the input differently with "as" (e.g.
	 *     "{1} as other").
	 */
	public static ResKey2 ERROR_INPUT_NAMED_LIKE_FIXED_SUPPLIED__NAME_INPUT;

	/**
	 * @en Two values the element supplies to the script are named "{0}", so the script cannot tell
	 *     them apart. Name one of them differently with the attribute "{1}".
	 */
	public static ResKey2 ERROR_SUPPLIED_NAMES_CLASH__NAME_ATTRIBUTE;

	static {
		initConstants(I18NConstants.class);
	}
}
