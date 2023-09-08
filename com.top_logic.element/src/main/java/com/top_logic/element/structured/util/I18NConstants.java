/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link I18NConstants} for this package
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** I18N constant for {@link NumberHandler} if no date pattern is given. */
	public static ResKey ERROR_NUMBER_HANDLER_NO_DATE_PATTERN;

	/** I18N constant for {@link NumberHandler} if no label provider is given. */
	public static ResKey ERROR_NUMBER_HANDLER_NO_LABEL_PROVIDER;

	/** I18N constant for {@link NumberHandler} if no number format is given. */
	public static ResKey1 ERROR_NUMBER_HANDLER_NO_NUMBER_PATTERN;

	/** I18N constant for {@link NumberHandler} if an invalid number format is given. */
	public static ResKey ERROR_NUMBER_HANDLER_INVALID_NUMBER_PATTERN;

	/** I18N constant for {@link NumberHandler} if an invalid date pattern is given. */
	public static ResKey ERROR_NUMBER_HANDLER_INVALID_DATE_PATTERN;

	/**
	 * I18N constant used when creating a default attribute value using a {@link NumberHandler}
	 * fail.
	 */
	public static ResKey2 ERROR_CREATE_NUMBER_HANDLER_DEFAULT__NUMBER_HANDLER__ATTRIBUTE;

	/**
	 * I18N constant used as {@link ExecutableState#getI18NReasonKey()} for commands that can not be
	 * executed on {@link StructuredElement#isRoot() structure root} elements.
	 */
	public static ResKey COMMAND_DISABLED_FOR_ROOT;
	
	static {
		initConstants(I18NConstants.class);
	}
}
