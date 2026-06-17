/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol.loginmethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Entry point for enumerating the {@link LoginMethod}s contributed by all registered
 * {@link LoginMethodProvider}s.
 *
 * <p>
 * Login UIs call {@link #all()} to render external login actions besides the built-in
 * username/password form.
 * </p>
 *
 * @see LoginMethodConfig
 */
public class LoginMethods {

	private LoginMethods() {
		// Utility class.
	}

	/**
	 * All external login methods currently offered by the registered providers.
	 *
	 * @return A possibly empty, never {@code null} list, in provider registration order.
	 */
	public static List<LoginMethod> all() {
		LoginMethodConfig config = ApplicationConfig.getInstance().getConfig(LoginMethodConfig.class);
		List<PolymorphicConfiguration<? extends LoginMethodProvider>> providerConfigs = config.getProviders();
		if (providerConfigs.isEmpty()) {
			return Collections.emptyList();
		}

		DefaultInstantiationContext context = new DefaultInstantiationContext(LoginMethods.class);
		List<LoginMethod> result = new ArrayList<>();
		for (PolymorphicConfiguration<? extends LoginMethodProvider> providerConfig : providerConfigs) {
			LoginMethodProvider provider = context.getInstance(providerConfig);
			if (provider != null) {
				result.addAll(provider.getLoginMethods());
			}
		}
		return result;
	}

}
