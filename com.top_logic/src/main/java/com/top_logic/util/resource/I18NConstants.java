/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.resource;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * {@link I18NConstants} for this package
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en {0} ({1})
	 */
	public static ResKey2 WITH_LANGUAGE__FIELD_LANG;

	/** Key for the title of the form group containing I18N fields. */
	public static ResKey I18N_FORM_GROUP_TITLE;
	
	static {
		initConstants(I18NConstants.class);
	}
}
