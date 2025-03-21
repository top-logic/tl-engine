/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.conf;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;

/**
 * Configuration plug-in to define {@link ServerAuthentication}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ServerAuthentications extends ConfigurationItem {

	/** @see #getAuthentications() */
	String AUTHENTICATIONS = "authentications";

	/**
	 * All available authentications.
	 */
	@Key(AuthenticationConfig.DOMAIN)
	@Name(AUTHENTICATIONS)
	Map<String, ServerAuthentication.Config<?>> getAuthentications();

}

