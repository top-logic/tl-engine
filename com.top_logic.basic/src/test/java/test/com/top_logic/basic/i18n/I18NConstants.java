/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.i18n;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;

/**
 * I18NConstants for tests.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	public static ResKey EDIT_TOKEN_TIMED_OUT;

	public static ResKey1 FORMAT_INVALID__VALUE;

	/**
	 * I18N resource key pointing to a message pattern that explains that a
	 * value was entered with an illegal format. The message pattern takes to
	 * arguments: the entered value and an example for a valid value.
	 */
	public static ResKey2 FORMAT_INVALID__VALUE_EXAMPLE; 

	static {
		initConstants(I18NConstants.class);
	}

}
