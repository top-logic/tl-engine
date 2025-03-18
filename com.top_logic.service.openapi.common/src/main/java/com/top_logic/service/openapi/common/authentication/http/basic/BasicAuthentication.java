/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.http.basic;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.authentication.ServerAuthentication;
import com.top_logic.service.openapi.common.authentication.http.HTTPAuthentication;
import com.top_logic.service.openapi.common.authentication.http.HTTPSecret;
import com.top_logic.service.openapi.common.authentication.http.LoginCredentials;
import com.top_logic.service.openapi.common.authentication.impl.Authenticator;
import com.top_logic.service.openapi.common.authentication.impl.NeverAuthenticated;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.util.OpenAPIConfigs;

/**
 * Authentication using HTTP <i>BasicAuth</i> mechanism.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("HTTP authentication (BasicAuth)")
public class BasicAuthentication extends AbstractConfiguredInstance<BasicAuthentication.Config<?>>
		implements ServerAuthentication<BasicAuthentication.Config<?>> {

	/**
	 * Configuration options for {@link BasicAuthentication}.
	 */
	@TagName("basic-authentication")
	public interface Config<I extends BasicAuthentication> extends ServerAuthentication.Config<I>, HTTPAuthentication {
		// Marker interface.
	}

	/**
	 * Creates a {@link BasicAuthentication} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BasicAuthentication(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public SecuritySchemeObject createSchemaObject(String schemaName) {
		SecuritySchemeObject securityScheme = OpenAPIConfigs.newHTTPAuthentication(schemaName);
		securityScheme.setScheme("Basic");
		return securityScheme;
	}

	@Override
	public Authenticator createAuthenticator(List<? extends SecretConfiguration> availableSecrets) {
		Config<?> config = getConfig();
		Set<LoginCredentials> authentications =
			OpenAPIConfigs.secretsOfType(config, availableSecrets, HTTPSecret.class)
				.map(HTTPSecret::getLogin)
				.collect(Collectors.toSet());
		if (authentications.isEmpty()) {
			return NeverAuthenticated.missingSecret();
		}

		return new BasicAuthAuthenticator(authentications);
	}

}

