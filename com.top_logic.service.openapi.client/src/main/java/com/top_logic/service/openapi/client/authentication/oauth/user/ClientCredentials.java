/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.oauth.user;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.client.authentication.I18NConstants;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancerFactory;
import com.top_logic.service.openapi.client.authentication.config.ClientAuthentication;
import com.top_logic.service.openapi.client.authentication.oauth.ClientCredentialEnhancer;
import com.top_logic.service.openapi.client.authentication.oauth.ClientCredentialSecret;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;
import com.top_logic.service.openapi.common.authentication.oauth.TokenBasedAuthentication;

/**
 * {@link TokenBasedAuthentication} to authenticate using client credentials.
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
			(ac, sc) -> new ClientCredentialEnhancer(sc, TypedConfigUtil.createInstance(ac.getURIProvider()));
		return SecurityEnhancerFactory.createEnhancer(config, registry, ClientCredentialSecret.class,
			noSecretOrWrongType, enhancerFactory);
	}
}

