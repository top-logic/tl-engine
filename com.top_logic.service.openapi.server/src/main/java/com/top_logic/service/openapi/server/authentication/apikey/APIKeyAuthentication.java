/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.apikey;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyConfig;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.document.SecuritySchemeType;
import com.top_logic.service.openapi.common.util.OpenAPIConfigs;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;
import com.top_logic.service.openapi.server.authentication.impl.NeverAuthenticated;

/**
 * {@link AuthenticationConfig} to authenticate using an API key.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("API key")
public class APIKeyAuthentication extends AbstractConfiguredInstance<APIKeyAuthentication.Config<?>>
		implements ServerAuthentication<APIKeyAuthentication.Config<?>> {
	
	/**
	 * Configuration options for {@link APIKeyAuthentication}.
	 */
	@DisplayOrder({
		Config.DOMAIN,
		Config.PARAMETER_NAME,
		Config.POSITION
	})
	@TagName("api-key-authentication")
	public interface Config<I extends APIKeyAuthentication> extends ServerAuthentication.Config<I>, APIKeyConfig {
		// Pure sum interface.
	}

	/**
	 * Creates a {@link APIKeyAuthentication}.
	 */
	public APIKeyAuthentication(InstantiationContext context, APIKeyAuthentication.Config<?> config) {
		super(context, config);
	}

	@Override
	public SecuritySchemeObject createSchemaObject(String schemaName) {
		APIKeyAuthentication.Config<?> config = getConfig();
		SecuritySchemeObject securityScheme = OpenAPIConfigs.newSecuritySchema(schemaName);
		securityScheme.setType(SecuritySchemeType.API_KEY);
		OpenAPIConfigs.transferIfNotEmpty(config::getParameterName, securityScheme::setName);
		OpenAPIConfigs.transferIfNotEmpty(config::getPosition, securityScheme::setIn);
		return securityScheme;
	}

	@Override
	public Authenticator createAuthenticator(List<? extends SecretConfiguration> availableSecrets) {
		Config<?> config = getConfig();
		Map<String, String> allowedKeys =
			OpenAPIConfigs.secretsOfType(config.getDomain(), availableSecrets, ServerAPIKeySecret.class)
				.collect(Collectors.toMap(s -> s.getAPIKey(), s -> s.getUserId()));

		if (allowedKeys.isEmpty()) {
			return NeverAuthenticated.missingSecret();
		}
		return new APIKeyAuthenticator(config.getPosition(), config.getParameterName(), allowedKeys);
	}

}


