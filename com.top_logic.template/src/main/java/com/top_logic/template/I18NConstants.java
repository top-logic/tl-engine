/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template;

import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.basic.util.ResKey5;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for template error messages.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/** @see I18NConstantsBase */
	/** the res prefix for model errors */
	public static ResKey4 PARSER_ERROR__EXPECTED_ENCOUNTERED_ROW_COL;
	/** the res prefix for model errors */       
	public static ResKey4 TOKEN_ERROR__ROW_COL_ERROR_AFTER;
	
	public static ResKey3 ID_EXISTS__NAME_ROW_COL;
	public static ResKey3 INVALID_ATTRIBUTES__NAME_ROW_COL;
	public static ResKey3 REFERENCE_NOT_IN_SCOPE__NAME_ROW_COL;
	public static ResKey3 REFERENCE_INVALID__NAME_ROW_COL;
	public static ResKey3 REFERENCE_ALREADY_IN_SCOPE__NAME_ROW_COL;
	public static ResKey3 MISSING_ATTRIBUTE__NAME_ROW_COL;
	public static ResKey5 FUNCTION_MATCHING_TYPE__NAME_ROW_COL_EXPECTED_ENCOUNTERED;
	public static ResKey3 FUNCTION_ARGUMENTS_SIZE__NAME_ROW_COL;
	public static ResKey3 FUNCTION_NOT_EXISTS__NAME_ROW_COL;
	public static ResKey3 FOREACH_WRONG_TYPE__NAME_ROW_COL;
	public static ResKey3 EXPRESSION_OPERATOR_NOT_SUPPORTED__NAME_ROW_COL;
	public static ResKey3 EXPRESSION_OPERATOR_WRONG_TYPE__NAME_ROW_COL;
	public static ResKey3 EXPRESSION_OPERATOR_MATCHING_TYPE__NAME_ROW_COL;

	public static ResKey4 ERROR_TEMPLATE_EXPANSION_FAILED__NAME_ROW_COL_ERROR;
	
	static {
		initConstants(I18NConstants.class);
	}
}
