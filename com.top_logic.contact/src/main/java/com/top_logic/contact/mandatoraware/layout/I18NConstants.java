/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Resources for mandatoraware contact components
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Create command is not executable if contacts are not allowed on mandator.
	 */
	public static ResKey CREATE_CONTACTS_NOT_ALLOWED;

	public static ResKey ERROR_SYSTEM_CONTACT_NOT_EDITABLE = legacyKey("tl.executable.disabled.systemContact.notEditable");

	static {
		initConstants(I18NConstants.class);
	}
}
