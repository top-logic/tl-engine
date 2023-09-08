/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config.google;

import org.pac4j.oidc.client.GoogleOidcClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.security.auth.pac4j.config.ClientConfigurator;
import com.top_logic.security.auth.pac4j.config.DefaultOidcClientConfigurator;

/**
 * {@link ClientConfigurator} specialized for {@link GoogleOidcClient}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GoogleClientConfigurator<C extends GoogleClientConfigurator.Config<?>>
		extends DefaultOidcClientConfigurator<C> {

	/**
	 * Configuration options for {@link GoogleClientConfigurator}.
	 */
	@TagName("google")
	public interface Config<I extends GoogleClientConfigurator<?>> extends DefaultOidcClientConfigurator.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link GoogleClientConfigurator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GoogleClientConfigurator(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected OidcClient createClientImpl(OidcConfiguration clientConfig) {
		return new GoogleOidcClient(clientConfig);
	}

}
