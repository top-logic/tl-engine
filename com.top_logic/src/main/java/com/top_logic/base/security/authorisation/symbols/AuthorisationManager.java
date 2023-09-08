/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.authorisation.symbols;

import com.top_logic.base.security.SecurityConfiguration;
import com.top_logic.basic.config.ApplicationConfig;

/**
 * Singleton holder for {@link Authorisation}.
 * 
 * @author tri
 * 
 * @author <a href="mailto:tri@top-logic.com">Thomat Trichter</a>
 */
public class AuthorisationManager {

	private AuthorisationManager() {
		super();
	}
	
	/** 
	 * Returns the authorisation of the application.
	 * 
	 * @see SecurityConfiguration#getAuthorisation()
	 */
	public static Authorisation getAuthorisation() {
		return ApplicationConfig.getInstance().getConfig(SecurityConfiguration.class).getAuthorisation();
	}
}
