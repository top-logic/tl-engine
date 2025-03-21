/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.oauth.user;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.func.misc.AlwaysFalse;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.client.authentication.config.ClientAuthentication;
import com.top_logic.service.openapi.client.authentication.oauth.UserBearerTokenEnhancer;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;

/**
 * Uses the personal access token of the user currently logged in via OIDC for authorization.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WithUserAuthentication extends AbstractConfiguredInstance<WithUserAuthentication.Config<?>>
		implements ClientAuthentication<WithUserAuthentication.Config<?>> {

	/**
	 * Configuration options for {@link WithUserAuthentication}.
	 */
	public interface Config<I extends WithUserAuthentication> extends ClientAuthentication.Config<I> {

		@Override
		@Derived(fun = AlwaysFalse.class, args = {})
		boolean isSeparateSecretNeeded();

	}

	/**
	 * Creates a {@link WithUserAuthentication} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public WithUserAuthentication(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public SecurityEnhancer createSecurityEnhancer(ServiceMethodRegistry registry) {
		return UserBearerTokenEnhancer.INSTANCE;
	}
}
