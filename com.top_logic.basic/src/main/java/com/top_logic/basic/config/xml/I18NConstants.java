/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.xml;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/** Error key if the element tag is not a start element tag */
	public static ResKey2 ERROR_EXPECTED_START_ELEMENT_ACTUALELEMENT_LOCATION;

	/** Error key if the element tag is not an end element tag */
	public static ResKey2 ERROR_EXPECTED_END_ELEMENT_ACTUALELEMENT_LOCATION;

	/** Error key for unexpected element names */
	public static ResKey3 ERROR_UNEXPECTED_ELEMENT_NAME__EXPECTEDNAME_ACTUALNAME_LOCATION;

	/** Error key is a value does not exist */
	public static ResKey2 ERROR_VALUE_DOES_NOT_EXIST__VALUE_LOCATION;

	/** Error key if a value already exists */
	public static ResKey2 ERROR_VALUE_ALREADY_EXISTS__VALUE_LOCATION;

	/** Error key for unknown operations */
	public static ResKey3 ERROR_UNKNOWN_OPERATION__OPERATIONNAME_OPERATION_LOCATION;

	/** Error key for unknown positions */
	public static ResKey3 ERROR_UNKNOWN_POSITION__POSITIONNAME_POSITION_LOCATION;
	
	/** Error key if a reference object does not exist */
	public static ResKey2 ERROR_REFERENCE_OBJECT_NOT_FOUND__VALUE_LOCATION;

	/** Error key if an object to update does not exist */
	public static ResKey2 ERROR_UPDATE_OBJECT_NOT_FOUND__VALUE_LOCATION;

	/** Error key for a missing move position for updates */
	public static ResKey2 ERROR_MOVE_POSITION_MISSING__VALUE_LOCATION;

	/** Error key for unexpected arguments */
	public static ResKey2 ERROR_UNEXPECTED_ARGUMENT__ARGUMENT_LOCATION;

	/** Error key for expected arguments */
	public static ResKey2 ERROR_EXPECTED_ARGUMENT__ARGUMENT_LOCATION;

	static {
		initConstants(I18NConstants.class);
	}
}
