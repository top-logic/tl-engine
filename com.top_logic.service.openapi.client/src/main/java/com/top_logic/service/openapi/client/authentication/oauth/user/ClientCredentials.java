/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.oauth.user;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.credential.oauth.ClientCredentialsProvider;
import com.top_logic.basic.credential.oauth.ClientSecretAuthentication;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.client.authentication.CredentialEnhancer;
import com.top_logic.service.openapi.client.authentication.I18NConstants;
import com.top_logic.service.openapi.client.authentication.NoSecurityEnhancement;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancerFactory;
import com.top_logic.service.openapi.client.authentication.config.ClientAuthentication;
import com.top_logic.service.openapi.client.authentication.oauth.ClientCredentialSecret;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;
import com.top_logic.service.openapi.common.authentication.oauth.ClientSecret;
import com.top_logic.service.openapi.common.authentication.oauth.TokenBasedAuthentication;
import com.top_logic.service.openapi.common.authentication.oauth.TokenURIProvider;
import com.top_logic.util.Resources;

/**
 * {@link TokenBasedAuthentication} to authenticate using client credentials.
 *
 * <p>
 * Requests are authenticated with an access token obtained by the OAuth 2.0 client credentials
 * grant from the token endpoint of the {@link Config#getURIProvider() URI provider}. The client
 * authenticates at the token endpoint with the client id and secret of its
 * {@link ClientCredentialSecret}, sent in the request body. The token is fetched by a
 * {@link ClientCredentialsProvider}, which caches it and renews it before it expires.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("OpenID authentication")
public class ClientCredentials extends AbstractConfiguredInstance<ClientCredentials.Config<?>>
		implements ClientAuthentication<ClientCredentials.Config<?>> {

	/**
	 * Configuration options for {@link ClientCredentials}.
	 */
	@TagName("client-credentials-authentication")
	public interface Config<I extends ClientCredentials>
			extends ClientAuthentication.Config<I>, TokenBasedAuthentication {
		// Pure sum interface.
	}

	/**
	 * Creates a {@link ClientCredentials} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ClientCredentials(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public SecurityEnhancer createSecurityEnhancer(ServiceMethodRegistry registry) {
		Config<?> config = getConfig();
		Supplier<ResKey> noSecretOrWrongType =
			() -> I18NConstants.ERROR_NO_CLIENT_CREDENTIALS__AUTHENTICATION_NAME.fill(config.getDomain());
		BiFunction<ClientCredentials.Config<?>, ClientCredentialSecret, SecurityEnhancer> enhancerFactory =
			(ac, sc) -> createEnhancer(ac, sc);
		return SecurityEnhancerFactory.createEnhancer(config, registry, ClientCredentialSecret.class,
			noSecretOrWrongType, enhancerFactory);
	}

	private static SecurityEnhancer createEnhancer(Config<?> config, ClientCredentialSecret secret) {
		ClientSecret clientSecret = secret.getClientSecret();
		if (clientSecret == null) {
			ResKey message = I18NConstants.ERROR_NO_CLIENT_SECRET__AUTHENTICATION_NAME.fill(config.getDomain());
			Logger.warn(Resources.getLogInstance().getString(message), ClientCredentials.class);
			return NoSecurityEnhancement.INSTANCE;
		}
		TokenURIProvider uriProvider = TypedConfigUtil.createInstance(config.getURIProvider());
		return new CredentialEnhancer(TypedConfigUtil.createInstance(credentials(uriProvider, clientSecret)));
	}

	/**
	 * The {@link ClientCredentialsProvider} requesting tokens from the token endpoint of the given
	 * {@link TokenURIProvider} with the given client credentials.
	 */
	private static ClientCredentialsProvider.Config credentials(TokenURIProvider uriProvider,
			ClientSecret clientSecret) {
		ClientSecretAuthentication.Config authentication =
			TypedConfiguration.newConfigItem(ClientSecretAuthentication.Config.class);
		authentication.setMethod(ClientSecretAuthentication.Method.POST);
		authentication.setSecret(TypedConfiguration.copy(clientSecret.getPassword()));

		ClientCredentialsProvider.Config result =
			TypedConfiguration.newConfigItem(ClientCredentialsProvider.Config.class);
		uriProvider.initTokenEndpoint(result);
		result.setClientId(clientSecret.getClientId());
		result.setClientAuthentication(authentication);
		return result;
	}
}

