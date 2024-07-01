/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.authentication.ServerAuthenticationVisitor;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeySecret;
import com.top_logic.service.openapi.common.authentication.http.HTTPSecret;
import com.top_logic.service.openapi.common.authentication.http.LoginCredentials;
import com.top_logic.service.openapi.common.authentication.http.basic.BasicAuthentication;
import com.top_logic.service.openapi.common.authentication.oauth.ServerCredentials;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.authentication.apikey.APIKeyAuthenticator;
import com.top_logic.service.openapi.server.authentication.http.basic.BasicAuthAuthenticator;
import com.top_logic.service.openapi.server.authentication.oauth.ClientCredentialsAuthenticator;
import com.top_logic.service.openapi.server.authentication.oauth.ServerCredentialSecret;
import com.top_logic.util.error.TopLogicException;

/**
 * Visitor determining an {@link Authenticator} for the visited {@link AuthenticationConfig}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AuthenticateVisitor implements ServerAuthenticationVisitor<Authenticator, OpenApiServer> {

	/** Singleton {@link AuthenticateVisitor} instance. */
	public static final AuthenticateVisitor INSTANCE = new AuthenticateVisitor();

	/**
	 * Creates a new {@link AuthenticateVisitor}.
	 */
	protected AuthenticateVisitor() {
		// singleton instance
	}

	@Override
	public Authenticator visitAPIKeyAuthentication(APIKeyAuthentication config, OpenApiServer arg) {
		Set<String> allowedKeys = secretsOfType(config, arg, APIKeySecret.class)
			.map(APIKeySecret::getAPIKey)
			.collect(Collectors.toSet());

		if (allowedKeys.isEmpty()) {
			return missingSecret();
		}
		return new APIKeyAuthenticator(config.getPosition(), config.getParameterName(), allowedKeys);
	}

	private static Authenticator missingSecret() {
		return new NeverAuthenticated(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			I18NConstants.ERROR_MISSING_AUTHENTICATION_SECRET);
	}

	@Override
	public Authenticator visitServerCredentials(ServerCredentials config, OpenApiServer arg) {
		Set<ServerCredentialSecret> secrets = secretsOfType(config, arg, ServerCredentialSecret.class)
			.collect(Collectors.toSet());
		switch (secrets.size()) {
			case 0:
				return missingSecret();
			case 1:
				return new ClientCredentialsAuthenticator(config, secrets.iterator().next());
			default:
				throw new TopLogicException(
					I18NConstants.ERROR_MULTIPLE_CLIENT_CREDENTIAL_SECRETS__DOMAIN.fill(config.getDomain()));
		}
	}

	@Override
	public Authenticator visitBasicAuthentication(BasicAuthentication config, OpenApiServer arg) {
		Set<LoginCredentials> authentications = secretsOfType(config, arg, HTTPSecret.class)
			.map(HTTPSecret::getLogin)
			.collect(Collectors.toSet());
		if (authentications.isEmpty()) {
			return missingSecret();
		}
		return new BasicAuthAuthenticator(authentications);
	}

	private <T extends SecretConfiguration> Stream<T> secretsOfType(AuthenticationConfig authenticationConfig,
			OpenApiServer arg, Class<T> secretType) {
		return arg.getConfig().getSecrets()
			.stream()
			.filter(secretType::isInstance)
			.map(secretType::cast)
			.filter(secret -> secret.getDomain().equals(authenticationConfig.getDomain()));
	}

}

