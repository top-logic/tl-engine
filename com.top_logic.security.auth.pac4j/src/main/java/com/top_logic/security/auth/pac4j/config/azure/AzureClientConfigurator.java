/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config.azure;

import org.pac4j.oidc.client.AzureAdClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.AzureAdOidcConfiguration;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.misc.AlwaysNull;
import com.top_logic.security.auth.pac4j.config.ClientConfigurator;
import com.top_logic.security.auth.pac4j.config.DefaultOidcClientConfigurator;

/**
 * {@link ClientConfigurator} for an {@link AzureAdClient} with specialized configuration options.
 * 
 * @see Config#getTenant()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AzureClientConfigurator<C extends AzureClientConfigurator.Config<?>> extends DefaultOidcClientConfigurator<C> {

	/**
	 * Configuration options for {@link AzureClientConfigurator}.
	 */
	@TagName("azure")
	public interface Config<I extends AzureClientConfigurator<?>> extends DefaultOidcClientConfigurator.Config<I> {

		/**
		 * Must not be set because it is hard-coded by the {@link AzureAdClient} implementation.
		 * 
		 * <p>
		 * Set {@link #getTenant()} instead.
		 * </p>
		 */
		@Override
		@Derived(fun = AlwaysNull.class, args = {})
		String getDiscoveryURI();

		/**
		 * @see AzureAdOidcConfiguration#getTenant()
		 */
		@Mandatory
		String getTenant();
	}

	/**
	 * Creates a {@link AzureClientConfigurator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AzureClientConfigurator(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected OidcClient createRawClient() {
		return new AzureAdClient(buildAzureAdConfig());
	}

	/**
	 * Creates configuration for an {@link AzureAdClient}.
	 */
	protected final AzureAdOidcConfiguration buildAzureAdConfig() {
		AzureAdOidcConfiguration result = createAzureConfig();
		fillAzureConfig(result);
		return result;
	}

	/**
	 * Creates the {@link AzureAdClient} configuration object.
	 */
	protected AzureAdOidcConfiguration createAzureConfig() {
		return new AzureAdOidcConfiguration();
	}

	/**
	 * Populates the {@link AzureAdClient} configuration object from the application configuration.
	 */
	protected void fillAzureConfig(AzureAdOidcConfiguration result) {
		fillConfig(result);

		Config<?> config = getConfig();
		result.setTenant(config.getTenant());
	}

}
