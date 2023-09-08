/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 MESSAGE__X;

	public static ResKey2 MESSAGE__X_Y;

	public static ResKey NO_MESSAGE;

	public static ResKey OID;
	
	public static ResKey TEST;

	public static ResKey EXISTING_1;

	public static ResKey EXISTING_2;

	public static ResKey EXISTING_3;

	public static ResKey UNDEFINED_1;

	public static ResKey UNDEFINED_2;

	public static ResKey FOR_TEST = ResKey.legacy("test.message0");

	public static ResKey FOR_TEST_1 = ResKey.legacy("test.message1");

	public static ResKey FOR_TEST_2 = ResKey.legacy("test.message2");

	public static ResKey1 LABELS_FOR_BUSINESS_OBJECTS;

	static {
		initConstants(I18NConstants.class);
	}

}
