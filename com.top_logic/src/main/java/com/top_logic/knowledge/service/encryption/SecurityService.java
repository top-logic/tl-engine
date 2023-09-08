/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.encryption;

import java.security.Provider;
import java.security.Security;
import java.util.LinkedList;
import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service for the application security by, among other things, registering security providers.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp(value = false)
public class SecurityService extends ConfiguredManagedClass<SecurityService.Config> {

	/**
	 * Configuration options for {@link SecurityService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<SecurityService> {

		/** Name of property {@link #getProviders()}. */
		String PROVIDERS = "providers";

		/** Name for the entry tag of property {@link #getProviders()}. */
		String PROVIDER = "provider";

		/**
		 * Security providers.
		 */
		@Name(PROVIDERS)
		@EntryTag(PROVIDER)
		List<PolymorphicConfiguration<? extends Provider>> getProviders();

	}

	private List<Provider> _registeredProviders;

	/**
	 * Creates a {@link SecurityService} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public SecurityService(InstantiationContext context, Config config) {
		super(context, config);

		_registeredProviders = addSecurityProviders(context, config);
	}

	@Override
	protected void shutDown() {
		removeSecurityProviders();

		super.shutDown();
	}

	private List<Provider> addSecurityProviders(InstantiationContext context, Config config) {
		List<Provider> registeredProviders = new LinkedList<>();

		for (PolymorphicConfiguration<? extends Provider> providerConfig : config.getProviders()) {
			Provider provider = context.getInstance(providerConfig);

			Security.addProvider(provider);

			registeredProviders.add(provider);
		}

		return registeredProviders;
	}

	private void removeSecurityProviders() {
		for (Provider provider : _registeredProviders) {
			Security.removeProvider(provider.getName());
		}
	}

	/**
	 * {@link BasicRuntimeModule} for {@link SecurityService}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
	 */
	public static class Module extends TypedRuntimeModule<SecurityService> {

		/**
		 * Singleton {@link SecurityService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<SecurityService> getImplementation() {
			return SecurityService.class;
		}
	}

}
