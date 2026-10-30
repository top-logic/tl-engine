/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.AuthenticationCategory;

/**
 * Service deciding where the named secrets of the installation live, from which a
 * {@link NamedSecretSource} fetches its secret by name.
 *
 * <p>
 * A secret property of a service names its secret, e.g. <code>&lt;api-key
 * name="tl_openai_api_key"/&gt;</code>. Where the named secrets are stored differs between
 * installations (environment variables on a developer machine, mounted files or a vault in
 * production) and is decided once per installation in the configuration of this service, instead
 * of in each secret property. The configuration can be edited in the service editor like that of
 * any other service.
 * </p>
 *
 * <pre>
 * &lt;config service-class="com.top_logic.basic.credential.SecretStoreService"&gt;
 *   &lt;instance class="com.top_logic.basic.credential.SecretStoreService"&gt;
 *     &lt;store class="com.top_logic.basic.credential.DirectorySecretStore" directory="/run/secrets"/&gt;
 *     &lt;overrides&gt;
 *       &lt;override name="db_password"&gt;
 *         &lt;source class="com.top_logic.basic.credential.FileSecretSource" path="/etc/db/password"/&gt;
 *       &lt;/override&gt;
 *     &lt;/overrides&gt;
 *   &lt;/instance&gt;
 * &lt;/config&gt;
 * </pre>
 *
 * <p>
 * A service reading a named secret declares a dependency on this service (see
 * {@link ServiceDependencies}), so that the secret storage is started before and stopped after
 * the service.
 * </p>
 *
 * @see SecretStore
 */
@Label("Secret storage")
@ServiceCategory.Of(AuthenticationCategory.class)
public class SecretStoreService extends ConfiguredManagedClass<SecretStoreService.Config> {

	/**
	 * Configuration options for {@link SecretStoreService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<SecretStoreService> {

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

	private static final ThreadLocal<Set<String>> RESOLVING = ThreadLocal.withInitial(HashSet::new);

	private final SecretStore _store;

	private final Map<String, SecretSource> _overrides = new HashMap<>();

	/**
	 * Creates a {@link SecretStoreService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating the store and the overrides.
	 * @param config
	 *        The configuration.
	 */
	public SecretStoreService(InstantiationContext context, Config config) {
		super(context, config);
		_store = context.getInstance(config.getStore());
		for (SecretOverride override : config.getOverrides().values()) {
			_overrides.put(override.getName(), context.getInstance(override.getSource()));
		}
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
	public String getSecret(String name) {
		return resolve(name, SecretSource::getSecret, store -> store.getSecret(name));
	}

	/**
	 * The current value of the secret with the given name, if it is held where it is looked for.
	 *
	 * <p>
	 * The secret is looked up like in {@link #getSecret(String)}, but a secret that is absent - its
	 * {@link Config#getOverrides() override} does not hold it, or the {@link Config#getStore()
	 * store} does not - is answered with <code>null</code> instead of an error.
	 * </p>
	 *
	 * @param name
	 *        The name of the secret.
	 * @return The secret, never empty, or <code>null</code> if it is absent.
	 *
	 * @throws I18NRuntimeException
	 *         If the secret is present but cannot be delivered. The message of the exception
	 *         never contains the secret.
	 *
	 * @see SecretSource#lookup()
	 * @see SecretStore#lookup(String)
	 */
	public String lookup(String name) {
		return resolve(name, SecretSource::lookup, store -> store.lookup(name));
	}

	private String resolve(String name, Function<SecretSource, String> fromOverride,
			Function<SecretStore, String> fromStore) {
		Set<String> resolving = RESOLVING.get();
		if (!resolving.add(name)) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_OVERRIDE_CYCLE__NAME.fill(name));
		}
		try {
			SecretSource override = _overrides.get(name);
			if (override != null) {
				return fromOverride.apply(override);
			}
			return fromStore.apply(_store);
		} finally {
			resolving.remove(name);
		}
	}

	/**
	 * The source of the secret with the given name, if it does not live in the store.
	 *
	 * @param name
	 *        The name of the secret.
	 * @return The source of the {@link Config#getOverrides() override} for the name,
	 *         <code>null</code> if the secret is taken from the {@link #getStore() store}.
	 */
	public SecretSource getOverride(String name) {
		return _overrides.get(name);
	}

	/**
	 * The store delivering every secret without {@link #getOverride(String) override}.
	 *
	 * @see Config#getStore()
	 */
	public SecretStore getStore() {
		return _store;
	}

	/**
	 * The store to which the application writes secrets, e.g. access tokens entered by users.
	 *
	 * <p>
	 * A secret written to it is delivered by {@link #getSecret(String)} and {@link #lookup(String)}
	 * of this service under the same name, unless an {@link Config#getOverrides() override} is
	 * configured for the name: the override takes precedence over every store.
	 * </p>
	 *
	 * @return The {@link SecretStore#getWritableStore() writable store} of the
	 *         {@link #getStore() store}, <code>null</code> if the configured store is not writable.
	 *
	 * @see WritableSecretStore
	 */
	public WritableSecretStore getWritableStore() {
		return _store.getWritableStore();
	}

	/**
	 * The {@link #getWritableStore() writable store} of the started {@link SecretStoreService}.
	 *
	 * @return The writable store, <code>null</code> if the installation has no writable store
	 *         configured.
	 *
	 * @throws I18NRuntimeException
	 *         If the {@link SecretStoreService} has not been started.
	 */
	public static WritableSecretStore writableSecretStore() {
		if (!Module.INSTANCE.isActive()) {
			throw new I18NRuntimeException(I18NConstants.ERROR_WRITABLE_SECRET_STORE_NOT_STARTED);
		}
		return Module.INSTANCE.getImplementationInstance().getWritableStore();
	}

	/**
	 * The current value of the secret with the given name from the started
	 * {@link SecretStoreService}.
	 *
	 * @see #getSecret(String)
	 *
	 * @throws I18NRuntimeException
	 *         If the secret is not available, or if the {@link SecretStoreService} has not been
	 *         started.
	 */
	public static String secret(String name) {
		if (!Module.INSTANCE.isActive()) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_STORE_NOT_STARTED__NAME.fill(name));
		}
		return Module.INSTANCE.getImplementationInstance().getSecret(name);
	}

	/**
	 * The current value of the secret with the given name from the started
	 * {@link SecretStoreService}, if it is held where it is looked for.
	 *
	 * @see #lookup(String)
	 *
	 * @throws I18NRuntimeException
	 *         If the secret is present but cannot be delivered, or if the
	 *         {@link SecretStoreService} has not been started.
	 */
	public static String lookupSecret(String name) {
		if (!Module.INSTANCE.isActive()) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_STORE_NOT_STARTED__NAME.fill(name));
		}
		return Module.INSTANCE.getImplementationInstance().lookup(name);
	}

	/**
	 * Module for {@link SecretStoreService}.
	 */
	public static final class Module extends TypedRuntimeModule<SecretStoreService> {

		/**
		 * Singleton {@link SecretStoreService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<SecretStoreService> getImplementation() {
			return SecretStoreService.class;
		}

	}

}
