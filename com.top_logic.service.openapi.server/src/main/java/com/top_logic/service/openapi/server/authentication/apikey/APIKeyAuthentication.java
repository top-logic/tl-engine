/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.apikey;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyConfig;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.document.SecuritySchemeType;
import com.top_logic.service.openapi.common.util.OpenAPIConfigs;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.apikey.APIKeyAuthenticator.AllowedKey;
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
		List<AllowedKey> allowedKeys =
			OpenAPIConfigs.secretsOfType(config.getDomain(), availableSecrets, ServerAPIKeySecret.class)
				.filter(this::hasAPIKey)
				.map(s -> new AllowedKey(TypedConfigUtil.createInstance(s.getAPIKey()),
					StringServices.nonNull(s.getUserId())))
				.collect(Collectors.toList());

		if (allowedKeys.isEmpty()) {
			return NeverAuthenticated.missingSecret();
		}
		return new APIKeyAuthenticator(config.getPosition(), config.getParameterName(), allowedKeys);
	}

	/**
	 * Whether the given secret has an API key.
	 * 
	 * <p>
	 * A secret without API key (e.g. an empty API key attribute) is ignored and reported as configuration error, since it
	 * would accept requests sending an empty API key.
	 * </p>
	 */
	private boolean hasAPIKey(ServerAPIKeySecret secret) {
		if (secret.getAPIKey() == null) {
			Config<?> config = getConfig();
			Logger.error("Ignoring API key secret with empty API key for authentication domain '"
				+ config.getDomain() + "' (parameter '" + config.getParameterName() + "').",
				APIKeyAuthentication.class);
			return false;
		}
		return true;
	}

}


