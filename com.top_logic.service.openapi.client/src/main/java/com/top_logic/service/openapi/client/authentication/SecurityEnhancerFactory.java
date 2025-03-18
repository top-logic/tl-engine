/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.client.authentication.config.ClientAuthentication;
import com.top_logic.service.openapi.client.authentication.config.ClientAuthenticationVisitor;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.util.Resources;

/**
 * {@link ClientAuthenticationVisitor} to get {@link SecurityEnhancer} for the visited
 * {@link ClientAuthentication}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityEnhancerFactory {

	/**
	 * Checks whether a matching secret is available and then creates a {@link SecurityEnhancer} for
	 * that secret.
	 */
	public static <AC extends AuthenticationConfig, SC extends SecretConfiguration> SecurityEnhancer createEnhancer(
			AC config,
			ServiceMethodRegistry registry, Class<SC> expectedSecretType, Supplier<ResKey> noSecretOrWrongType,
			BiFunction<AC, SC, SecurityEnhancer> enhancerFactory) {
		SecretConfiguration secretConfig = registry.getConfig().getSecrets().get(config.getDomain());
		if (secretConfig == null || !expectedSecretType.isInstance(secretConfig)) {
			ResKey key = noSecretOrWrongType.get();
			Logger.warn(Resources.getLogInstance().getString(key), SecurityEnhancerFactory.class);
			return NoSecurityEnhancement.INSTANCE;
		} else {
			return enhancerFactory.apply(config, expectedSecretType.cast(secretConfig));
		}
	}

}

