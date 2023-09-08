/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config.keycloak;

import org.pac4j.oidc.client.KeycloakOidcClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.KeycloakOidcConfiguration;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.misc.AlwaysNull;
import com.top_logic.security.auth.pac4j.config.ClientConfigurator;
import com.top_logic.security.auth.pac4j.config.DefaultOidcClientConfigurator;

/**
 * {@link ClientConfigurator} for an {@link KeycloakOidcClient} with specialized configuration
 * options.
 * 
 * @see Config#getRealm()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KeycloakClientConfigurator<C extends KeycloakClientConfigurator.Config<?>> extends DefaultOidcClientConfigurator<C> {

	/**
	 * Configuration options for {@link KeycloakClientConfigurator}.
	 */
	@TagName("keycloak")
	public interface Config<I extends KeycloakClientConfigurator<?>> extends DefaultOidcClientConfigurator.Config<I> {

		/**
		 * Must not be set because it is hard-coded by the {@link KeycloakOidcClient}
		 * implementation.
		 * 
		 * <p>
		 * Set {@link #getBaseUri()} and {@link #getRealm()} instead.
		 * </p>
		 */
		@Override
		@Derived(fun = AlwaysNull.class, args = {})
		String getDiscoveryURI();

		/**
		 * @see KeycloakOidcConfiguration#getBaseUri()
		 */
		@Mandatory
		String getBaseUri();

		/**
		 * @see KeycloakOidcConfiguration#getRealm()
		 */
		@Mandatory
		String getRealm();

	}

	/**
	 * Creates a {@link KeycloakClientConfigurator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public KeycloakClientConfigurator(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected OidcClient createRawClient() {
		return new KeycloakOidcClient(buildKeycloakConfig());
	}

	/**
	 * Creates the {@link KeycloakOidcClient} configuration object.
	 */
	protected final KeycloakOidcConfiguration buildKeycloakConfig() {
		KeycloakOidcConfiguration result = createKeycloakConfig();
		fillKeycloakConfig(result);
		return result;
	}

	/**
	 * Creates an uninitialized {@link KeycloakOidcClient} configuration object.
	 */
	protected KeycloakOidcConfiguration createKeycloakConfig() {
		return new KeycloakOidcConfiguration();
	}

	/**
	 * Populates the given {@link KeycloakOidcClient} configuration object from the application
	 * configuration.
	 */
	protected void fillKeycloakConfig(KeycloakOidcConfiguration result) {
		fillConfig(result);

		Config<?> config = getConfig();
		result.setBaseUri(config.getBaseUri());
		result.setRealm(config.getRealm());
	}

}
