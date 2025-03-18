/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.http.basic;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.client.authentication.I18NConstants;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancerFactory;
import com.top_logic.service.openapi.client.authentication.config.ClientAuthentication;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;
import com.top_logic.service.openapi.common.authentication.http.HTTPAuthentication;
import com.top_logic.service.openapi.common.authentication.http.HTTPSecret;

/**
 * Authentication using HTTP <i>BasicAuth</i> mechanism.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("HTTP authentication (BasicAuth)")
public class BasicAuthentication extends AbstractConfiguredInstance<BasicAuthentication.Config<?>>
		implements ClientAuthentication<BasicAuthentication.Config<?>> {

	/**
	 * Configuration options for {@link BasicAuthentication}.
	 */
	@TagName("basic-authentication")
	public interface Config<I extends BasicAuthentication> extends ClientAuthentication.Config<I>, HTTPAuthentication {
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
	public SecurityEnhancer createSecurityEnhancer(ServiceMethodRegistry registry) {
		Config<?> config = getConfig();
		Supplier<ResKey> noSecretOrWrongType =
			() -> I18NConstants.ERROR_BASIC_AUTH_CREDENTIALS__AUTHENTICATION_NAME.fill(config.getDomain());
		BiFunction<BasicAuthentication.Config<?>, HTTPSecret, SecurityEnhancer> enhancerFactory =
			(ac, sc) -> new HTTPAuthenticationEnhancer(sc.getLogin());
		return SecurityEnhancerFactory.createEnhancer(config, registry, HTTPSecret.class, noSecretOrWrongType,
			enhancerFactory);
	}

}

