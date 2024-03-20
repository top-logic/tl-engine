/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.base.accesscontrol;

import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * {@link ExternalUserMapping} just resolving the external user name to a {@link Person}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultExternalUserMapping implements ExternalUserMapping {

	/** Singleton {@link DefaultExternalUserMapping} instance. */
	public static final DefaultExternalUserMapping INSTANCE = new DefaultExternalUserMapping();

	/**
	 * Creates a new {@link DefaultExternalUserMapping}.
	 */
	protected DefaultExternalUserMapping() {
		// singleton instance
	}

	@Override
	public Person findAccountForExternalName(String externalUserName) throws LoginDeniedException {
		return ExternalUserMapping.findAccount(externalUserName);
	}

}

