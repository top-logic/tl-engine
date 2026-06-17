/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol.loginmethod;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;

/**
 * Application-level configuration listing the registered {@link LoginMethodProvider}s.
 *
 * <p>
 * Accessed through {@link com.top_logic.basic.config.ApplicationConfig}. Modules that contribute
 * external login methods append their provider, e.g.:
 * </p>
 *
 * <pre>
 * &lt;config config:interface="com.top_logic.base.accesscontrol.loginmethod.LoginMethodConfig"&gt;
 *     &lt;providers&gt;
 *         &lt;provider class="com.top_logic.security.auth.pac4j...Pac4jLoginMethodProvider"/&gt;
 *     &lt;/providers&gt;
 * &lt;/config&gt;
 * </pre>
 *
 * @see LoginMethods#all()
 */
public interface LoginMethodConfig extends ConfigurationItem {

	/** Configuration name for {@link #getProviders()}. */
	String PROVIDERS = "providers";

	/**
	 * The registered {@link LoginMethodProvider}s.
	 */
	@Name(PROVIDERS)
	@DefaultContainer
	List<PolymorphicConfiguration<? extends LoginMethodProvider>> getProviders();

}
