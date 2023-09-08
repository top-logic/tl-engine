/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityObjectProviderFormat;

/**
 * The {@link SecurityObjectProviderManager} manages the {@link SecurityObjectProvider}s and their
 * aliases to comfort up layout configuration.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class SecurityObjectProviderManager extends ManagedClass {

	private static final String DEFAULT_PROVIDER_ALIAS = "default";
	/** Prefix to denote path in SecurityObjectProvider configuration. */
    public static final String PATH_SECURITY_OBJECT_PROVIDER = "path:";
    
	/**
	 * {@link ConfigurationValueProvider} that resolves a configured {@link SecurityObjectProvider}
	 * through the {@link SecurityObjectProviderManager}.
	 */
	public static final class SecurityProviderByName extends AbstractConfigurationValueProvider<SecurityObjectProvider> {

		/** Sole instance of {@link SecurityProviderByName} */
		public static final SecurityProviderByName INSTANCE = new SecurityProviderByName();

		private SecurityProviderByName() {
			// singleton instance
			super(SecurityObjectProvider.class);
		}

		@Override
		protected String getSpecificationNonNull(SecurityObjectProvider configValue) {
			return configValue.getClass().getName();
		}

		@Override
		protected SecurityObjectProvider getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
			String securityProvider = propertyValue.toString();
			boolean securityProviderConfigured = !StringServices.isEmpty(securityProvider);
			if (securityProviderConfigured) {
				return SecurityObjectProviderManager.getInstance().getSecurityObjectProvider(securityProvider);
			} else {
				return PathSecurityObjectProvider.MODEL_INSTANCE;
			}
		}
	}

	/**
	 * Configuration of a {@link SecurityObjectProviderManager}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ManagedClass.ServiceConfiguration<SecurityObjectProviderManager> {

		/**
		 * Mapping of the alias for a {@link SecurityObjectProvider} to its configuration.
		 */
		@Key(ProviderConfig.NAME_ATTRIBUTE)
		Map<String, ProviderConfig> getProviders();
	}

	/**
	 * Configuration of a {@link SecurityObjectProvider} that also holds a name.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ProviderConfig extends NamedConfigMandatory {

		/**
		 * The alias for the configured {@link SecurityObjectProvider}.
		 * 
		 * @see #getImpl()
		 */
		@Override
		String getName();

		/**
		 * {@link SecurityObjectProvider} for the given alias.
		 * 
		 * @see #getName()
		 */
		@Format(SecurityObjectProviderFormat.class)
		PolymorphicConfiguration<SecurityObjectProvider> getImpl();
	}

	private ConcurrentHashMap<String, SecurityObjectProvider> _providers;

	/**
	 * Creates a new {@link SecurityObjectProviderManager} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link SecurityObjectProviderManager}.
	 */
	public SecurityObjectProviderManager(InstantiationContext context, Config config) {
		super(context, config);
		SecurityObjectProviderContext securityObjectProviderContext = new SecurityObjectProviderContext(context, this);
		_providers = createProviders(securityObjectProviderContext, config);
		securityObjectProviderContext.resolveReferences();
	}

	private ConcurrentHashMap<String, SecurityObjectProvider> createProviders(InstantiationContext context,
			Config config) {
		ConcurrentHashMap<String, SecurityObjectProvider> providers = new ConcurrentHashMap<>();
		for (ProviderConfig provider : config.getProviders().values()) {
			SecurityObjectProvider securityObjectProvider = context.getInstance(provider.getImpl());
			if (securityObjectProvider == null) {
				context.error("Null SecurityObjectProvider for key '" + provider.getName() + "' is not allowed.");
				continue;
			}
			providers.put(provider.getName(), securityObjectProvider);
		}
		if (!providers.containsKey(DEFAULT_PROVIDER_ALIAS)) {
			context.error(
				"No default SecurityObjectProvider configured: Missing configuration '" + DEFAULT_PROVIDER_ALIAS + "'");
		}
		return providers;
	}

    /**
     * Gets the singleton instance of this class.
     *
     * @return the instance of this class
     */
    public static SecurityObjectProviderManager getInstance() {
		return Module.INSTANCE.getImplementationInstance();
    }

	/**
	 * Returns the "default" {@link SecurityObjectProvider}.
	 */
	public final SecurityObjectProvider getDefaultSecurityObjectProvider() {
		try {
			return getSecurityObjectProvider(DEFAULT_PROVIDER_ALIAS);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("No default SecurityObjectProvider configured.");
		}
	}

    /**
	 * Gets the (default) instance of the given security object provider.
	 * 
	 * <p>
	 * This method caches the instances so that there exists only one instance of each security
	 * object provider at once.
	 * </p>
	 *
	 * @param key
	 *        The name of the security object provider to get (may be an alias)
	 * @return The requested security object provider. Never <code>null</code>.
	 * 
	 * @throws ConfigurationException
	 *         If the configured provider cannot be instantiated.
	 */
	public SecurityObjectProvider getSecurityObjectProvider(String key) throws ConfigurationException {
		SecurityObjectProvider existingProvider = _providers.get(key);
		if (existingProvider != null) {
			return existingProvider;
		}
		return MapUtil.putIfAbsent(_providers, key, newProvider(key));
    }

	private SecurityObjectProvider newProvider(String key) throws ConfigurationException {
		PolymorphicConfiguration<? extends SecurityObjectProvider> config =
			SecurityObjectProviderFormat.INSTANCE.getValue(key, key);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	boolean hasSecurityObjectProvider(String key) {
		return _providers.containsKey(key);
	}

	/**
	 * Singleton reference for {@link SecurityObjectProviderManager}.
	 */
	public static final class Module extends TypedRuntimeModule<SecurityObjectProviderManager> {

		/**
		 * Singleton {@link SecurityObjectProviderManager.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<SecurityObjectProviderManager> getImplementation() {
			return SecurityObjectProviderManager.class;
		}

	}

}
