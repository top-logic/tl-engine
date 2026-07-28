/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey ERROR_TAILING_GARBAGE;

	public static ResKey1 ERROR_EXPRESSION_SYNTAX__DETAILS;

	/** @en Unknown method "{0}" in {1}. */
	public static ResKey2 ERROR_UNKNOWN_METHOD__NAME_EXPR;

	/** @en Expecting exactly two operands in the compare operation {1}: Got {0} operands. */
	public static ResKey2 ERROR_WRONG_OPERANDS_COUNT__NUMBER_EXPR;

	/** @en Expecting a type literal, found: {0} */
	public static ResKey1 ERROR_TYPE_LITERAL_EXPECTED__EXPR;

	/** @en Expecting a type part literal, found: {0} */
	public static ResKey1 ERROR_PART_LITERAL_EXPECTED__EXPR;

	static {
		initConstants(I18NConstants.class);
	}
}
