/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.apikey;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.client.authentication.I18NConstants;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancerFactory;
import com.top_logic.service.openapi.client.authentication.config.ClientAuthentication;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyConfig;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeySecret;

/**
 * {@link AuthenticationConfig} to authenticate using an API key.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("API key")
public class APIKeyAuthentication extends AbstractConfiguredInstance<APIKeyAuthentication.Config<?>>
		implements ClientAuthentication<APIKeyAuthentication.Config<?>> {
	
	/**
	 * Configuration options for {@link APIKeyAuthentication}.
	 */
	@DisplayOrder({
		Config.DOMAIN,
		Config.PARAMETER_NAME,
		Config.POSITION
	})
	@TagName("api-key-authentication")
	public interface Config<I extends APIKeyAuthentication> extends ClientAuthentication.Config<I>, APIKeyConfig {
		// Pure sum interface.
	}

	/**
	 * Creates a {@link APIKeyAuthentication}.
	 */
	public APIKeyAuthentication(InstantiationContext context, APIKeyAuthentication.Config<?> config) {
		super(context, config);
	}

	@Override
	public SecurityEnhancer createSecurityEnhancer(ServiceMethodRegistry registry) {
		Config<?> config = getConfig();
		Supplier<ResKey> noSecretOrWrongType =
			() -> I18NConstants.ERROR_NO_API_KEY__AUTHENTICATION_NAME.fill(config.getDomain());
		BiFunction<APIKeyAuthentication.Config<?>, APIKeySecret, SecurityEnhancer> enhancerFactory =
			(ac, sc) -> new APIKeyEnhancer(ac.getPosition(), ac.getParameterName(), sc.getAPIKey());
		return SecurityEnhancerFactory.createEnhancer(config, registry, APIKeySecret.class, noSecretOrWrongType,
			enhancerFactory);
	}

}


