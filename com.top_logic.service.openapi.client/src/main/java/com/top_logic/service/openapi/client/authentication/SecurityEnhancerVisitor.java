/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.client.authentication.apikey.APIKeyEnhancer;
import com.top_logic.service.openapi.client.authentication.http.basic.HTTPAuthenticationEnhancer;
import com.top_logic.service.openapi.client.authentication.oauth.ClientCredentialEnhancer;
import com.top_logic.service.openapi.client.authentication.oauth.ClientCredentialSecret;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.ClientAuthenticationVisitor;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeySecret;
import com.top_logic.service.openapi.common.authentication.http.HTTPSecret;
import com.top_logic.service.openapi.common.authentication.http.basic.BasicAuthentication;
import com.top_logic.service.openapi.common.authentication.oauth.ClientCredentials;
import com.top_logic.util.Resources;

/**
 * {@link ClientAuthenticationVisitor} to get {@link SecurityEnhancer} for the visited
 * {@link AuthenticationConfig}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityEnhancerVisitor implements ClientAuthenticationVisitor<SecurityEnhancer, ServiceMethodRegistry> {

	/** Singleton {@link SecurityEnhancerVisitor} instance. */
	public static final SecurityEnhancerVisitor INSTANCE = new SecurityEnhancerVisitor();

	/**
	 * Creates a new {@link SecurityEnhancerVisitor}.
	 */
	protected SecurityEnhancerVisitor() {
		// singleton instance
	}

	@Override
	public SecurityEnhancer visitAPIKeyAuthentication(APIKeyAuthentication config, ServiceMethodRegistry arg) {
		Supplier<ResKey> noSecretOrWrongType =
			() -> I18NConstants.ERROR_NO_API_KEY__AUTHENTICATION_NAME.fill(config.getDomain());
		BiFunction<APIKeyAuthentication, APIKeySecret, SecurityEnhancer> enhancerFactory =
			(ac, sc) -> new APIKeyEnhancer(ac.getPosition(), ac.getParameterName(), sc.getAPIKey());
		return createEnhancer(config, arg, APIKeySecret.class, noSecretOrWrongType, enhancerFactory);

	}

	@Override
	public SecurityEnhancer visitClientCredentials(ClientCredentials config, ServiceMethodRegistry arg) {
		Supplier<ResKey> noSecretOrWrongType =
			() -> I18NConstants.ERROR_NO_CLIENT_CREDENTIALS__AUTHENTICATION_NAME.fill(config.getDomain());
		BiFunction<ClientCredentials, ClientCredentialSecret, SecurityEnhancer> enhancerFactory =
			(ac, sc) -> new ClientCredentialEnhancer(sc, TypedConfigUtil.createInstance(ac.getURIProvider()));
		return createEnhancer(config, arg, ClientCredentialSecret.class, noSecretOrWrongType, enhancerFactory);

	}

	@Override
	public SecurityEnhancer visitBasicAuthentication(BasicAuthentication config, ServiceMethodRegistry arg) {
		Supplier<ResKey> noSecretOrWrongType =
			() -> I18NConstants.ERROR_BASIC_AUTH_CREDENTIALS__AUTHENTICATION_NAME.fill(config.getDomain());
		BiFunction<BasicAuthentication, HTTPSecret, SecurityEnhancer> enhancerFactory =
			(ac, sc) -> new HTTPAuthenticationEnhancer(sc.getLogin());
		return createEnhancer(config, arg, HTTPSecret.class, noSecretOrWrongType, enhancerFactory);
	}

	private <AC extends AuthenticationConfig, SC extends SecretConfiguration> SecurityEnhancer createEnhancer(AC config,
			ServiceMethodRegistry registry, Class<SC> expectedSecretType, Supplier<ResKey> noSecretOrWrongType,
			BiFunction<AC, SC, SecurityEnhancer> enhancerFactory) {
		SecretConfiguration secretConfig = registry.getConfig().getSecrets().get(config.getDomain());
		if (secretConfig == null || !expectedSecretType.isInstance(secretConfig)) {
			ResKey key = noSecretOrWrongType.get();
			Logger.warn(Resources.getLogInstance().getString(key), SecurityEnhancerVisitor.class);
			return NoSecurityEnhancement.INSTANCE;
		} else {
			return enhancerFactory.apply(config, expectedSecretType.cast(secretConfig));
		}
	}
}

