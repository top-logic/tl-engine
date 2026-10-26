/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * The place where the secrets of the installation live, from which a {@link NamedSecretSource}
 * fetches its secret by name.
 *
 * <p>
 * A secret property of a service names its secret, e.g. <code>&lt;api-key
 * name="tl_openai_api_key"/&gt;</code>. Where the named secrets are stored differs between
 * installations (environment variables on a developer machine, mounted files or a vault in
 * production) and is decided once per installation in the global configuration section
 * {@link Config}, instead of in each secret property:
 * </p>
 *
 * <pre>
 * &lt;config config:interface="com.top_logic.basic.credential.SecretStores$Config"&gt;
 *   &lt;store class="com.top_logic.basic.credential.DirectorySecretStore" directory="/run/secrets"/&gt;
 *   &lt;overrides&gt;
 *     &lt;override name="db_password"&gt;
 *       &lt;source class="com.top_logic.basic.credential.FileSecretSource" path="/etc/db/password"/&gt;
 *     &lt;/override&gt;
 *   &lt;/overrides&gt;
 * &lt;/config&gt;
 * </pre>
 *
 * <p>
 * The configuration is a global configuration section of the application configuration and not a
 * service, because secrets are needed before the services start, e.g. the password of the
 * database. The default store is the {@link EnvironmentSecretStore}.
 * </p>
 *
 * @see SecretStore
 */
public final class SecretStores {

	/**
	 * Global configuration of the secret storage of the installation.
	 */
	@Label("Secret storage")
	public interface Config extends ConfigurationItem {

		/**
		 * @see #getStore()
		 */
		String STORE = "store";

		/**
		 * @see #getOverrides()
		 */
		String OVERRIDES = "overrides";

		/**
		 * The store holding the secrets of the installation.
		 */
		@Name(STORE)
		@ItemDefault(EnvironmentSecretStore.class)
		@NonNullable
		PolymorphicConfiguration<? extends SecretStore> getStore();

		/**
		 * @see #getStore()
		 */
		void setStore(PolymorphicConfiguration<? extends SecretStore> value);

		/**
		 * Sources of single secrets that do not live in the {@link #getStore() store}, by the
		 * name of the secret.
		 */
		@Name(OVERRIDES)
		@Key(SecretOverride.NAME_ATTRIBUTE)
		@EntryTag(SecretOverride.TAG_NAME)
		Map<String, SecretOverride> getOverrides();

	}

	/**
	 * The source of a single secret that does not live in the store of the installation.
	 */
	public interface SecretOverride extends NamedConfigMandatory {

		/**
		 * Tag name of a {@link SecretOverride} in {@link Config#getOverrides()}.
		 */
		String TAG_NAME = "override";

		/**
		 * @see #getSource()
		 */
		String SOURCE = "source";

		/**
		 * The source delivering the secret with this name.
		 */
		@Name(SOURCE)
		@Mandatory
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getSource();

		/**
		 * @see #getSource()
		 */
		void setSource(PolymorphicConfiguration<? extends SecretSource> value);

	}

	private static volatile Resolved _resolved;

	private static volatile Config _defaultConfig;

	private static final ThreadLocal<Set<String>> RESOLVING = ThreadLocal.withInitial(HashSet::new);

	private SecretStores() {
		// Static access only.
	}

	/**
	 * The current value of the secret with the given name.
	 *
	 * <p>
	 * The secret is delivered by its {@link Config#getOverrides() override}, if one is configured
	 * for the name, and by the {@link Config#getStore() store} of the installation otherwise.
	 * </p>
	 *
	 * @param name
	 *        The name of the secret.
	 * @return The secret, never <code>null</code> and never empty.
	 *
	 * @throws I18NRuntimeException
	 *         If the secret is not available. The message of the exception never contains the
	 *         secret.
	 */
	public static String getSecret(String name) {
		Set<String> resolving = RESOLVING.get();
		if (!resolving.add(name)) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_OVERRIDE_CYCLE__NAME.fill(name));
		}
		try {
			return resolved().getSecret(name);
		} finally {
			resolving.remove(name);
		}
	}

	private static Resolved resolved() {
		Config config = config();
		Resolved resolved = _resolved;
		if (resolved == null || !resolved.isFor(config)) {
			resolved = new Resolved(config);
			_resolved = resolved;
		}
		return resolved;
	}

	private static Config config() {
		if (ApplicationConfig.Module.INSTANCE.isActive()) {
			return ApplicationConfig.getInstance().getConfig(Config.class);
		}
		Config result = _defaultConfig;
		if (result == null) {
			result = TypedConfiguration.newConfigItem(Config.class);
			_defaultConfig = result;
		}
		return result;
	}

	/**
	 * The store and overrides instantiated from a {@link Config}.
	 *
	 * <p>
	 * It stays valid as long as the configuration is the current one and its store configuration
	 * is unchanged. An override is instantiated on first use and again when its configuration has
	 * changed.
	 * </p>
	 */
	private static final class Resolved {

		private final Config _config;

		private final PolymorphicConfiguration<? extends SecretStore> _storeConfig;

		private final SecretStore _store;

		private final ConcurrentMap<String, OverrideInstance> _overrides = new ConcurrentHashMap<>();

		Resolved(Config config) {
			_config = config;
			_storeConfig = config.getStore();
			_store = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(_storeConfig);
		}

		boolean isFor(Config config) {
			return config == _config && config.getStore() == _storeConfig;
		}

		String getSecret(String name) {
			SecretOverride override = _config.getOverrides().get(name);
			if (override == null) {
				return _store.getSecret(name);
			}
			PolymorphicConfiguration<? extends SecretSource> sourceConfig = override.getSource();
			OverrideInstance instance = _overrides.get(name);
			if (instance == null || instance._sourceConfig != sourceConfig) {
				instance = new OverrideInstance(sourceConfig);
				_overrides.put(name, instance);
			}
			return instance._source.getSecret();
		}

	}

	/**
	 * A {@link SecretSource} instantiated from the configuration of an override.
	 */
	private static final class OverrideInstance {

		final PolymorphicConfiguration<? extends SecretSource> _sourceConfig;

		final SecretSource _source;

		OverrideInstance(PolymorphicConfiguration<? extends SecretSource> sourceConfig) {
			_sourceConfig = sourceConfig;
			_source = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(sourceConfig);
		}

	}

}
