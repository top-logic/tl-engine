/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey START_END_DATE_ORDER_MISMATCH;

	public static ResKey ONLY_NUMBERS_ALLOWED;

	public static ResKey ERROR_INVALID_SELECTION__DETAIL_COUNT = legacyKey("layout.form.nonOptionsBasedSelection.big");

	public static ResKey ERROR_VALUES_NOT_UNIQUE = legacyKey("tl.constraint.uniquevalue.fail");

	public static ResKey ERROR_INVALID_PROTOCOL__GIVEN_EXPECTED = legacyKey("layout.form.url.protocol");

	public static ResKey ERROR_INVALID_SELECTION__DETAIL = legacyKey("layout.form.nonOptionsBasedSelection.small");

	/* Error key if a string containts invalid characters. */
	public static ResKey CONTAINS_INVALID_CHARS;

	static {
		initConstants(I18NConstants.class);
	}

}
