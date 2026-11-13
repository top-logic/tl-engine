/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.select;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Resource keys of the options the tests of the select controls offer; their texts are in the test
 * bundle {@code testLayoutReactMessages}.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey PRIORITY_HIGH;

	public static ResKey PRIORITY_LOW;

	static {
		initConstants(I18NConstants.class);
	}
}
