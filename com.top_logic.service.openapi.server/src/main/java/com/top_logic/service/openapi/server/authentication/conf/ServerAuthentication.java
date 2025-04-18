/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.conf;

import java.util.List;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.server.authentication.Authenticator;

/**
 * Configured factory for {@link Authenticator} implementations in an <i>OpenAPI</i> server.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ServerAuthentication<C extends ServerAuthentication.Config<?>> extends ConfiguredInstance<C> {

	/**
	 * Configuration options for {@link ServerAuthentication}.
	 */
	@Abstract
	public interface Config<I extends ServerAuthentication<?>>
			extends PolymorphicConfiguration<I>, AuthenticationConfig {
		// Base interface.
	}

	/**
	 * Exports this authentication to an <i>OpenAPI</i> specification.
	 */
	SecuritySchemeObject createSchemaObject(String schemaName);

	/**
	 * Creates an {@link Authenticator} that actually performs request authentication.
	 *
	 * @param availableSecrets
	 *        The configured secrets from the <i>OpenAPI</i> server configuration.
	 */
	Authenticator createAuthenticator(List<? extends SecretConfiguration> availableSecrets);

}
