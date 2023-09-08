/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.resource;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey TEST_RESOURCE_DYNAMIC_HELPER_1;

	public static ResKey TEST_RESOURCE_DYNAMIC_HELPER_2;

	public static ResKey TEST_RESOURCE_DYNAMIC_HELPER_3;

	public static ResKey TEST_RESOURCE_DYNAMIC_HELPER_4;

	static {
		initConstants(I18NConstants.class);
	}

}
