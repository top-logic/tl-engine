/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config;

import org.pac4j.core.profile.CommonProfile;

/**
 * Strategy for retrieving a local user name (ID) from a {@link CommonProfile} authentication
 * result.
 * 
 * @see ClientConfigurator.Config#getUserNameExtractor()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface UserNameExtractor {

	/**
	 * Extracts the user name form the given profile data.
	 */
	String getUserName(CommonProfile arg);

}
